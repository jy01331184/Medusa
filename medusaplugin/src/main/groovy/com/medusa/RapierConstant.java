package com.medusa;

import org.gradle.api.Project;

/**
 * Created by tianyang on 17/2/6.
 */
public class RapierConstant {
    public static final String PLUGIN_VERSION = "1.0.3";

    public static final String LOCAL_BUNDLE_POSTFIX = ":0.0.0";


    public static String TEMP_PROPERTY = "";
    public static String BUILD_GRADLE = "";
    public static String LOCAL_PROPERTY = "";
    public static String BUNDLE_JSON = "";


    public static void INIT(Project o){
        TEMP_PROPERTY = o.getBuildDir().getAbsolutePath()+"/tmp/merge.properties";
        BUILD_GRADLE = o.getProjectDir().getAbsolutePath()+"/build.gradle";
        LOCAL_PROPERTY = o.getProjectDir().getAbsolutePath()+"/local.properties";
        BUNDLE_JSON = o.getBuildDir().getAbsolutePath()+"/tmp/assets/bundle.json";

    }
}
