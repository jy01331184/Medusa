package com.bundle1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by tianyang on 16/8/17.
 */
public class Bundle1Service extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("start bundle1 service");
        return super.onStartCommand(intent, START_STICKY, startId);
    }
}
