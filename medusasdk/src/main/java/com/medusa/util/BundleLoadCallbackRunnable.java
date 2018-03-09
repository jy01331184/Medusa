package com.medusa.util;

import android.content.Intent;

import com.medusa.application.LazyLoadActivity;
import com.medusa.application.MedusaAgent;
import com.medusa.application.MedusaApplicationProxy;
import com.medusa.bundle.Bundle;
import com.medusa.classloader.MedusaClassLoader;

import java.util.List;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleLoadCallbackRunnable extends BundleLoadRunnable {

    private String medusaBundleId;
    private String lazyWaitValue;
    private android.os.Bundle param;


    public BundleLoadCallbackRunnable(MedusaClassLoader classLoader, List<Bundle> bundles, String medusaBundleId, String lazyWaitValue, android.os.Bundle param) {
        super(classLoader, bundles, MedusaApplicationProxy.getInstance().getLisenter());
        this.medusaBundleId = medusaBundleId;
        this.lazyWaitValue = lazyWaitValue;
        this.param = param;
    }

    @Override
    public void run() {
        try {
            super.run();
            Intent intent = new Intent(LazyLoadActivity.ACTION);
            intent.putExtra(LazyLoadActivity.KEY_MEDUSA_BUNDLE_ID, medusaBundleId);
            intent.putExtra(LazyLoadActivity.KEY_MEDUSA_BUNDLE_PARAM, param);
            intent.putExtra(LazyLoadActivity.KEY, lazyWaitValue);
            MedusaAgent.getInstance().getApplication().sendStickyBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
