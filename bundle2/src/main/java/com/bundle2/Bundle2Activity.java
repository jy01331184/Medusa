package com.bundle2;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.medusa.bundle.BundleActivity;

public class Bundle2Activity extends BundleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bundle2);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //System.out.println("remote 1");
                    //Toast.makeText(getApplicationContext(),getString(R.string.app_name),Toast.LENGTH_LONG).show();
                    //startActivity(new Intent(getApplicationContext(), Bundle3Activity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        BitmapFactory.Options options = new BitmapFactory.Options();


    }
}
