package com.medusa.classloader;

import android.content.Context;

import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.PathClassLoader;

/**
 * Created by tianyang on 16/8/11.
 */
public class MedusaClassLoader extends PathClassLoader {

    private List<BundleClassLoader> loaders = new ArrayList<>();
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

        //System.out.println("m find class:"+name);

        if(cache.containsKey(name))
        {
            //Log.log("MedusaClassLoader","load class "+name +" from cache");
            Class<?> cls = cache.get(name).loadClass(name);
            if(cls == null)
                cache.remove(name);
            else{
                //System.out.println("cache hit in findclass:"+name);
                return cls;
            }
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
                    return cls;
                }
            }
            else
            {
//                synchronized (bundle.loaded){
//                    if( (!bundle.loaded || bundle.classLoader == null)){
//                        BundleExecutor.getInstance().loadBundle(this,bundle);
//                        if(bundle.classLoader != null && bundle.loaded){
//                            Class<?> cls = bundle.classLoader.loadClass(name);
//                            if(cls != null)
//                            {
//                                //Log.log("MedusaClassLoader","hard load class "+name +" from bundle "+bundle.artifactId);
//                                return cls;
//                            }
//                        }
//                    }else{
//                        Class<?> cls = bundle.classLoader.loadClass(name);
//                        if(cls != null)
//                        {
//                            //Log.log("MedusaClassLoader","load class "+name +" from bundle "+bundle.artifactId);
//                            return cls;
//                        }
//                    }
//                }
            }
        }

        return super.findClass(name);
    }

    public void addClassLoader(BundleClassLoader classLoader)
    {
        loaders.add(classLoader);
    }

    private Class findInCache(String className){

        if(cache.containsKey(className)){
            try {
                Class<?> cls = cache.get(className).loadClass(className);
                if(cls == null)
                    cache.remove(className);
                return cls;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void addInCache(String className,ClassLoader classLoader){
        cache.put(className,classLoader);
    }

    Class loadClassFromDependency(Bundle bundle,String className){
        Class result = null;
        Class findInCache = findInCache(className);
        if( findInCache != null){
            //System.out.println("cache hit in loadClassFromDependency:"+className);
            return findInCache;
        }

        Bundle targetBundle = BundleManager.getInstance().queryBundleName(className);

        if(targetBundle != null && targetBundle.loaded)
        {
            Class<?> cls = targetBundle.classLoader.loadClassDirectly(className);
            if(cls != null){
                //System.out.println("load "+className+" in from dep:"+dependency.artifactId);
                addInCache(className,targetBundle.classLoader);
                return cls;
            }

        }

        return  result;
    }


}
