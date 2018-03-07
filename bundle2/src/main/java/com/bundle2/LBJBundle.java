package com.bundle2;

import android.content.Intent;
import android.os.Bundle;

import com.medusa.application.MedusaAgent;
import com.medusa.application.MedusaBundle;

/**
 * Created by tianyang on 18/3/1.
 */
public class LBJBundle implements MedusaBundle {

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart(Bundle bundle) {
        Intent intent = new Intent(MedusaAgent.getInstance().getApplication(), Bundle2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MedusaAgent.getInstance().getApplication().startActivity(intent);
    }
}
