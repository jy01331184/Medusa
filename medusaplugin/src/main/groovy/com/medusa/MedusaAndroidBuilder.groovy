package com.medusa

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dependency.ManifestDependencyImpl
import com.android.builder.core.AaptPackageProcessBuilder
import com.android.builder.core.AndroidBuilder
import com.android.builder.core.DexOptions
import com.android.builder.dependency.ManifestDependency
import com.android.builder.sdk.TargetInfo
import com.android.ide.common.process.ProcessException
import com.android.ide.common.process.ProcessOutputHandler
import com.android.manifmerger.ManifestMerger2
import com.android.sdklib.BuildToolInfo
import com.android.utils.ILogger
import com.medusa.model.RapierExtention
import com.medusa.util.Log
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

import java.lang.reflect.Field
/**
 * Created by tianyang on 16/7/28.
 */
public class MedusaAndroidBuilder extends AndroidBuilder {

    private TestedExtension android;
    private Task task;
    private Project project;
    private final ILogger mLogger;
    private TargetInfo mTargetInfo;
    private String mCreatedBy;

    public MedusaAndroidBuilder(AndroidBuilder androidBuilder, TestedExtension android, Task task, Project project) {
        super(v(androidBuilder, "mProjectId"), v(androidBuilder, "mCreatedBy"), v(androidBuilder, "mProcessExecutor"), v(androidBuilder, "mJavaProcessExecutor"), v(androidBuilder, "mErrorReporter"), v(androidBuilder, "mLogger"), v(androidBuilder, "mVerboseExec"));
        s(this, androidBuilder, "mSdkInfo");
        s(this, androidBuilder, "mTargetInfo");
        s(this, androidBuilder, "mBootClasspathFiltered");
        s(this, androidBuilder, "mBootClasspathAll");
        s(this, androidBuilder, "mLibraryRequests");
        s(this, androidBuilder, "mCreatedBy")
        mLogger = v(androidBuilder, "mLogger")
        mTargetInfo = v(androidBuilder, "mTargetInfo")


        this.android = android;
        this.task = task;
        this.project = project;
    }

    @Override
    public void processResources(AaptPackageProcessBuilder aaptCommand, boolean enforceUniquePackageName, ProcessOutputHandler processOutputHandler) throws IOException, InterruptedException, ProcessException {
        HookedBuildTool hookedBuildTool = new HookedBuildTool(targetInfo.buildTools);

        Field field = targetInfo.class.getDeclaredField('mBuildToolInfo')
        field.accessible = true
        field.set(targetInfo, hookedBuildTool)
        super.processResources(aaptCommand, enforceUniquePackageName, processOutputHandler)
        //aapt(aaptCommand)

    }

    @Override
    void mergeManifests(File mainManifest, List<File> manifestOverlays, List<? extends ManifestDependency> libraries, String packageOverride, int versionCode, String versionName, String minSdkVersion, String targetSdkVersion, Integer maxSdkVersion, String outManifestLocation, String outAaptSafeManifestLocation, String outInstantRunManifestLocation, ManifestMerger2.MergeType mergeType, Map<String, Object> placeHolders, List<ManifestMerger2.Invoker.Feature> optionalFeatures, File reportFile) {
        project.configurations.getByName('medusa').files.each {
            if (it.absolutePath.endsWith(".xml")) {
                ManifestDependencyImpl manifestDependency = new ManifestDependencyImpl(it, new ArrayList<ManifestDependencyImpl>())
                libraries.add(manifestDependency)
                Log.log(this, "add merge manifest:" + it.absolutePath)
            }
        }

        super.mergeManifests(mainManifest, manifestOverlays, libraries, packageOverride, versionCode, versionName, minSdkVersion, targetSdkVersion, maxSdkVersion, outManifestLocation, outAaptSafeManifestLocation, outInstantRunManifestLocation, mergeType, placeHolders, optionalFeatures, reportFile)
    }

//    @Override
//    void mergeManifests(File mainManifest, List<File> manifestOverlays, List<? extends ManifestDependency> libraries, String packageOverride, int versionCode, String versionName, String minSdkVersion, String targetSdkVersion, Integer maxSdkVersion, String outManifestLocation, String outAaptSafeManifestLocation, ManifestMerger2.MergeType mergeType, Map<String, Object> placeHolders, File reportFile) {
//
//        project.configurations.getByName('medusa').files.each {
//            if (it.absolutePath.endsWith(".xml")) {
//                ManifestDependencyImpl manifestDependency = new ManifestDependencyImpl(it, new ArrayList<ManifestDependencyImpl>())
//                libraries.add(manifestDependency)
//                Log.log(this, "add merge manifest:" + it.absolutePath)
//            }
//        }
//
//        super.mergeManifests(mainManifest, manifestOverlays, libraries, packageOverride, versionCode, versionName, minSdkVersion, targetSdkVersion, maxSdkVersion, outManifestLocation, outAaptSafeManifestLocation, mergeType, placeHolders, reportFile)
//    }

    private void aapt(AaptPackageProcessBuilder aaptCommand) {

//        def aaptBaseDir,aapt;
//
//        if(Os.isFamily(Os.FAMILY_WINDOWS))
//            aapt = "aapt.exe"
//        else if(Os.isFamily(Os.FAMILY_MAC))
//            aapt = "aapt"
//        else if(Os.isFamily(Os.FAMILY_UNIX))
//            aapt = "aapt_linux"
//
//        if (project.parent != null)
//            aaptBaseDir = project.parent.projectDir.absolutePath + "/tools/"+aapt
//        else
//            aaptBaseDir = project.projectDir.absolutePath + "/tools/"+aapt
//
//
//
//        BundleExtention bundleExtention = project.extensions.findByName('bundle')
//
//        def packageId = bundleExtention == null ? 0x7f : bundleExtention.packageId
//        def androidJar = android.getSdkDirectory().absolutePath + "/platforms/" + android.getCompileSdkVersion() + "/android.jar"
//        def manifestDir = project.buildDir.absolutePath + "/intermediates/manifests/full/release/AndroidManifest.xml"
//        def resDir = ""
//        def assetDir = project.tasks.findByName("mergeReleaseAssets").outputs.files.files.getAt(1).absolutePath
//        def aapt_rules = ""
//        def apOutPutDir = ""
//        def rOutputDir = ""
//        def extraPackage = ""
//        targetInfo.buildTools
//
//        for (File resFile : project.tasks.findByName("mergeReleaseResources").outputs.files.files) {
//            if (resFile.absolutePath.indexOf("res/merged/release") >= 0)
//                resDir = resFile.absolutePath
//        }
//
//        String mainPackageName = aaptCommand.getPackageForR();
//        if (mainPackageName == null) {
//            mainPackageName = VariantConfiguration.getManifestPackage(aaptCommand.getManifestFile());
//        }
//
//        List<? extends SymbolFileProvider> libs = aaptCommand.getLibraries()
//        for (SymbolFileProvider provider : libs) {
//
//            if (provider.symbolFile != null && provider.symbolFile.isFile()) {
//                String packageName = VariantConfiguration.getManifestPackage(provider.getManifest());
//                if (!Utils.isEmpty(packageName) && !packageName.equals(mainPackageName)) {
//                    if (Utils.isEmpty(extraPackage))
//                        extraPackage = extraPackage.concat(packageName)
//                    else
//                        extraPackage = extraPackage.concat(":" + packageName)
//                }
//            }
//        }
//
//        Set<File> list = task.outputs.files.files;
//
//        for (File file : list) {
//            if (file.absolutePath.indexOf(".ap_") >= 0) {
//                apOutPutDir = file.absolutePath
//            } else if (file.absolutePath.indexOf("/source/r") >= 0) {
//                rOutputDir = file.absolutePath
//            } else if (file.absolutePath.indexOf("proguard-rules/release/aapt_rules") >= 0) {
//                aapt_rules = file.absolutePath
//            }
//        }
//
//        def mArgs = ['package', '--no-crunch', '-f', '-m', '-J', rOutputDir, '-S', resDir, '-I',
//                     androidJar, '-M', manifestDir,  '-F', apOutPutDir, '-0', 'apk', '--debug-mode',
//                     "-A", assetDir,'--package-id', packageId,'-x','--custom-package',mainPackageName,'--non-constant-id']
//        if(android.aaptOptions.additionalParameters != null)
//            mArgs.addAll(android.aaptOptions.additionalParameters)
//
//        if (!Utils.isEmpty(extraPackage)) {
//            mArgs.add('--extra-packages')
//            mArgs.add(extraPackage)
//        }
//        if (!Utils.isEmpty(aapt_rules)) {
//            mArgs.add('-G')
//            mArgs.add(aapt_rules)
//        }
//
//
//        Log.log(this, 'bundle aapt shell:' + aaptBaseDir + ' args:' + mArgs.asList())
//
//        project.exec {
//            executable = aaptBaseDir
//            args = mArgs
//        }
    }

    private static <T> T v(Object obj, String name) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void s(Object obj, Object origin, String name) {
        try {
            Field field = origin.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, v(origin, name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    String toString() {
        return getClass().name
    }

    @Override
    void convertByteCode(Collection<File> inputs, File outDexFolder, boolean multidex, File mainDexList, DexOptions dexOptions, List<String> additionalParameters, boolean incremental, boolean optimize, ProcessOutputHandler processOutputHandler) throws IOException, InterruptedException, ProcessException {
        Configuration bundleConf = project.configurations.getByName('medusa')

        List<File> finalInput = new ArrayList<>(inputs);
        RapierExtention rapierExtention = project.extensions.findByName('rapier');

        ResolvedConfiguration resolvedConfiguration = bundleConf.getResolvedConfiguration();

        if (rapierExtention.staticLink != null) {
            resolvedConfiguration.resolvedArtifacts.each {
                ModuleComponentIdentifier componentIdentifier = it.id.componentIdentifier
                String prefix = componentIdentifier.group + ":" + componentIdentifier.module
                if (rapierExtention.staticLink.contains(prefix) && it.extension.equals("apk")) {
                    finalInput.add(it.file)
                }
            }
        }

        Log.log(this, "convertByteCode:" + finalInput.toString())
        super.convertByteCode(finalInput, outDexFolder, multidex, mainDexList, dexOptions, additionalParameters, incremental, optimize, processOutputHandler)
    }


    class HookedBuildTool extends BuildToolInfo {

        private BuildToolInfo origin;

        HookedBuildTool(BuildToolInfo buildToolInfo) {
            super(buildToolInfo.getRevision(), buildToolInfo.getLocation())
            origin = buildToolInfo;
        }

        @Override
        String getPath(com.android.sdklib.BuildToolInfo.PathId pathId) {
            if (pathId == com.android.sdklib.BuildToolInfo.PathId.AAPT) {
                def aaptBaseDir, aapt;

                if (Os.isFamily(Os.FAMILY_WINDOWS))
                    aapt = "aapt.exe"
                else if (Os.isFamily(Os.FAMILY_MAC))
                    aapt = "aapt"
                else if (Os.isFamily(Os.FAMILY_UNIX))
                    aapt = "aapt_linux"

                if (project.parent != null)
                    aaptBaseDir = project.parent.projectDir.absolutePath + "/tools/" + aapt
                else
                    aaptBaseDir = project.projectDir.absolutePath + "/tools/" + aapt

                return aaptBaseDir
            }

            return origin.getPath(pathId)
        }
    }
}
