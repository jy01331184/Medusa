package com.medusa.bundle;

import com.medusa.application.MedusaApplicationProxy;
import com.medusa.application.MedusaBundle;
import com.medusa.application.MedusaLisenter;
import com.medusa.classloader.BundleClassLoader;
import com.medusa.classloader.MedusaClassLoader;
import com.medusa.util.BundleComparetor;
import com.medusa.util.BundleLoadCallbackRunnable;
import com.medusa.util.BundleLoadRunnable;
import com.medusa.util.Constant;
import com.medusa.util.Log;
import com.medusa.util.ReflectUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleExecutor extends ThreadPoolExecutor {

    private static BundleExecutor instance;

    private BundleExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public static BundleExecutor getInstance() {
        if (instance == null)
            instance = new BundleExecutor(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        return instance;
    }

    private List<Bundle> removeBundles = new ArrayList<>();
    private ThreadLocal<Set<Bundle>> bundleInLoadingLocal = new ThreadLocal<Set<Bundle>>() {
        @Override
        protected Set<Bundle> initialValue() {
            return Collections.synchronizedSet(new HashSet<Bundle>());
        }
    };
    private ThreadLocal<LinkedList<Bundle>> medusaBundlesLocal = new ThreadLocal<LinkedList<Bundle>>() {
        @Override
        protected LinkedList<Bundle> initialValue() {
            return new LinkedList<>();
        }
    };

    public void deleteBundle(Bundle bundle) {
        removeBundles.add(bundle);
    }

    public synchronized void loadBundle(MedusaClassLoader classLoader,final Bundle bundle, String medusaBundleId, String lazyWaitValue, android.os.Bundle param) {
        List<Bundle> tasks = new LinkedList<>();
        tasks.add(bundle);
        execute(new BundleLoadCallbackRunnable(classLoader, tasks, medusaBundleId,lazyWaitValue,param));
    }

    public synchronized void loadBundle(Collection<Bundle> bundles, MedusaClassLoader classLoader, MedusaLisenter lisenter) {
        List<Bundle> tasks = new ArrayList<>();

        for (Bundle bundle : bundles) {
            if (!bundle.loaded && bundle.priority == Constant.PRIORITY_MAX) {
                loadBundle(classLoader, bundle, lisenter);
            } else if (!bundle.loaded && bundle.priority < Constant.PRIORITY_LAZY) {
                tasks.add(bundle);
            }
        }
        if (!tasks.isEmpty()) {
            Collections.sort(tasks, new BundleComparetor());
            execute(new BundleLoadRunnable(classLoader, tasks, lisenter));
        }
    }

    public void commit() {
        for (int i = removeBundles.size() - 1; i >= 0; i--) {
            BundleManager.getInstance().removeBundle(removeBundles.remove(i));
        }
    }

    public void loadBundle(MedusaClassLoader classLoader, Bundle bundle, MedusaLisenter lisenter) {
        try {
            if (bundleInLoadingLocal.get().contains(bundle)) {
                return;
            }
            //Log.info(this, "enter :" + bundle.artifactId);
            bundleInLoadingLocal.get().add(bundle);
            long time = System.currentTimeMillis();
            File bundleFile = Constant.getBundleFile(bundle);
//            if (!BundleUtil.copyBundleFile(bundle, bundleFile)) {
//                Log.info(this, "copy bundle fail :" + bundleFile);
//                BundleManager.getInstance().disableConfig();
//                lisenter.onBundleLoad(bundle.artifactId, false);
//                return;
//            }
            BundleClassLoader dexClassLoader = new BundleClassLoader(bundle, bundleFile.getAbsolutePath(), Constant.getPluginDir().getAbsolutePath(),  ClassLoader.getSystemClassLoader(), classLoader);
            bundle.classLoader = dexClassLoader;

            List<String> list = new LinkedList<>();
            list.add(bundleFile.getAbsolutePath());
            list.add(MedusaApplicationProxy.getInstance().getApplication().getApplicationInfo().sourceDir);

            if (bundle.dependencies != null) {
                for (String dep : bundle.dependencies) {
                    Bundle depBundle = BundleManager.getInstance().queryBundleByBundleName(dep);
                    if (depBundle != null) {
                        File depBundleFile = Constant.getBundleFile(depBundle);
                        list.add(depBundleFile.getAbsolutePath());
                        synchronized (depBundle.loaded) {
                            if (!depBundle.loaded) {
                                loadBundle(classLoader, depBundle, lisenter);
                            }
                        }
                    }
                }
            }

            BundleResource resources = ReflectUtil.addAsset(bundle, list);
            bundle.resources = resources;
            bundle.loaded = true;

            bundleInLoadingLocal.get().remove(bundle);
            medusaBundlesLocal.get().add(bundle);
            Log.info(this, "finish bundle load :" + bundle + "  use " + (System.currentTimeMillis() - time) + "ms");
            lisenter.onBundleLoad(bundle.artifactId, true);
            if (bundleInLoadingLocal.get().isEmpty()) {
                executeMedusaBundles();
            }
        } catch (Exception e) {
            e.printStackTrace();
            lisenter.onBundleLoad(bundle.artifactId, false);
        }
    }

    private void executeMedusaBundles() {
        LinkedList<Bundle> list = medusaBundlesLocal.get();
        for (Bundle bundle : list) {
            if (bundle.medusaBundles != null && !bundle.medusaBundles.isEmpty()) {
                Set<String> keys = bundle.medusaBundles.keySet();
                for (String key : keys) {
                    String clsName = bundle.medusaBundles.get(key);
                    try {
                        MedusaBundle medusaBundle = (MedusaBundle) bundle.classLoader.loadClass(clsName).newInstance();
                        medusaBundle.onCreate();
                        MedusaApplicationProxy.getInstance().initMedusaBundle(key, medusaBundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        list.clear();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
