package com.medusa.util;

import com.medusa.application.MedusaApplicationProxy;

import java.io.File;

/**
 * Created by tianyang on 16/8/11.
 */
public class Constant {

    public static File getPluginDir()
    {
        File optDir = new File(MedusaApplicationProxy.getInstance().getApplication().getFilesDir(),"plugins");
        if(!optDir.exists())
            optDir.mkdir();
        return optDir;
    }

    public static File getPluginInfoFile()
    {
        File optDir = new File(getPluginDir(),"bundle.json");

        return optDir;
    }

    public static File getDexOptFile(File apkFile)
    {
        if(apkFile == null)
            return null;

        return new File(apkFile.getAbsolutePath().replaceAll(".apk",".dex"));

    }

    public static final int PRIORITY_LAZY = 1747;
    public static final int PRIORITY_MAX = 0;
}
