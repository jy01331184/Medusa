package com.medusa.bundle;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return super.obtainAttributes(set, attrs);
    }

    @Override
    public String toString() {
        return bundle.artifactId+"->"+super.toString();
    }
}
