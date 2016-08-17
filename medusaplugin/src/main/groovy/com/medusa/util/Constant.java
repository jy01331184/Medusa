package com.medusa.util;

/**
 * Created by tianyang on 16/8/9.
 */
public class Constant {

    public static final String PLUGIN_VERSION = "1.0.0";

    public static final String BUNDLE_GRADLE_STR = "/**\n" +
            " * 插件自动生成 请勿手动改动\n" +
            " */\n" +
            "\n" +
            "buildscript {\n" +
            "    repositories {\n" +
            "        jcenter()\n" +
            "        mavenLocal()\n" +
            "    }\n" +
            "    dependencies {\n" +
            "        classpath 'com.medusa.plugin:medusaplugin:"+PLUGIN_VERSION+"'\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "allprojects {\n" +
            "    repositories {\n" +
            "        jcenter()\n" +
            "        mavenLocal()\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "apply plugin: 'java'\n" +
            "apply plugin: 'medusa.linken'\n" +
            "dependencies {";

}
