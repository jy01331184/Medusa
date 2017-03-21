package com.medusa;

import org.gradle.api.Project;

/**
 * Created by tianyang on 17/3/21.
 */
public class BundleConstant {
    public static String BUNDLE_PROPERTY = "";


    public static void INIT(Project o){
        BUNDLE_PROPERTY = o.getProjectDir().getAbsolutePath()+"/bundle.properties";
    }
}
