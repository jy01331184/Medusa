package com.medusa.bundle;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.medusa.application.MedusaApplicationProxy;
import com.medusa.util.Constant;
import com.medusa.util.FileUtil;
import com.medusa.util.Log;
import com.medusa.util.ReflectUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by tianyang on 16/8/11.
 */
public class BundleUtil {

    /**
     * 更新bundle的MD5 priority dependency
     *
     * @param bundleFile
     * @param bundle
     */
    public static void syncBundle(File bundleFile, Bundle bundle) {
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            zipFile = new ZipFile(bundleFile);
            inputStream = zipFile.getInputStream(zipFile.getEntry("META-INF/BUNDLE.MF"));
            Properties properties = new Properties();
            properties.load(inputStream);
            bundle.priority = Integer.parseInt(properties.getProperty("priority", Constant.PRIORITY_LAZY + ""));
            String dependencyStr = properties.getProperty("dependency", "");
            bundle.dependencies = Arrays.asList(dependencyStr.split(","));

            String bundleStr = properties.getProperty("medusaBundles", "");
            bundle.medusaBundles = new HashMap<>();

            if (!TextUtils.isEmpty(bundleStr)) {
                JSONObject jsonObject = JSON.parseObject(bundleStr);
                Set<String> keys = jsonObject.keySet();

                for (String key : keys) {
                    bundle.medusaBundles.put(key, jsonObject.getString(key));
                }
            }

            bundle.version = properties.getProperty("version");
            bundle.md5 = properties.getProperty("md5");
            String activities = properties.getProperty("activities");
            if (!TextUtils.isEmpty(activities)) {
                bundle.activities = new HashSet<>(Arrays.asList(activities.split(",")));
            }
            String exportPackages = properties.getProperty("exportPackages");
            if (!TextUtils.isEmpty(exportPackages)) {
                bundle.exportPackages = new HashSet<>(Arrays.asList(exportPackages.split(",")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static String getBundleFileName(Bundle bundle) {
        if (bundle == null || TextUtils.isEmpty(bundle.path))
            return null;
        if (bundle.path.endsWith(".apk"))
            return bundle.path;
        if (bundle.path.endsWith(".so"))
            return bundle.path.substring(0, bundle.path.length() - 3) + ".apk";
        else
            return bundle.path;
    }

    /**
     * @return 1 -> s1 > s2     0 -> s1 = s2    -1 -> s1 < s2
     */
    public static int compareVersion(String s1, String s2) {
        if (TextUtils.isEmpty(s1) && TextUtils.isEmpty(s2))
            return 0;
        else if (TextUtils.isEmpty(s1))
            return -1;
        else if (TextUtils.isEmpty(s2))
            return 1;

        if (s1.equals("0.0.0") && !s2.equals("0.0.0"))
            return 1;
        if (!s1.equals("0.0.0") && s2.equals("0.0.0"))
            return 1;

        String[] s1Versions = s1.split("\\.");
        String[] s2Versions = s2.split("\\.");

        int minLength = Math.min(s1Versions.length, s2Versions.length);
        for (int i = 0; i < minLength; i++) {
            Integer s1Temp = Integer.parseInt(s1Versions[i]);
            Integer s2Temp = Integer.parseInt(s2Versions[i]);
            if (s1Temp != s2Temp)
                return s1Temp - s2Temp;
        }

        return s1Versions.length - s2Versions.length;
    }

    public static boolean compareLocalBundle(Bundle bundleInAsset, Bundle bundle) {
        if (bundleInAsset.isLocalBundle() && bundle.isLocalBundle()) {

            if (TextUtils.isEmpty(bundle.md5)) {
                return true;
            }

            File assetBundleFile = Constant.getBundleFile(bundleInAsset);

            if (!TextUtils.equals(readBundleMD5(assetBundleFile), bundle.md5)) {
                return true;
            }
        }

        return false;
    }

    private static String readBundleMD5(File file) {
        ZipFile zipFile = null;
        InputStream ins = null;
        String md5 = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry("META-INF/BUNDLE.MF");
            ins = zipFile.getInputStream(entry);
            Properties properties = new Properties();
            properties.load(ins);
            md5 = properties.getProperty("md5");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ins != null) {
                    ins.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return md5;
    }

    public static String readRapierMD5(File file) {
        ZipFile zipFile = null;
        String md5 = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry("META-INF/RAPIER.MF");
            InputStream ins = zipFile.getInputStream(entry);
            Properties properties = new Properties();
            properties.load(ins);
            md5 = properties.getProperty("md5");
            ins.close();
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return md5;
    }

    public static void replaceResource(Context context, String clsName) {
        Bundle bundle = BundleManager.getInstance().queryBundleName(clsName);

        if (bundle != null) {
            ReflectUtil.replaceResource(context, bundle.resources);
            //File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
//            Log.log("BundleUtil","replace res:"+bundle.resources+"-"+bundleFile.exists());
//            if (bundleFile.exists()) {
//
//            }
        }
    }

    public static Map<String, Bundle> generateBundleDict() {
//        Type collectionType = new TypeToken<Map<String, Bundle>>() {
//        }.getType();
        String str = FileUtil.readAssetFile(MedusaApplicationProxy.getInstance().getApplication(), "bundle.json");
        Map<String, Bundle> tempBundles = JSON.parseObject(str, new TypeReference<Map<String, Bundle>>() {
        });

        return tempBundles;
    }

    public static void generateGloableExportPackages(BundleConfig bundleConfig) {
        if (bundleConfig.bundles != null) {
            Collection<Bundle> values = bundleConfig.bundles.values();
            if (values != null) {
                for (Bundle bundle : values) {
                    if (bundle.exportPackages != null) {
                        for (String pck : bundle.exportPackages) {
                            bundleConfig.exportPackages.put(pck, bundle);
                        }
                    }
                }
            }
        }
    }

    public static boolean copyBundleFile(Bundle bundle, File bundleFile) {
        try {
            if (!bundleFile.exists()) {
                File file = new File(MedusaApplicationProxy.getInstance().getApplication().getApplicationInfo().nativeLibraryDir + "/" + bundle.path);
                FileUtil.copyFile(new FileInputStream(file).getChannel(), bundleFile.getAbsolutePath());
                Log.log("BundleManager", "copy bundle " + bundle.artifactId + " to" + bundleFile.getAbsolutePath());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
