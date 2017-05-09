package com.medusa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.medusa.application.MedusaApplicationProxy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MedusaApplicationProxy.getInstance().startBundle("1747",null);
//                    Intent intent = new Intent();
//                    intent.setClassName(MainActivity.this, "com.bundle1.Bundle1Activity");
//                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName(MainActivity.this, "com.bundle2.Bundle2Activity");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MedusaApplicationProxy.getInstance().startBundle("1802",null);
//                    Intent intent = new Intent();
//                    intent.setClassName(MainActivity.this, "medusa.com.bundle3.Bundle3Activity");
//
//                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName(MainActivity.this, "com.bundle1.Bundle1Service");

                    startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
