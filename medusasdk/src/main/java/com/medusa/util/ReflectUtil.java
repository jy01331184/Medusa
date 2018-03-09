package com.medusa.util;

import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.medusa.application.MedusaApplicationProxy;
import com.medusa.application.MedusaInstrumentation;
import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleResource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by tianyang on 16/8/11.
 */
public class ReflectUtil {

    private static Method addAssetPath;

    static {
        try {
            addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", new Class<?>[]{String.class});
            addAssetPath.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static boolean setClassLoaderParent(ClassLoader classLoader, ClassLoader parent) {
        try {
            Field parentField = ClassLoader.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            parentField.set(classLoader, parent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean setBoostClassLoader(ContextWrapper context, ClassLoader classLoader) {
        Object loadedApk = getLoadedApk(context.getBaseContext());

        Field classloaderField = null;
        try {
            classloaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
            classloaderField.setAccessible(true);
            classloaderField.set(loadedApk, classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static Object getActivityThread(Context context) {
        try {
            return context.getClassLoader().loadClass("android.app.ActivityThread").getDeclaredMethod("currentActivityThread", new Class[]{}).invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getLoadedApk(Context contextImpl) {
        try {
            Field mPackageInfoField = contextImpl.getClass().getDeclaredField("mPackageInfo");
            mPackageInfoField.setAccessible(true);
            Object loadedApk = mPackageInfoField.get(contextImpl);
            return loadedApk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BundleResource addAsset(Bundle bundle, List<String> dirs) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();

            for (String dir : dirs) {
                Object obj = addAssetPath.invoke(assetManager, dir);
                Log.info("BundleResource", bundle.artifactId + " add path:" + dir + "  res:" + obj);
            }

            BundleResource resources = new BundleResource(assetManager, MedusaApplicationProxy.getInstance().getApplication().getResources().getDisplayMetrics(), MedusaApplicationProxy.getInstance().getApplication().getResources().getConfiguration(), bundle);
            return resources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void replaceResource(Context contextimpl, Resources resources) {
        try {
            Field resField = contextimpl.getClass().getDeclaredField("mResources");

            resField.setAccessible(true);

            resField.set(contextimpl, resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void replaceInstrumentation() {
        try {
            Object activityThread = MedusaApplicationProxy.getInstance().getApplication().getClassLoader().loadClass("android.app.ActivityThread").getDeclaredMethod("currentActivityThread", new Class[]{}).invoke(null);

            Field insField = activityThread.getClass().getDeclaredField("mInstrumentation");
            insField.setAccessible(true);
            Instrumentation instruments = (Instrumentation) insField.get(activityThread);
            MedusaInstrumentation mins = new MedusaInstrumentation(instruments);
            insField.set(activityThread, mins);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
