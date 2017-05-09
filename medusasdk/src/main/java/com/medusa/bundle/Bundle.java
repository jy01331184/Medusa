package com.medusa.bundle;

import com.medusa.classloader.BundleClassLoader;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Set<String> activities;
    public Set<String> exportPackages;
    public List<String> dependencies;
    public Map<String,String> medusaBundles;

    public int priority;

    /**
     * temp
     */

    public transient Boolean loaded = false;

    public transient BundleClassLoader classLoader;

    public transient BundleResource resources;

    @Override
    public String toString() {
        return "Bundle{" +
                "artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", path='" + path + '\'' +
                ", dependencies=" + dependencies +
                ", priority=" + priority +
                '}';
    }

    public boolean isLocalBundle(){
        return "0.0.0".equals(version);
    }
}
