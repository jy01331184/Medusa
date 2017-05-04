package com.bundle1;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;

import com.medusa.bundle.BundleActivity;

import org.xmlpull.v1.XmlPullParser;

public class Bundle1Activity extends BundleActivity {

    Resources.Theme theme;

    int themeId ;

//    @Override
//    public void setTheme(int resid) {
//        this.themeId = resid;
//        if(themeId != 0)
//            theme.applyStyle(themeId,true);
//        super.setTheme(resid);
//    }

//    @Override
//    public Resources.Theme getTheme() {
//        if(theme != null)
//            return theme;
//
//        if (theme == null && getResources() instanceof BundleResource) {
//            theme = getResources().newTheme();
//
//
//
//            if(themeId != 0)
//                theme.applyStyle(themeId,true);
//            System.out.println("rrrr:"+theme);
//            theme.setTo(getApplicationContext().getTheme());
//            Resources r = getResources();
//
//            return theme;
//        }else{
//            System.out.println("temp le:"+getResources());
//        }
//
//        return super.getTheme();
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bundle1);



        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                System.out.println(medusa.com.bundle3.R.drawable.kk+":"+0x1d020000);
//                System.out.println(getResources().getDrawable(0x1d020000).getIntrinsicWidth());
                    LayoutInflater lay = LayoutInflater.from(Bundle1Activity.this);
//                    System.out.println( android.R.string.search_go);
//                    System.out.println(medusa.com.bundle3.R.string.bundle+":"+0x1d050000);

//                    TypedArray type = obtainStyledAttributes(new int[]{});
                    TypedValue typedValue = new TypedValue();


                    XmlResourceParser parser = getResources().getLayout(R.layout.activity_bundle2);

                    AttributeSet set = Xml.asAttributeSet(parser);

                    int type = parser.getEventType();

                    while (type != XmlPullParser.END_DOCUMENT){
                        switch (type){
                            case XmlPullParser.START_TAG:
                                System.out.println("start:"+parser.getName()+"-");
                                for (int i = 0; i < parser.getAttributeCount(); i++) {
                                    System.out.println(parser.getAttributeName(i)+":"+parser.getAttributeValue(i));
                                }


                                break;
                        }


                        type = parser.next();
                    }


                    //lay.inflate(R.layout.activity_bundle2, null);
                    //System.out.println(getTheme().getResources());
                    //View view = LayoutInflater.from(Bundle1Activity.this).inflate(R.layout.activity_bundle2, null);
                    //System.out.println(view);
//                System.out.println(getResources()+"-----"+findViewById(R.id.txt).getContext()+"-----"+findViewById(R.id.txt).getResources());
//                Resources.Theme th = getTheme();
//
//
//                Class cls = LayoutInflater.class;
//
//                Field[] fs = cls.getDeclaredFields();
//                for (Field f : fs) {
//                    f.setAccessible(true);
//                    System.out.println(f.getName()+"->"+f.get(lay));
//                }
//
//                System.out.println();
//                System.out.println();
//                Method[] ms = cls.getDeclaredMethods();
//
//                for (Method m : ms) {
//                    m.setAccessible(true);
//                    System.out.println(m.getName());
//                }
//                startActivity(new Intent(Bundle1Activity.this,Bundle2Activity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
