package com.bundle1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.medusa.bundle.BundleUtil;

/**
 * Created by tianyang on 18/2/28.
 */
public class BundleCompatActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        BundleUtil.replaceResource(newBase, this.getClass().getName());
    }
}
