package com.medusa.application;

import android.app.Application;
import android.content.Context;

import com.medusa.bundle.BundleManager;
import com.medusa.classloader.MedusaClassLoader;
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
        //// TODO: 17/3/7 添加替换classloader失败 全局禁用bundle
        classLoader = new MedusaClassLoader(this, getApplicationInfo().sourceDir, getClassLoader(), getClassLoader());
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
