package com.medusa.bundle;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.medusa.application.MedusaApplicationProxy;
import com.medusa.application.MedusaLisenter;
import com.medusa.classloader.MedusaClassLoader;
import com.medusa.util.Constant;
import com.medusa.util.FileUtil;
import com.medusa.util.Log;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        if (bundle != null && !TextUtils.isEmpty(bundle.artifactId)) {
            bundleConfig.bundles.put(bundle.artifactId, bundle);
        }
    }

    public void removeBundle(Bundle bundle) {
        Bundle removeBundle = bundleConfig.bundles.remove(bundle.artifactId);

        File dexOptFile = Constant.getDexOptFile(bundle);
        if (dexOptFile != null)
            dexOptFile.delete();
        Log.info("BundleManager", "remove bundle:" + removeBundle);
    }

    public Bundle queryBundleByBundleName(String bundleName) {
        if (bundleConfig.bundles != null)
            return bundleConfig.bundles.get(bundleName);
        return null;
    }

    public Bundle queryBundleName(String className) {

        if (bundleConfig != null && bundleConfig.bundles != null) {
            String packageName;
            int index = className.lastIndexOf(".");
            if (index < 0)
                packageName = className;
            else
                packageName = className.substring(0, index);

            Bundle result = bundleConfig.exportPackages.get(packageName);

            if (result != null)
                return result;
//
            Collection<Bundle> values = bundleConfig.bundles.values();
            if (values != null) {
                for (Bundle bundle : values) {
                    if (bundle.activities != null && bundle.activities.contains(className)) {
                        return bundle;
                    }
                }
            }

        }

        return null;
    }

    public void init() {
        try {
            bundleConfig = new BundleConfig();
            bundleConfig.bundles = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean initBundleUpdate(MedusaLisenter lisenter) {
        try {
            boolean bundleChange = localUpdateBundle() ? true : false;
            BundleUtil.generateGloableExportPackages(bundleConfig);
            if (bundleChange)
                save();
        } catch (Exception e) {
            e.printStackTrace();
            lisenter.onMedusaLoad(MedusaLisenter.MedusaLoadState.FAIL);
            return false;
        }
        return true;
    }

    public void initBundleClassLoader(MedusaClassLoader classLoader, MedusaLisenter lisenter) {
        try {
            BundleExecutor.getInstance().loadBundle(bundleConfig.bundles.values(), classLoader, lisenter);
            lisenter.onMedusaLoad(new MedusaLisenter.MedusaLoadState(1));
        } catch (Exception e) {
            e.printStackTrace();
            lisenter.onMedusaLoad(MedusaLisenter.MedusaLoadState.FAIL);
        }
    }

    private void save() {
        File file = Constant.getPluginInfoFile();
        FileUtil.writeToFileAsync(file, bundleConfig);
        Log.info("BundleManager", "async save bundle info to " + file.getAbsolutePath());
    }

    public boolean load() {
        try {
            long start = System.currentTimeMillis();
            File file = Constant.getPluginInfoFile();
            String content = FileUtil.readFile(file);
            if (TextUtils.isEmpty(content))
                return false;
            bundleConfig = JSONObject.parseObject(content, BundleConfig.class);
            if (bundleConfig.bundles == null) {
                bundleConfig.bundles = new HashMap<>();
            }
            Log.info("BundleManager", "load bundle info from " + file.getAbsolutePath() + " use " + (System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean localUpdateBundle() throws Exception {
        boolean change = false;
        String sourceMd5 = BundleUtil.readRapierMD5(new File(MedusaApplicationProxy.getInstance().getApplication().getApplicationInfo().sourceDir));

        if (TextUtils.equals(sourceMd5, bundleConfig.sourceMd5))
            return false;

        Log.info("BundleManager", "apk md5 changes from:" + bundleConfig.sourceMd5 + " to " + sourceMd5);

        Map<String, Bundle> dict = BundleUtil.generateBundleDict();

        Collection<Bundle> values = bundleConfig.bundles.values();

        for (Bundle bundle : values) { //bundle 是当前存在data/data的
            Bundle bundleInAsset = dict.remove(bundle.artifactId);

            if (bundleInAsset == null)   //该bundle已经被删除
            {
                BundleExecutor.getInstance().deleteBundle(bundle);
                change = true;
                continue;
            }

//            File bundleFile = Constant.getBundleFile(bundle);
//            if (!bundleFile.exists())    //如果不存在 则不存在替换问题
//                continue;
            /**
             * 本地bundle升级替换策略
             */
            bundle.slink = bundleInAsset.slink;

            int result = BundleUtil.compareVersion(bundleInAsset.version, bundle.version);
            if (result > 0 || BundleUtil.compareLocalBundle(bundleInAsset, bundle)) {
                File assetBundleFile = Constant.getBundleFile(bundleInAsset);
                if (assetBundleFile.exists()) {
                    File dexOptFile = Constant.getDexOptFile(bundle);
                    if (dexOptFile != null && dexOptFile.exists()) {
                        dexOptFile.delete();
                        Log.info("BundleManager", "delete old bundle dex" + dexOptFile.getAbsolutePath());
                    }

                    Log.info("BundleManager", "update bundle " + bundle.artifactId + ":" + bundle.version + " to " + bundleInAsset.version);
                    BundleUtil.syncBundle(assetBundleFile, bundle);
                    bundle.path = bundleInAsset.path;
                    change = true;
                } else {
                    Log.error("BundleManager", "update bundle fail." + assetBundleFile.getAbsolutePath() + " not exist!");
                }
            }

        }
        Collection<Bundle> leftValues = dict.values();
        //新增bundle
        for (Bundle tempBundle : leftValues) {
            Log.info("BundleManager", "add new bundle " + tempBundle.artifactId + ":" + tempBundle.version);
            bundleConfig.bundles.put(tempBundle.artifactId, tempBundle);
            File originBundleFile = Constant.getBundleFile(tempBundle);
            BundleUtil.syncBundle(originBundleFile, tempBundle);
            change = true;
        }

        BundleExecutor.getInstance().commit();
        bundleConfig.sourceMd5 = sourceMd5;
        return change;
    }


    public BundleConfig getBundleConfig() {
        return bundleConfig;
    }
}
