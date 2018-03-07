package com.medusa.application;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.medusa.bundle.Bundle;

import java.lang.reflect.Method;

/**
 * Created by tianyang on 16/8/15.
 */
public class MedusaInstrumentation extends Instrumentation {



    private Instrumentation mBase;
    private Method execStartActivity;

    public MedusaInstrumentation(Instrumentation mBase) {
        this.mBase = mBase;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        try
        {
            return super.newActivity(cl, className, intent);
        }
        catch (ClassNotFoundException e)
        {
//            com.medusa.bundle.Bundle bundle = BundleManager.getInstance().queryBundleName(className);
//            if(bundle!= null && !bundle.loaded)
//            {
//                //Toast.makeText(MedusaApplication.getInstance(),"懒加载bundle 模拟 3s后加载",Toast.LENGTH_LONG).show();
//                String lazyClassName = className;
//                className = LazyLoadActivity.class.getName();
//                Activity activity = super.newActivity(cl,className,intent);
//                BundleExecutor.getInstance().loadBundle(bundle, (LazyLoadActivity) activity,lazyClassName);
//                return activity;
//            }
            e.printStackTrace();
        }
        return null;
    }

//    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
//        try
//        {
//            if(execStartActivity == null)
//                initMethod();
////            Log.log(this,"hook startActivity :"+target);
//
//            return (ActivityResult) execStartActivity.invoke(mBase, who, contextThread, token, target, intent, requestCode, options);
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException("do not support in Instumentation");
//        }
//    }


    private void initMethod() throws Exception
    {
        execStartActivity = Instrumentation.class.getDeclaredMethod("execStartActivity",
                Context.class, IBinder.class, IBinder.class, Activity.class,
                Intent.class, int.class, Bundle.class);
        execStartActivity.setAccessible(true);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
