package com.medusa.classloader;

import com.medusa.bundle.Bundle;
import com.medusa.util.StringUtil;

import java.io.IOException;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by tianyang on 16/8/11.
 */
public class BundleClassLoader extends PathClassLoader {

    private Bundle bundle;
    private MedusaClassLoader medClassLoader;
    private DexFile dexFile;

    public BundleClassLoader(Bundle bundle, String dexPath, String optimizedDirectory, ClassLoader parent, MedusaClassLoader medClassLoader) {
        super(".", parent);
        this.bundle = bundle;
        this.medClassLoader = medClassLoader;
        try {
            if(!bundle.slink){
                dexFile = DexFile.loadDex(dexPath,generateOutputName(dexPath,optimizedDirectory),0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        //System.out.println(bundle.artifactId+" want load class:"+className);
        Class cls = loadClassDirectly(className);
        if (cls == null) {
            //System.out.println(bundle.artifactId+" want load class from dep:"+className);
            cls = medClassLoader.loadClassFromDependency(bundle, className);
            if (cls == null) {
                try {
                    //System.out.println(bundle.artifactId+" want load class from path:"+className);
                    cls = medClassLoader.getPathClassLoader().loadClass(className);
                } catch (Exception e) {

                }
            }
        }
        //System.out.println(bundle.artifactId+" return :"+className+":"+cls+"  by:"+cls.getClassLoader());
        if (null == cls) {
            throw new ClassNotFoundException("BundleClassLoader(" + bundle.path + ") can't find class: " + className
                    + ", depends: " + StringUtil.collection2String(bundle.dependencies));
        }
        return cls;
    }

    Class<?> loadClassDirectly(String className) {
        try {
            return super.loadClass(className);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class clazz = null;
        try {
            if(dexFile != null){
                clazz = dexFile.loadClass(name, this);
            }
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException("BundleClassLoader(" + bundle + ") Failed to findClass(" + name + ")", e);
        }

        return clazz;
    }

    public static String generateOutputName(String sourcePathName, String outputDir) {
        if(null==sourcePathName||null==outputDir){
            return null;
        }

        StringBuilder newStr = new StringBuilder(120);

        /* start with the output directory */
        newStr.append(outputDir);
        if (!outputDir.endsWith("/"))
            newStr.append("/");

        /* get the filename component of the path */
        String sourceFileName;
        int lastSlash = sourcePathName.lastIndexOf('/');
        if (lastSlash < 0)
            sourceFileName = sourcePathName;
        else
            sourceFileName = sourcePathName.substring(lastSlash + 1);

        /*
         * Replace ".jar", ".zip", whatever with ".dex".  We don't want to
         * use ".odex", because the build system uses that for files that
         * are paired with resource-only jar files.  If the VM can assume
         * that there's no classes.dex in the matching jar, it doesn't need
         * to open the jar to check for updated dependencies, providing a
         * slight performance boost at startup.  The use of ".dex" here
         * matches the use on files in /data/dalvik-cache.
         */
        int lastDot = sourceFileName.lastIndexOf('.');
        if (lastDot < 0)
            newStr.append(sourceFileName);
        else
            newStr.append(sourceFileName, 0, lastDot);
        newStr.append(".dex");

        return newStr.toString();
    }
}
