package com.bundle2;

import android.os.Bundle;
import android.view.View;

import com.medusa.application.MedusaAgent;
import com.medusa.bundle.BundleActivity;

public class Bundle2Activity extends BundleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bundle2);
        System.out.println("hello bundle2");
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MedusaAgent.getInstance().startBundle("1747", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
