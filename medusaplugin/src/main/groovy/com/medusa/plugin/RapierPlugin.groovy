package com.medusa.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.builder.core.AndroidBuilder
import com.android.ide.common.xml.AndroidManifestParser
import com.android.io.FolderWrapper
import com.android.xml.AndroidManifest
import com.medusa.Constant
import com.medusa.MedusaAndroidBuilder
import com.medusa.task.BaseMedusaTask
import com.medusa.task.PrepareBundleTask
import com.medusa.task.PrepareDependencyTask
import com.medusa.util.BundleUtil
import com.medusa.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

public class RapierPlugin implements Plugin<Project> {

    AppExtension android;

    @Override
    void apply(Project o) {
        Task assemableRapierTask = o.task('assemableRapier')
        Task installRapierTask = o.task('installRapier')
        Constant.INIT(o)
        android = o.extensions.findByName("android")
        android.sourceSets{
            main.assets.srcDirs = [main.assets.srcDirs,o.file(Constant.BUNDLE_JSON).parentFile.absolutePath]

        }
        println("rapier plugin:"+Constant.PLUGIN_VERSION)
        o.getConfigurations().create('bundle')
        assemableRapierTask.group = 'bundle'
        installRapierTask.group = 'bundle'
        assemableRapierTask.doLast{
            hookProcessManifest(o,o.tasks.findByName("processReleaseManifest"))
        }

        installRapierTask.doLast {
            hookProcessManifest(o,o.tasks.findByName("processReleaseManifest"))
            o.tasks.findByName('installRelease').doLast {
                o.tasks.findByName('processReleaseManifest').outputs.files.files.each {
                    if(it.name.contains("AndroidManifest.xml"))
                    {
                        def data = AndroidManifestParser.parse(AndroidManifest.getManifest(new FolderWrapper(it.parentFile)))
                        def mArgs = ['shell','am','start','-n',data.package+"/"+data.launcherActivity.name,'-a','android.intent.action.MAIN','-c','android.intent.category.LAUNCHER']
                        Log.log("RapierPlugin",'adb install'+android.adbExe.absolutePath+ ' args:'+mArgs)

                        o.exec {
                            executable = android.adbExe.absolutePath
                            args = mArgs
                        }
                    }
                }
            }
        }

        Task prepareBundleTask = o.task('prepareBundle') //生成bundle的依赖
        makePrepareBundleTask(prepareBundleTask,o)

        Task prepareDependencyTask = o.task('prepareDependency')
        makePrepareDependencyTask(prepareDependencyTask,o)

        Task prepareBundleJsonTask = o.task('prepareBundleJson')
        makePrepareBundleJsonTask(prepareBundleJsonTask,o)


        prepareBundleTask.finalizedBy prepareDependencyTask
        prepareDependencyTask.finalizedBy prepareBundleJsonTask

        assemableRapierTask.dependsOn.add(prepareBundleTask)
        installRapierTask.dependsOn.add(prepareBundleTask)


        o.afterEvaluate{
            installRapierTask.finalizedBy o.tasks.findByName('installRelease')
            assemableRapierTask.finalizedBy o.tasks.findByName('assembleRelease')

            Task mergeAssetTask = o.tasks.findByName('mergeReleaseAssets')
            mergeAssetTask.inputs.file(o.file(Constant.BUNDLE_JSON).parentFile)

            o.tasks.matching {it.name.startsWith("lintVital")}.each {
                Log.log("RapierPlugin","disable lint "+it.name)
                it.enabled = false
            }
            Task mergeJniLib = o.tasks.findByName("mergeReleaseJniLibFolders")

            mergeJniLib.doLast{
                File file = null
                mergeJniLib.outputs.files.files.each {
                    if(it.absolutePath.contains('intermediates/jniLibs/release'))
                        file = it
                }
                Log.log("RapierPlugin",'mergeBundleToLibTask copy to '+file.absolutePath)
                Configuration bundleConf = o.configurations.getByName('bundle')
                bundleConf.files.each {
                    if(it.name.endsWith('.apk')){
                        String path = it.absolutePath
                        String soName =  "lib"+it.name.substring(0,it.name.length()-4)+".so"
                        o.copy{
                            from path
                            into file.absolutePath+"/armeabi/" // 目标位置
                        }
                        File dest = o.file(file.absolutePath+"/armeabi/"+it.name)
                        if(dest.exists())
                            dest.renameTo(file.absolutePath+"/armeabi/"+soName)
                    }
                }

            }
        }

    }

    void makePrepareDependencyTask(Task prepareDependencyTask,Project o){

        prepareDependencyTask.doLast{
            Log.log("RapierPlugin","prepareDependencyTask")
            BaseMedusaTask prepareTask = BaseMedusaTask.regist(o,PrepareDependencyTask.class);
            prepareTask.execute(o.file(Constant.TEMP_PROPERTY),null)
            List<String> list = prepareTask.getResult()

            for (String dep:list) {
                o.getDependencies().add('bundle',dep+"@apk")
                o.getDependencies().add('bundle',dep+":AndroidManifest@xml")
            }

            Configuration bundleConf = o.configurations.getByName('bundle')

            Task mergeJniLib = o.tasks.findByName("mergeReleaseJniLibFolders")
            bundleConf.files.each {
                if(it.name.endsWith('.apk')){
                    mergeJniLib.inputs.file(it)
                }
            }
        }
    }

    void makePrepareBundleTask(Task prepareBundleTask,Project o){

        prepareBundleTask.inputs.files(Constant.BUNDLE_PROPERTY,Constant.LOCAL_PROPERTY)
        prepareBundleTask.outputs.file(Constant.TEMP_PROPERTY)

        prepareBundleTask.doLast{
            Log.log("RapierPlugin","prepareBundle")
            BaseMedusaTask prepareTask = BaseMedusaTask.regist(o,PrepareBundleTask.class);
            prepareTask.execute(o.projectDir,new File(Constant.TEMP_PROPERTY))
        }
    }

    void makePrepareBundleJsonTask(Task task,Project o)
    {
        task.inputs.file(Constant.TEMP_PROPERTY)
        task.outputs.file(Constant.BUNDLE_JSON)

        task.doLast {
            String json = BundleUtil.readBundleProperty(o,new File(Constant.TEMP_PROPERTY))
            File file = new File(Constant.BUNDLE_JSON)
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs()
            FileWriter writer = new FileWriter(file)
            writer.write(json)
            writer.flush()
            writer.close()
            Log.log("RapierPlugin","hookMergeAssets add bundle.json to"+file.absolutePath)
            Log.log("RapierPlugin","bundle.json:"+json)
            Task mergeAssetTask = o.tasks.findByName('mergeReleaseAssets')
            mergeAssetTask.inputs.file(o.file(Constant.BUNDLE_JSON).parentFile)
        }
    }

    void hookProcessManifest(Project project,Task rTask)
    {
        BaseTask.class.getDeclaredFields().find{
            it.name.equals("androidBuilder")
        }.each {
            Log.log("RapierPlugin",'hookProcessManifest')
            project.configurations.getByName('bundle').files.each {
                if(it.absolutePath.endsWith(".xml")){
                    rTask.inputs.file(it)
                }
            }
            it.setAccessible(true)
            AndroidBuilder originBuilder = it.get(rTask);
            MedusaAndroidBuilder mBuilder = new MedusaAndroidBuilder(originBuilder,android,rTask,project);
            it.set(rTask,mBuilder)
        }
    }
}