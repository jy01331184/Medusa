package com.bundle1;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.medusa.application.MedusaAgent;
import com.medusa.application.MedusaApplicationProxy;

public class MainActivity extends BundleCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MedusaApplicationProxy.getInstance().startBundle("1802", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MedusaAgent.getInstance().startBundleAsync("1802", null);
//                    Intent intent = new Intent();
//                    intent.setClassName(MainActivity.this, "com.bundle2.Bundle2Activity");
//                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MedusaApplicationProxy.getInstance().startBundle("1802", null);
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
                    Cursor cursor = getContentResolver().query(Uri.parse("content://com.bundle2/contact"), null, null, null, null);
                    while (cursor.moveToNext()) {
                        System.out.println("loop:" + cursor.getString(0) + "-" + cursor.getString(1));
                    }
//                    Intent intent = new Intent();
//                    intent.setClassName(MainActivity.this, "com.bundle1.Bundle1Service");
//
//                    startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
