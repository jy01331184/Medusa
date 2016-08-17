package com.medusa.bundle;

import dalvik.system.DexClassLoader;

/**
 * Created by tianyang on 16/8/11.
 */
public class BundleClassLoader extends DexClassLoader {

    private Bundle bundle;

    public BundleClassLoader(Bundle bundle,String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.bundle = bundle;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Class result = loadClassSuper(className);
        if(result == null)
            result = loadClassByDependency(className);

        return  result;
    }

    Class<?> loadClassByDependency(String className)
    {
        Class result = null;
        try
        {
            //System.out.println("want dependency:"+bundle.artifactId+":"+ bundle.dependencies+"--->"+className);
            if(bundle.dependencies != null && !bundle.dependencies.isEmpty())
            {
                for (String name : bundle.dependencies) {
                    Bundle dependency = BundleManager.getInstance().queryBundleByBundleName(name);
                    if(dependency != null && dependency.loaded)
                    {
                        return dependency.classLoader.loadClassSuper(className);
                    }
                }
            }
        }
        catch (Exception e){}

        return  result;
    }

    Class<?> loadClassSuper(String className)
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
