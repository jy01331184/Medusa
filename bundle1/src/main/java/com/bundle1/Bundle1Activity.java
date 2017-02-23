package com.bundle1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bundle2.Bundle2Activity;
import com.medusa.bundle.BundleActivity;

public class Bundle1Activity extends BundleActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bundle1);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try
            {
                System.out.println("ben simons");
                startActivity(new Intent(Bundle1Activity.this,Bundle2Activity.class));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            }
        });


    }
}
