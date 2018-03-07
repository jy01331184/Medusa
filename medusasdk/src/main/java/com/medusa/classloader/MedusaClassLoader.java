package com.medusa.classloader;

import com.medusa.application.MedusaApplicationProxy;
import com.medusa.bundle.Bundle;
import com.medusa.bundle.BundleExecutor;
import com.medusa.bundle.BundleManager;
import com.medusa.util.Log;
import com.medusa.util.ThreadRelatedLock;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tianyang on 16/8/11.
 */
public class MedusaClassLoader extends ClassLoader {

    private final ConcurrentHashMap<String, ThreadRelatedLock> loadingClassNames = new ConcurrentHashMap<String, ThreadRelatedLock>();

    private ClassLoader pathClassLoader;
    private BundleManager bundleManager;
    private BundleExecutor bundleExecutor;
    private ClassNotFoundException mDefaultException;


    public MedusaClassLoader(ClassLoader parent, ClassLoader origin, BundleManager bundleManager, BundleExecutor bundleExecutor) {
        super(parent);
        this.pathClassLoader = origin;
        this.bundleManager = bundleManager;
        this.bundleExecutor = bundleExecutor;
        ThreadRelatedLock lock = ThreadRelatedLock.createClassLoaderLock(Thread.currentThread().getId());
        /** 下面的类, 在HostClassLoader.loadClass()中会用到, 须提前加载, 避免这些类本身加载失败 */
        loadingClassNames.put(ThreadRelatedLock.class.getName(), lock);
        mDefaultException = new ClassNotFoundException("HostClassLoader");
    }

    public ClassLoader getPathClassLoader() {
        return pathClassLoader;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;

        try {
            clazz = super.loadClass(className, resolve);
        } catch (ClassNotFoundException e) {
            // NOSONAR
        }

        if (clazz == null) {
            long currentThreadId = Thread.currentThread().getId();
            ThreadRelatedLock lock = loadingClassNames.get(className);
            if (null == lock) {
                lock = ThreadRelatedLock.createClassLoaderLock(currentThreadId);
                loadingClassNames.put(className, lock);
            } else {
                if (currentThreadId == lock.getThreadId()) {
                    lock = null; // 本线程正在加载中, 避免无限递归
                }
            }

            if (lock != null) {
                Bundle bundle = bundleManager.queryBundleName(className);

                if (bundle != null) {
                    if (bundle.loaded && bundle.classLoader != null) {
                        clazz = bundle.classLoader.loadClass(className);
                        if (clazz != null) {
                            loadingClassNames.remove(className);
                        }
                    } else {
                        synchronized (bundle.loaded) {
                            if ((!bundle.loaded || bundle.classLoader == null)) {
                                Log.log("MedusaClassLoader", "hard load bundle:" + bundle.artifactId + " for class:" + className);
                                bundleExecutor.loadBundle(this, bundle, MedusaApplicationProxy.getInstance().getLisenter());
                                if (bundle.classLoader != null && bundle.loaded) {
                                    clazz = bundle.classLoader.loadClass(className);
                                    if (clazz != null) {
                                        loadingClassNames.remove(className);
                                    }
                                }
                            } else {
                                clazz = bundle.classLoader.loadClass(className);
                                if (clazz != null) {
                                    loadingClassNames.remove(className);
                                }
                            }
                        }
                    }
                }
            }
        }

        //Log.log("med",className+" :" + clazz);
        if (null == clazz) {
            throw mDefaultException;
        }
        return clazz;
    }

    Class loadClassFromDependency(Bundle bundle, String className) {
        Class result = null;

        Bundle targetBundle = BundleManager.getInstance().queryBundleName(className);

        if (targetBundle != null && targetBundle.loaded) {
            Class<?> cls = targetBundle.classLoader.loadClassDirectly(className);
            if (cls != null) {
                //System.out.println("load "+className+" in from dep:"+dependency.artifactId);
                return cls;
            }

        }

        return result;
    }


}
