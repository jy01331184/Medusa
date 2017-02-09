package com.medusa.application;

import android.app.Application;
import android.content.Context;

import com.medusa.bundle.BundleManager;
import com.medusa.util.ReflectUtil;

/**
 * Created by tianyang on 16/8/11.
 */
public class MedusaApplication extends Application {

    private MedusaClassLoader classLoader;

    private static MedusaApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
        setUpClassLoader();
        initBundles();
    }

    private void setUpClassLoader() {
        classLoader = new MedusaClassLoader(this, getApplicationInfo().sourceDir, ClassLoader.getSystemClassLoader(), getClassLoader());
        ReflectUtil.setBoostClassLoader(this, classLoader);
        ReflectUtil.replaceInstrumentation();
    }

    private void initBundles() {
        if (!BundleManager.getInstance().load()) {
            BundleManager.getInstance().init();
        }

        BundleManager.getInstance().initBundleClassLoader(classLoader);
    }

    public MedusaClassLoader getMedClassLoader() {
        return classLoader;
    }

    public static MedusaApplication getInstance() {
        return instance;
    }
}
