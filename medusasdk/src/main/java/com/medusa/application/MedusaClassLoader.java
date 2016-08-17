package com.medusa.application;

import android.content.Context;

import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleExecutor;
import com.medusa.bundle.BundleManager;
import com.medusa.util.Constant;
import com.medusa.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.PathClassLoader;

/**
 * Created by tianyang on 16/8/11.
 */
public class MedusaClassLoader extends PathClassLoader {

    private List<ClassLoader> loaders = new ArrayList<>();
    private Map<String,ClassLoader> cache = new HashMap<>();
    private ClassLoader originClassLoader;

    public MedusaClassLoader(Context context, String dexPath, ClassLoader parent,ClassLoader origin) {
        super(dexPath,context.getApplicationInfo().nativeLibraryDir, parent);
        this.originClassLoader = origin;
    }

    public ClassLoader getOriginClassLoader() {
        return originClassLoader;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(cache.containsKey(name))
        {
            //Log.log("MedusaClassLoader","load class "+name +" from cache");
            Class<?> cls = cache.get(name).loadClass(name);
            if(cls == null)
                cache.remove(name);
            else
                return cls;
        }
        Bundle bundle = BundleManager.getInstance().queryBundleName(name);
        if(bundle != null )
        {
            if(bundle.loaded && bundle.classLoader != null)
            {
                Class<?> cls = bundle.classLoader.loadClass(name);
                if(cls != null)
                {
                    //Log.log("MedusaClassLoader","load class "+name +" from bundle "+bundle.artifactId);
                    cache.put(name,bundle.classLoader);
                    return cls;
                }
            }
            else if(bundle.priority <= Constant.PRIORITY_LAZY){
                BundleExecutor.getInstance().loadBundle(this,bundle);
                if(bundle.loaded && bundle.classLoader != null)
                {
                    Class<?> cls = bundle.classLoader.loadClass(name);
                    if(cls != null)
                    {
                        Log.log("MedusaClassLoader","hard load class "+name +" from bundle "+bundle.artifactId);
                        cache.put(name,bundle.classLoader);
                        return cls;
                    }
                }
            }
        }

        for (ClassLoader cl : loaders) {
            Class<?> cls = cl.loadClass(name);
            if(cls != null)
            {
//                Log.log("MedusaClassLoader","load class "+name +" from loop");
                cache.put(name,cl);
                return cls;
            }
        }

        return super.findClass(name);
    }

    public void addClassLoader(ClassLoader classLoader)
    {
        loaders.add(classLoader);
    }

}
