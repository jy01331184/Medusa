package com.medusa.bundle;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleResource extends Resources {

    private Bundle bundle;

    public BundleResource(AssetManager assets, DisplayMetrics metrics, Configuration config,Bundle bundle) {
        super(assets, metrics, config);
        this.bundle = bundle;
    }


    @Override
    public String toString() {
        return bundle.artifactId+"->"+super.toString();
    }
}
