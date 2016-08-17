package com.medusa.bundle;

import android.app.Activity;
import android.content.Context;

/**
 * Created by tianyang on 16/8/12.
 */
public class BundleActivity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        BundleUtil.replaceResource(newBase,getClass().getName());
    }
}
