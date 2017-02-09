package com.bundle1;

import android.os.Bundle;
import android.view.View;

import com.medusa.bundle.BundleActivity;

import example.A;

public class Bundle1Activity extends BundleActivity {
    A a = new A();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bundle1);

        
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try
            {
                System.out.println("start le 1802 1747");
                //startActivity(new Intent(Bundle1Activity.this,Bundle2Activity.class));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            }
        });
    }
}
