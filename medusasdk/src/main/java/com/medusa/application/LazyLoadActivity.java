package com.medusa.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.medusa.sdk.R;


/**
 * Created by tianyang on 16/8/16.
 */
public class LazyLoadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lazy);

    }

    public void finishFor(String name)
    {
        if(isFinishing())
            return;
        try
        {
            Intent intent = new Intent();
            intent.setClassName(this,name);
            startActivity(intent);
            finish();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
