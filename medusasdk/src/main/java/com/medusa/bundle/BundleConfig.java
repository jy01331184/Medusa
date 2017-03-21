package com.medusa.bundle;

import java.util.Map;

/**
 * Created by tianyang on 16/8/12.
 */
public class BundleConfig {

    public Map<String,Bundle> bundles;

    public String sourceMd5;


    public transient Map<String,Bundle> components;
}
