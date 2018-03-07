package com.medusa.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleExecutor;
import com.medusa.bundle.BundleManager;
import com.medusa.classloader.MedusaClassLoader;
import com.medusa.util.Log;
import com.medusa.util.ReflectUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dalvik.system.PathClassLoader;

/**
 * Created by tianyang on 17/5/8.
 */
public class MedusaApplicationProxy extends MedusaAgent {

    private static MedusaApplicationProxy instance;

    private Application application;
    private MedusaLisenter lisenter;
    private MedusaClassLoader classLoader;

    private Map<String, MedusaBundleConfig> medusaBundles = new HashMap<>();


    public static MedusaApplicationProxy getInstance() {
        if (instance == null)
            instance = new MedusaApplicationProxy();
        return instance;
    }

    public void attachContext(Application application, MedusaLisenter lisenter) {
        this.application = application;
        this.lisenter = lisenter;
        Log.log("MedusaApplicationProxy", "attachContext");
        if (setUpClassLoader()) {
            initBundles();
        } else {
            lisenter.onMedusaLoad(MedusaLisenter.MedusaLoadState.FAIL);
        }
    }

    private boolean setUpClassLoader() {
        PathClassLoader pathClassLoader = (PathClassLoader) MedusaApplicationProxy.class.getClassLoader();
        ClassLoader bootClassLoader = pathClassLoader.getParent();
        classLoader = new MedusaClassLoader(bootClassLoader, pathClassLoader, BundleManager.getInstance(), BundleExecutor.getInstance());

        if (ReflectUtil.setClassLoaderParent(pathClassLoader, classLoader)) {
            ReflectUtil.replaceInstrumentation();
            return true;
        }
        return false;
    }

    public Context getApplication() {
        return application;
    }

    public MedusaLisenter getLisenter() {
        return lisenter == null ? MedusaLisenter.NULL : lisenter;
    }

    public void startBundle(String id, android.os.Bundle param) {
        MedusaBundleConfig medusaBundleConfig = medusaBundles.get(id);
        if (medusaBundleConfig != null) {
            if (medusaBundleConfig.medusaBundle != null) {
                medusaBundleConfig.medusaBundle.onStart(param);
            } else {
                synchronized (medusaBundleConfig.bundle.loaded) {
                    if (!medusaBundleConfig.bundle.loaded) {
                        BundleExecutor.getInstance().loadBundle(classLoader, medusaBundleConfig.bundle, lisenter);

                        if (medusaBundleConfig.medusaBundle == null) {
                            throw new RuntimeException("still no medusaBundle handle ID " + id);
                        } else {
                            medusaBundleConfig.medusaBundle.onStart(param);
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("no medusaBundle handle ID " + id);
        }
    }

    @Override
    public void startBundleAsync(String id, android.os.Bundle param) {
        MedusaBundleConfig medusaBundleConfig = medusaBundles.get(id);
        if (medusaBundleConfig != null) {
            if (medusaBundleConfig.medusaBundle != null) {
                medusaBundleConfig.medusaBundle.onStart(param);
            } else {
                if (!medusaBundleConfig.bundle.loaded) {
                    Intent intent = new Intent(getApplication(), LazyLoadActivity.class);
                    String val = String.valueOf(System.currentTimeMillis());
                    intent.putExtra(LazyLoadActivity.KEY, val);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);
                    BundleExecutor.getInstance().loadBundle(classLoader, medusaBundleConfig.bundle, id, val, param);
                } else {
                    throw new RuntimeException("no medusaBundle handle ID " + id);
                }
            }
        } else {
            throw new RuntimeException("no medusaBundle handle ID " + id);
        }
    }

    private void initBundles() {
        if (!BundleManager.getInstance().load()) {
            BundleManager.getInstance().init();
        }
        if (BundleManager.getInstance().initBundleUpdate(lisenter)) {
            Map<String, Bundle> bundles = BundleManager.getInstance().getBundleConfig().bundles;
            if (bundles != null) {
                Collection<Bundle> values = bundles.values();
                for (Bundle bundle : values) {
                    Map<String, String> tempBundles = bundle.medusaBundles;
                    if (tempBundles != null) {
                        Set<String> keys = tempBundles.keySet();
                        for (String key : keys) {
                            medusaBundles.put(key, new MedusaBundleConfig(bundle, tempBundles.get(key)));
                        }
                    }
                }
            }

            BundleManager.getInstance().initBundleClassLoader(classLoader, lisenter);
        }
    }

    private static class MedusaBundleConfig {
        public com.medusa.bundle.Bundle bundle;
        public String medusaBundleClassName;
        public MedusaBundle medusaBundle;

        public MedusaBundleConfig(com.medusa.bundle.Bundle bundle, String clsName) {
            this.bundle = bundle;
            this.medusaBundleClassName = clsName;
        }
    }

    public void initMedusaBundle(String key, MedusaBundle medusaBundle) {
        medusaBundles.get(key).medusaBundle = medusaBundle;
    }
}
