package com.bundle1;

import android.os.Bundle;

import com.medusa.application.MedusaBundle;

/**
 * Created by tianyang on 17/5/8.
 */
public class MB1  implements MedusaBundle{


    @Override
    public void onCreate() {
        System.out.println("MB1 oncreate");
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart(Bundle bundle) {
        System.out.println("MB1 onstart");
    }
}
