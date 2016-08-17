package com.medusa.model;

/**
 * Created by tianyang on 16/8/3.
 */
public class BundleModel {


    public String name;

    public String packageId;

    public String raw;

    public String group;

    public String version;

    @Override
    public String toString() {
        return "BundleModel{" +
                "group='" + group + '\'' +
                ", version='" + version + '\'' +
                ", packageId='" + packageId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
