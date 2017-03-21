package com.medusa.util;

import com.medusa.classloader.MedusaClassLoader;
import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleExecutor;

import java.util.List;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleLoadRunnable implements Runnable {

    private MedusaClassLoader classLoader;
    private List<Bundle> bundles;

    public BundleLoadRunnable(MedusaClassLoader classLoader,List<Bundle> bundles) {
        this.classLoader = classLoader;
        this.bundles = bundles;
    }

    @Override
    public void run() {
        for (Bundle bundle : bundles) {
            try
            {
                synchronized (bundle.loaded){
                    if(!bundle.loaded)
                    {
                        BundleExecutor.getInstance().loadBundle(classLoader,bundle);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
