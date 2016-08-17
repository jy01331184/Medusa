package com.medusa.bundle;

import android.content.Context;
import android.text.TextUtils;

import com.medusa.util.Constant;
import com.medusa.util.MD5Util;
import com.medusa.util.ReflectUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipFile;

/**
 * Created by tianyang on 16/8/11.
 */
public class BundleUtil {

    /**
     * 更新bundle的MD5 priority dependency
     * @param bundleFile
     * @param bundle
     */
    public static void sycnBundle(File bundleFile,Bundle bundle)
    {
        try {
            ZipFile zipFile = new ZipFile(bundleFile);
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("META-INF/BUNDLE.MF"));
            bundle.md5 = MD5Util.genFileMd5sum(bundleFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            bundle.priority = Integer.parseInt(properties.getProperty("priority",Constant.PRIORITY_LAZY+""));
            String dependencyStr = properties.getProperty("dependency", "");
            bundle.dependencies = Arrays.asList(dependencyStr.split(","));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bundle readFromJson(JSONObject jsonObject)
    {
        if(jsonObject == null)
            return null;

        Bundle bundle = new Bundle();

        bundle.version = jsonObject.optString("version");
        bundle.artifactId = jsonObject.optString("artifactId");
        bundle.groupId = jsonObject.optString("groupId");
        bundle.path = jsonObject.optString("path");

        JSONArray array = jsonObject.optJSONArray("activities");
        List<String> activities = new ArrayList<>();

        for (int i = 0;i < array.length(); i++)
        {
            activities.add(array.optString(i));
        }
        bundle.activities = activities;

        return bundle;
    }

    public static String getBundleFileName(Bundle bundle)
    {
        if(bundle == null || TextUtils.isEmpty(bundle.path))
            return null;
        if(bundle.path.endsWith(".apk"))
            return bundle.path;
        if(bundle.path.endsWith(".so"))
            return bundle.path.substring(0,bundle.path.length()-3)+".apk";
        else
            return bundle.path;
    }

    /**
     * @return 1 -> s1 > s2     0 -> s1 = s2    -1 -> s1 < s2
     */
    public static int compareVersion(String s1,String s2)
    {
        if(TextUtils.isEmpty(s1) && TextUtils.isEmpty(s2))
            return 0;
        else if(TextUtils.isEmpty(s1))
            return -1;
        else if(TextUtils.isEmpty(s2))
            return 1;

        if(s1.equals("0.0.0") && !s2.equals("0.0.0"))
            return 1;
        if(!s1.equals("0.0.0") && s2.equals("0.0.0"))
            return 1;

        String[] s1Versions = s1.split("\\.");
        String[] s2Versions = s2.split("\\.");

        int minLength = Math.min(s1Versions.length,s2Versions.length);
        for (int i = 0; i < minLength; i++)
        {
            Integer s1Temp = Integer.parseInt(s1Versions[i]);
            Integer s2Temp = Integer.parseInt(s2Versions[i]);
            if(s1Temp != s2Temp)
                return s1Temp - s2Temp;
        }

        return s1Versions.length - s2Versions.length;
    }

    public static void replaceResource(Context context,String clsName)
    {
        Bundle bundle = BundleManager.getInstance().queryBundleName(clsName);

        if(bundle != null)
        {
            File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
            if(bundleFile.exists()){
                ReflectUtil.replaceResource(context,bundle.resources);
            }
        }
    }

}
