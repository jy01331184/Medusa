package com.medusa.util;

import com.medusa.application.LazyLoadActivity;
import com.medusa.application.MedusaClassLoader;
import com.medusa.bundle.Bundle;

import java.util.List;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleLoadCallbackRunnable extends BundleLoadRunnable {

    private LazyLoadActivity activity;
    private String lazyClassName;


    public BundleLoadCallbackRunnable(MedusaClassLoader classLoader, List<Bundle> bundles,LazyLoadActivity activity,String name) {
        super(classLoader, bundles);
        this.activity = activity;
        this.lazyClassName = name;
    }

    @Override
    public void run() {
        try
        {
            Thread.sleep(3000);
            super.run();
            activity.finishFor(lazyClassName);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
