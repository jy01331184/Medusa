package com.bundle1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.medusa.bundle.BundleActivity;

public class LoadingActivity extends BundleActivity {

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        };
        registerReceiver(receiver,new IntentFilter("com.medusa.finishload"));
        if(AppApplication.isFinishLoad()){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
