package com.medusa;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.medusa.application.MedusaApplicationProxy;
import com.medusa.application.MedusaLisenter;

/**
 * Created by tianyang on 17/5/8.
 */
public class AppApplication extends Application {



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MedusaApplicationProxy.getInstance().attachContext(this, new MedusaLisenter() {
            @Override
            public void onMedusaLoad(MedusaLoadState medusaLoadState) {
                if(medusaLoadState.progress == 1){
                    setFinishLoad(true);
                    sendBroadcast(new Intent("com.medusa.finishload"));
                }
            }

            @Override
            public void onBundleLoad(String s, boolean b) {
                //System.out.println("load bundle :"+s+" - "+b);
            }
        });
    }

    private static boolean finishLoad = false;

    public synchronized static void setFinishLoad(boolean finishLoad) {
        AppApplication.finishLoad = finishLoad;
    }

    public synchronized static boolean isFinishLoad() {
        return finishLoad;
    }
}
