package com.medusa.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.medusa.bundle.BundleActivity;
import com.medusa.sdk.R;
import com.medusa.util.Log;


/**
 * Created by tianyang on 16/8/16.
 */
public class LazyLoadActivity extends BundleActivity {

    private static final int MIN_STAY_TIME = 1000;
    public static final String KEY = "LazyLoadActivity.DISPOSE";
    public static final String KEY_MEDUSA_BUNDLE_ID = "LazyLoadActivity.MEDUSA_BUNDLE";
    public static final String KEY_MEDUSA_BUNDLE_PARAM = "LazyLoadActivity.MEDUSA_PARAM";
    public static final String ACTION = "com.medusa.lazy.dispose";
    private String waitValue;
    private BroadcastReceiver receiver;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.log("LazyLoadActivity", "LazyLoadActivity onCreate");
        setContentView(R.layout.lazy);
        startTime = System.currentTimeMillis();
        waitValue = getIntent().getStringExtra(KEY);
        if (TextUtils.isEmpty(waitValue)) {
            throw new RuntimeException("LazyLoadActivity must have a KEY");
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String val = intent.getStringExtra(KEY);
                if (TextUtils.equals(waitValue, val)) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - startTime >= MIN_STAY_TIME) {
                        doExecute(intent);
                    } else {
                        getWindow().getDecorView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doExecute(intent);
                            }
                        }, MIN_STAY_TIME - nowTime + startTime);
                    }
                }
            }
        };


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void doExecute(Intent intent) {
        finish();
        String medusaBundleId = intent.getStringExtra(KEY_MEDUSA_BUNDLE_ID);
        Bundle param = intent.getBundleExtra(KEY_MEDUSA_BUNDLE_PARAM);
        MedusaAgent.getInstance().startBundle(medusaBundleId, param);
    }
}
