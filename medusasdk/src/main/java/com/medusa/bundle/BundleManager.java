package com.medusa.bundle;

import android.text.TextUtils;

import com.medusa.application.MedusaApplication;
import com.medusa.classloader.MedusaClassLoader;
import com.medusa.util.Constant;
import com.medusa.util.FileUtil;
import com.medusa.util.GsonUtil;
import com.medusa.util.Log;
import com.medusa.util.MD5Util;

import java.io.File;
import java.util.Collection;
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
        if(bundle != null && !TextUtils.isEmpty(bundle.artifactId)){
            bundleConfig.bundles.put(bundle.artifactId,bundle);
        }
    }

    public void removeBundle(Bundle bundle)
    {
        Bundle removeBundle = bundleConfig.bundles.remove(bundle.artifactId);
        File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
        bundleFile.delete();
        File dexOptFile = Constant.getDexOptFile(bundleFile);
        if(dexOptFile != null)
            dexOptFile.delete();
        Log.log(this,"remove bundle:"+removeBundle);
    }

    public Bundle queryBundleByBundleName(String bundleName) {
        if(bundleConfig.bundles != null)
            return bundleConfig.bundles.get(bundleName);
        return null;
    }

    public Bundle queryBundleName(String className) {

        if(bundleConfig.bundles != null)
        {
            Collection<Bundle> values = bundleConfig.bundles.values();
            for (Bundle bundle : values) {
                if(bundle.activities != null && bundle.activities.contains(className)){
                    return bundle;
                }
            }
        }

        return null;
    }

    public void init() {
        try {
            bundleConfig = new BundleConfig();
            bundleConfig.bundles = BundleUtil.generateBundleDict();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initBundleClassLoader(MedusaClassLoader classLoader) {
        try {
            boolean bundleChange = localUpdateBundle()?true:false;
            BundleExecutor.getInstance().loadBundle(bundleConfig.bundles.values(),classLoader);
            if (bundleChange)
                save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableConfig(){
        bundleConfig.sourceMd5 = "";
        save();
    }

    private void save() {
        File file = Constant.getPluginInfoFile();
        String str = GsonUtil.getGson().toJson(bundleConfig);
        FileUtil.writeToFileAsync(file,str );
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

        if(TextUtils.equals(sourceMd5,bundleConfig.sourceMd5))
            return false;

        Map<String, Bundle> dict = BundleUtil.generateBundleDict();

        Collection<Bundle> values = bundleConfig.bundles.values();

        for (Bundle bundle : values)
        {
            Bundle bundleInAsset = dict.remove(bundle.artifactId);

            if(bundleInAsset == null)   //该bundle已经被删除
            {
                BundleExecutor.getInstance().deleteBundle(bundle);
                change = true;
                continue;
            }

            File bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundle));
//            if (!bundleFile.exists())    //如果不存在 则不存在替换问题
//                continue;
            /**
             * 本地bundle升级替换策略
             */

            if (bundle.isLocalBundle()) //如果是本地bundle 则判断是否需要替换apk
            {
                File originBundleFile = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + bundleInAsset.path);
                if (originBundleFile.exists() ) //本地bundle存在更新
                {
                    String originBundleMd5 = MD5Util.genFileMd5sum(originBundleFile);
                    if( !TextUtils.equals(originBundleMd5,bundle.md5)){
                        //// TODO: 17/3/7 合并 bundleMF -> BUNDLE.JSON
                        Log.log("BundleManager", "subsitute local bundle " + bundle.artifactId + " from" + originBundleFile.getAbsolutePath());
                        //FileUtil.copyFile(new FileInputStream(originBundleFile).getChannel(), new File(Constant.getPluginDir(),BundleUtil.getBundleFileName(bundleInAsset)).getAbsolutePath() );
                        BundleUtil.syncBundleWithoutMd5(originBundleFile,bundle);
                        bundle.version = bundleInAsset.version;
                        bundle.md5 = originBundleMd5;
                        bundle.activities = bundleInAsset.activities;
                       // if(!TextUtils.equals(bundle.path,bundleInAsset.path)){
                        File dexOptFile = Constant.getDexOptFile(bundleFile);
                        if(dexOptFile != null)
                            dexOptFile.delete();
                        bundleFile.delete();
                        //Log.log("BundleManager", "remove local bundle file for update" + bundleFile.getAbsolutePath());
                        bundle.path = bundleInAsset.path;
                        //}
                        change = true;
                    }
                }
            }
            else
            {
                int result = BundleUtil.compareVersion(bundleInAsset.version, bundle.version) ;
                if(result > 0 ){
                    File originBundleFile = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + bundleInAsset.path);
                    if (originBundleFile.exists()) {
                        Log.log("BundleManager", "update remote bundle " + bundle.artifactId + ":" + bundle.version + " to " + bundleInAsset.version);
                        bundleFile.delete();
                        File dexOptFile = Constant.getDexOptFile(bundleFile);
                        if(dexOptFile != null)
                            dexOptFile.delete();
//                        bundleFile = new File(Constant.getPluginDir(), BundleUtil.getBundleFileName(bundleInAsset));
                        BundleUtil.syncBundle(originBundleFile,bundle);
                        bundle.activities = bundleInAsset.activities;
                        bundle.version = bundleInAsset.version;
                        bundle.path = bundleInAsset.path;
                        change = true;
                    }else {
                        Log.error("BundleManager","update remote bundle fail."+originBundleFile.getAbsolutePath()+" not exist!");
                    }
                }
            }
        }
        Collection<Bundle> leftValues = dict.values();
        //新增bundle
        for (Bundle tempBundle : leftValues) {
            Log.log("BundleManager", "add new bundle " + tempBundle.artifactId + ":" + tempBundle.version );
            bundleConfig.bundles.put(tempBundle.artifactId,tempBundle);
            File originBundleFile = new File(MedusaApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/" + tempBundle.path);
            BundleUtil.syncBundle(originBundleFile,tempBundle);
            change = true;
        }

        BundleExecutor.getInstance().commit();
        bundleConfig.sourceMd5 = sourceMd5;
        return change;
    }

}
