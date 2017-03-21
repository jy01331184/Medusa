package com.medusa.classloader;

import com.medusa.bundle.Bundle;

import dalvik.system.DexClassLoader;

/**
 * Created by tianyang on 16/8/11.
 */
public class BundleClassLoader extends DexClassLoader {

    private Bundle bundle;
    private MedusaClassLoader medClassLoader;

    public BundleClassLoader(Bundle bundle, String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, MedusaClassLoader medClassLoader) {
        super(dexPath, optimizedDirectory,libraryPath, parent);
        this.bundle = bundle;
        this.medClassLoader = medClassLoader;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
//        System.out.println(bundle.artifactId+"  b load class:"+className);
        Class cls = loadClassDirectly(className);
        if(cls != null){
            medClassLoader.addInCache(className,this);
        }
        else {
            cls = medClassLoader.loadClassFromDependency(bundle,className);
        }

//        if(cls == null)
//            throw new ClassNotFoundException(className+" not find in "+this.toString());
//        Class result = loadClassSuper(className);
//        if(result == null)
//            result = loadClassByDependency(className);
//
//        return  result;
        return cls;
    }

    Class<?> loadClassDirectly(String className)
    {
        try
        {
            return super.loadClass(className);
        }
        catch (Exception e)
        {}
        return null;
    }
}
