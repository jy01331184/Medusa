package com.medusa.util;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.medusa.application.MedusaApplication;
import com.medusa.application.MedusaInstrumentation;
import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleResource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by tianyang on 16/8/11.
 */
public class ReflectUtil {

    public static void setBoostClassLoader(Application application,ClassLoader classLoader)
    {
        Object loadedApk = getLoadedApk(application.getBaseContext());

        Field classloaderField = null;
        try {
            classloaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
            classloaderField.setAccessible(true);
            classloaderField.set(loadedApk,classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getActivityThread(Context context)
    {
        try {
            return context.getClassLoader().loadClass("android.app.ActivityThread").getDeclaredMethod("currentActivityThread", new Class[]{}).invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getLoadedApk(Context contextImpl){
        try{
            Field mPackageInfoField = contextImpl.getClass().getDeclaredField("mPackageInfo");
            mPackageInfoField.setAccessible(true);
            Object loadedApk = mPackageInfoField.get(contextImpl);
            return loadedApk;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static BundleResource addAsset(Bundle bundle,String... dirs)
    {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method method = AssetManager.class.getDeclaredMethod("addAssetPath", new Class<?>[]{String.class});
            method.setAccessible(true);

            for (String dir : dirs) {
                method.invoke(assetManager, dir);
            }

            BundleResource resources = new BundleResource(assetManager, MedusaApplication.getInstance().getResources().getDisplayMetrics(), MedusaApplication.getInstance().getResources().getConfiguration(),bundle);
            return resources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void replaceResource(Context contextimpl,Resources resources)
    {
        try
        {
            Field resField = contextimpl.getClass().getDeclaredField("mResources");

            resField.setAccessible(true);

            resField.set(contextimpl,resources);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void replaceInstrumentation()
    {
        try
        {
            Object activityThread = MedusaApplication.getInstance().getClassLoader().loadClass("android.app.ActivityThread").getDeclaredMethod("currentActivityThread", new Class[]{}).invoke(null);

            Field insField = activityThread.getClass().getDeclaredField("mInstrumentation");
            insField.setAccessible(true);
            Instrumentation instruments = (Instrumentation) insField.get(activityThread);
            MedusaInstrumentation mins = new MedusaInstrumentation(instruments);
            insField.set(activityThread,mins);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
