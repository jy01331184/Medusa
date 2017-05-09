package com.medusa.bundle;

import com.medusa.application.LazyLoadActivity;
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

    public void deleteBundle(Bundle bundle) {
        removeBundles.add(bundle);
    }

    public synchronized void loadBundle(final Bundle bundle, final LazyLoadActivity activity, final String lazyClassName) {
        List<Bundle> tasks = new ArrayList<>();
        tasks.add(bundle);
        execute(new BundleLoadCallbackRunnable(MedusaApplicationProxy.getInstance().getMedClassLoader(), tasks, activity, lazyClassName));
    }

    public synchronized void loadBundle(Collection<Bundle> bundles, MedusaClassLoader classLoader,MedusaLisenter lisenter) {
        List<Bundle> tasks = new ArrayList<>();

        for (Bundle bundle : bundles) {
            if (!bundle.loaded && bundle.priority == Constant.PRIORITY_MAX) {
                loadBundle(classLoader, bundle,lisenter);
            } else if (!bundle.loaded && bundle.priority < Constant.PRIORITY_LAZY) {
                tasks.add(bundle);
            }
        }
        Collections.sort(tasks, new BundleComparetor());
        execute(new BundleLoadRunnable(classLoader, tasks,lisenter));
    }

    public void commit() {
        for (int i = removeBundles.size() - 1; i >= 0; i--) {
            BundleManager.getInstance().removeBundle(removeBundles.remove(i));
        }
    }

    public void loadBundle(MedusaClassLoader classLoader, Bundle bundle, MedusaLisenter lisenter) {
        try {
            long time = System.currentTimeMillis();
            File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
            if (!BundleUtil.copyBundleFile(bundle, bundleFile)) {
                Log.log(this, "copy bundle fail :" +bundleFile);
                BundleManager.getInstance().disableConfig();
                lisenter.onBundleLoad(bundle.artifactId,false);
                return;
            }
            BundleClassLoader dexClassLoader = new BundleClassLoader(bundle, bundleFile.getAbsolutePath(), Constant.getPluginDir().getAbsolutePath(), bundleFile.getAbsolutePath(), classLoader.getOriginClassLoader(),classLoader);
            classLoader.addClassLoader(dexClassLoader);
            bundle.classLoader = dexClassLoader;

            List<String> list = new ArrayList<>();
            list.add(MedusaApplicationProxy.getInstance().getApplication().getApplicationInfo().sourceDir);
            list.add(bundleFile.getAbsolutePath());

            if (bundle.dependencies != null) {
                for (String dep : bundle.dependencies) {
                    Bundle depBundle = BundleManager.getInstance().queryBundleByBundleName(dep);
                    if (depBundle != null) {
                        File depBundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(depBundle));
                        list.add(depBundleFile.getAbsolutePath());
                        synchronized (depBundle.loaded) {
                            if (!depBundle.loaded) {
                                loadBundle(classLoader, depBundle,lisenter);
                            }
                        }
                    }
                }
            }

            BundleResource resources = ReflectUtil.addAsset(bundle, list.toArray(new String[]{}));
            bundle.resources = resources;
            bundle.loaded = true;
            if(bundle.medusaBundles != null && !bundle.medusaBundles.isEmpty()){
                Set<String> keys = bundle.medusaBundles.keySet();
                for (String key : keys) {
                    String clsName = bundle.medusaBundles.get(key);
                    try {
                        MedusaBundle medusaBundle = (MedusaBundle) bundle.classLoader.loadClass(clsName).newInstance();
                        medusaBundle.onCreate();
                        MedusaApplicationProxy.getInstance().initMedusaBundle(key,medusaBundle);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            Log.log(this, "finish bundle load :" + bundle + "  use " + (System.currentTimeMillis() - time) + "ms");
            lisenter.onBundleLoad(bundle.artifactId,true);
        } catch (Exception e) {
            e.printStackTrace();
            lisenter.onBundleLoad(bundle.artifactId,false);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
