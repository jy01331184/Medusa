package com.medusa.bundle;

import com.medusa.application.LazyLoadActivity;
import com.medusa.application.MedusaApplication;
import com.medusa.application.MedusaClassLoader;
import com.medusa.util.BundleComparetor;
import com.medusa.util.BundleLoadCallbackRunnable;
import com.medusa.util.BundleLoadRunnable;
import com.medusa.util.Constant;
import com.medusa.util.Log;
import com.medusa.util.ReflectUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleExecutor extends ThreadPoolExecutor{

    private static BundleExecutor instance;

    public BundleExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public static BundleExecutor getInstance() {
        if(instance == null)
            instance = new BundleExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10));
        return instance;
    }

    private List<Bundle> remoteBundles = new ArrayList<>();

    public void deleteBundle(Bundle bundle)
    {
        remoteBundles.add(bundle);
    }

    public synchronized void loadBundle(Bundle bundle, LazyLoadActivity activity, String lazyClassName)
    {
        List<Bundle> tasks = new ArrayList<>();
        tasks.add(bundle);
        execute(new BundleLoadCallbackRunnable(MedusaApplication.getInstance().getMedClassLoader(),tasks,activity,lazyClassName));
    }

    public synchronized void loadBundle(List<Bundle> bundles, MedusaClassLoader classLoader)
    {
        List<Bundle> tasks = new ArrayList<>();

        for (Bundle bundle : bundles) {
            if(!bundle.loaded && bundle.priority == Constant.PRIORITY_MAX)
            {
                loadBundle(classLoader,bundle);
            }
            else if(!bundle.loaded && bundle.priority < Constant.PRIORITY_LAZY)
            {
                tasks.add(bundle);
            }
        }
        Collections.sort(tasks,new BundleComparetor());
        execute(new BundleLoadRunnable(classLoader,tasks));
    }

    public void commit()
    {
        for (int i = remoteBundles.size()-1; i >= 0; i--) {
            BundleManager.getInstance().removeBundle(remoteBundles.remove(i));
        }
    }

    public void loadBundle(MedusaClassLoader classLoader,Bundle bundle)
    {
        File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
        BundleClassLoader dexClassLoader = new BundleClassLoader(bundle,bundleFile.getAbsolutePath(), Constant.getPluginDir().getAbsolutePath(), bundleFile.getAbsolutePath(), classLoader.getOriginClassLoader());
        classLoader.addClassLoader(dexClassLoader);
        bundle.classLoader = dexClassLoader;

        List<String> list = new ArrayList<>();
        list.add(MedusaApplication.getInstance().getApplicationInfo().sourceDir);
        list.add(bundleFile.getAbsolutePath());

        for (String dep : bundle.dependencies) {
            Bundle depBundle = BundleManager.getInstance().queryBundleByBundleName(dep);
            if(depBundle != null)
            {
                File depBundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(depBundle));
                list.add(depBundleFile.getAbsolutePath());
            }
        }

        BundleResource resources = ReflectUtil.addAsset(bundle,list.toArray(new String[]{}));
        bundle.resources = resources;
        bundle.loaded = true;
        Log.log(this,"finish bundle load :"+bundle);

        if(bundle.dependencies != null)
        {
            for (String dep : bundle.dependencies) {
                Bundle dependencyBundle = BundleManager.getInstance().queryBundleByBundleName(dep);
                if(dependencyBundle != null && !dependencyBundle.loaded) //处理依赖
                {
                    loadBundle(classLoader,dependencyBundle);
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
