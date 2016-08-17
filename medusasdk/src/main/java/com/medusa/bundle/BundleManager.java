package com.medusa.bundle;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.medusa.application.MedusaApplication;
import com.medusa.application.MedusaClassLoader;
import com.medusa.util.Constant;
import com.medusa.util.FileUtil;
import com.medusa.util.GsonUtil;
import com.medusa.util.Log;
import com.medusa.util.MD5Util;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyang on 16/8/11.
 */
public class BundleManager {

    private static BundleManager instance;

    private BundleConfig bundleConfig;

    private BundleManager() {
    }

    public static BundleManager getInstance() {
        if (instance == null)
            instance = new BundleManager();
        return instance;
    }

    public void addBundle(Bundle bundle) {
        bundleConfig.bundles.add(bundle);
    }

    public void removeBundle(Bundle bundle)
    {
        bundleConfig.bundles.remove(bundle);
        File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
        bundleFile.delete();
        File dexOptFile = Constant.getDexOptFile(bundleFile);
        if(dexOptFile != null)
            dexOptFile.delete();
        Log.log(this,"remove bundle:"+bundle);
    }

    public Bundle queryBundleByBundleName(String bundleName) {
        for (Bundle bundle : bundleConfig.bundles) {
            if(bundle.artifactId.equals(bundleName))
                return bundle;
        }
        return null;
    }

    public Bundle queryBundleName(String className) {
        for (Bundle bundle : bundleConfig.bundles) {
            for (String name : bundle.activities) {
                if (name.equals(className))
                    return bundle;
            }
        }
        return null;
    }

    public void init() {
        try {
            Type collectionType = new TypeToken<ArrayList<Bundle>>() {}.getType();
            bundleConfig = new BundleConfig();
            bundleConfig.bundles = GsonUtil.getGson().fromJson(FileUtil.readAssetFile(MedusaApplication.getInstance(), "bundle.json"), collectionType);

            for (Bundle bundle : bundleConfig.bundles) {
                File originBundleFile = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + bundle.path);
                BundleUtil.sycnBundle(originBundleFile,bundle);
            }
            bundleConfig.sourceMd5 = MD5Util.genFileMd5sum(new File(MedusaApplication.getInstance().getApplicationInfo().sourceDir));
            save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initBundleClassLoader(MedusaClassLoader classLoader) {
        try {
            boolean bundleChange = localUpdateBundle()?true:false;
            bundleChange = makeBundle()?true:bundleChange;
            BundleExecutor.getInstance().loadBundle(bundleConfig.bundles,classLoader);
            if (bundleChange)
                save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        File file = Constant.getPluginInfoFile();
        FileUtil.writeToFile(file, GsonUtil.getGson().toJson(bundleConfig));
        Log.log(this, "save bundle info to " + file.getAbsolutePath());
    }

    public boolean load()
    {
        try
        {
            File file = Constant.getPluginInfoFile();
            String content = FileUtil.readFile(file);
            if (TextUtils.isEmpty(content))
                return false;
            bundleConfig = GsonUtil.getGson().fromJson(content, BundleConfig.class);
            Log.log(this, "load bundle info from " + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean localUpdateBundle() throws Exception
    {
        boolean change = false;
        String sourceMd5 = MD5Util.genFileMd5sum(new File(MedusaApplication.getInstance().getApplicationInfo().sourceDir));

        Type collectionType = new TypeToken<ArrayList<Bundle>>() {}.getType();
        List<Bundle> tempBundles = GsonUtil.getGson().fromJson(FileUtil.readAssetFile(MedusaApplication.getInstance(), "bundle.json"), collectionType);

        for (Bundle bundle : bundleConfig.bundles)
        {
            Bundle bundleInAsset = null;
            for (Bundle tempBundle : tempBundles) {
                if(TextUtils.equals(tempBundle.artifactId,bundle.artifactId))
                {
                    bundleInAsset = tempBundle;
                    tempBundles.remove(tempBundle);
                    break;
                }
            }

            if(bundleInAsset == null)   //该bundle已经被删除
            {
                BundleExecutor.getInstance().deleteBundle(bundle);
                change = true;
                continue;
            }

            File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
            if (!bundleFile.exists())    //如果不存在 则不存在替换问题
                continue;
            /**
             * 本地bundle升级替换策略
             */

            if (bundle.isLocalBundle()) //如果是本地bundle 则判断是否需要替换apk
            {
                File originBundleFile = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + bundle.path);
                if (originBundleFile.exists() && !MD5Util.genFileMd5sum(originBundleFile).equals(bundle.md5)) //本地budnle存在更新
                {
                    Log.log("BundleManager", "subsitute local bundle " + bundle.artifactId + " from" + originBundleFile.getAbsolutePath());
                    FileUtil.copyFile(new FileInputStream(originBundleFile).getChannel(), bundleFile.getAbsolutePath());
                    BundleUtil.sycnBundle(bundleFile,bundle);
                    bundle.activities = bundleInAsset.activities;
                    change = true;
                }
            }
            else if(BundleUtil.compareVersion(bundleInAsset.version, bundle.version) > 0)
            {
                File originBundleFile = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + bundleInAsset.path);
                if (originBundleFile.exists()) {
                    Log.log("BundleManager", "update remote bundle " + bundle.artifactId + ":" + bundle.version + " to " + bundleInAsset.version);
                    bundleFile.delete();
                    File dexOptFile = Constant.getDexOptFile(bundleFile);
                    if(dexOptFile != null)
                        dexOptFile.delete();
                    bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundleInAsset));
                    FileUtil.copyFile(new FileInputStream(originBundleFile).getChannel(), bundleFile.getAbsolutePath());
                    BundleUtil.sycnBundle(bundleFile,bundle);
                    bundle.activities = bundleInAsset.activities;
                    bundle.version = bundleInAsset.version;
                    bundle.path = bundleInAsset.path;
                    change = true;
                }
            }
        }

        //新增bundle
        for (Bundle tempBundle : tempBundles) {
            Log.log("BundleManager", "add new bundle " + tempBundle.artifactId + ":" + tempBundle.version );
            bundleConfig.bundles.add(tempBundle);
        }

        BundleExecutor.getInstance().commit();
        bundleConfig.sourceMd5 = sourceMd5;
        return change;
    }

    private boolean makeBundle() throws Exception
    {
        boolean change = false;
        for (Bundle bundle : bundleConfig.bundles)
        {
            /**
             *  plugindir下bundle不存在时 拷贝策略
             */
            File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
            if (!bundleFile.exists() || !MD5Util.genFileMd5sum(bundleFile).equals(bundle.md5)) {
                File file = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + bundle.path);
                FileUtil.copyFile(new FileInputStream(file).getChannel(), bundleFile.getAbsolutePath());

                Log.log("BundleManager", "copy bundle " + bundle.artifactId + " to" + bundleFile.getAbsolutePath());
                BundleUtil.sycnBundle(bundleFile,bundle);

                change = true;
            } else {
                Log.log("BundleManager", "load directly bundle " + bundle.artifactId + " from" + bundleFile.getAbsolutePath());
            }
        }
        return change;
    }
}
