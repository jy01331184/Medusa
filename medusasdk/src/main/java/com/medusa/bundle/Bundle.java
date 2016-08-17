package com.medusa.bundle;

import java.util.List;

/**
 * Created by tianyang on 16/8/11.
 */
public class Bundle {

    /**
     * persistent
     */
    public String artifactId;
    public String groupId;
    public String version;

    public String path;
    public List<String> activities;
    public List<String> dependencies;
    public String md5;

    public int priority;

    /**
     * temp
     */


    public transient boolean loaded;

    public transient BundleClassLoader classLoader;

    public transient BundleResource resources;

    @Override
    public String toString() {
        return "Bundle{" +
                "artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", path='" + path + '\'' +
                ", activities=" + activities +
                ", dependencies=" + dependencies +
                ", md5='" + md5 + '\'' +
                ", priority=" + priority +
                '}';
    }

    public boolean isLocalBundle(){
        return "0.0.0".equals(version);
    }
}
