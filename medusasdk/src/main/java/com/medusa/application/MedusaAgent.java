package com.medusa.application;

import android.content.Context;

/**
 * Created by tianyang on 18/3/1.
 */
public abstract class MedusaAgent {


    public static MedusaAgent getInstance() {
        MedusaApplicationProxy instance = MedusaApplicationProxy.getInstance();
        return instance;
    }

    public abstract Context getApplication();

    public abstract void startBundle(String id, android.os.Bundle param);

    public abstract void startBundleAsync(String id, android.os.Bundle param);

}
