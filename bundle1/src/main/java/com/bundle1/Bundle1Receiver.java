package com.bundle1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tianyang on 16/8/17.
 */
public class Bundle1Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("receive in bundle1");
    }
}
