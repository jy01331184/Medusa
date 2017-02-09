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
import com.medusa.util.BundleUtil
import com.medusa.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

public class RapierPlugin implements Plugin<Project> {

    AppExtension android;

    @Override
    void apply(Project o) {
        Task assemableRapierTask = o.task('assemableRapier')
        Task installRapierTask = o.task('installRapier')

        android = o.extensions.findByName("android")
        println("rapier plugin:"+Constant.PLUGIN_VERSION)

        assemableRapierTask.group = 'bundle'
        installRapierTask.group = 'bundle'
        assemableRapierTask.doLast{
            hookProcessManifest(o,o.tasks.findByName("processReleaseManifest"))
            hookMergeAssets(o,o.tasks.findByName('mergeReleaseAssets'))
        }

        installRapierTask.doLast {
            hookProcessManifest(o,o.tasks.findByName("processReleaseManifest"))
            hookMergeAssets(o,o.tasks.findByName('mergeReleaseAssets'))
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

        assemableRapierTask.dependsOn.add(prepareBundleTask)
        installRapierTask.dependsOn.add(prepareBundleTask)


        o.afterEvaluate{
            installRapierTask.finalizedBy o.tasks.findByName('installRelease')
            assemableRapierTask.finalizedBy o.tasks.findByName('assembleRelease')

            o.tasks.matching {it.name.startsWith("lintVital")}.each {
                Log.log("RapierPlugin","disable lint "+it.name)
                it.enabled = false
            }
            Task mergeJniLib = o.tasks.findByName("mergeReleaseJniLibFolders")

            mergeJniLib.inputs.dir(o.projectDir.absolutePath+"/linken")

            mergeJniLib.doLast{
                File file = null
                mergeJniLib.outputs.files.files.each {
                    if(it.absolutePath.contains('intermediates/jniLibs/release'))
                        file = it
                }
                Log.log("RapierPlugin",'mergeBundleToLibTask copy to '+file.absolutePath)
                o.copy{
                    from o.projectDir.absolutePath+"/linken"
                    into file.absolutePath+"/armeabi" // 目标位置
                    include '*.so'
                }
            }
        }

        o.tasks.findByName('clean').doLast{
            Log.log("RapierPlugin","clean /linken")
            o.delete o.fileTree(o.projectDir.absolutePath+'/linken') {}
        }
    }

    void makePrepareBundleTask(Task prepareBundleTask,Project o){
//        prepareBundleTask.inputs.file(o.projectDir.absolutePath+"/bundle.properties")
//        prepareBundleTask.inputs.file(o.projectDir.absolutePath+"/local.properties")
//        prepareBundleTask.outputs.file(o.buildDir.absolutePath+"/tmp/merge.properties")
//        prepareBundleTask.outputs.file(o.projectDir.absolutePath+"/linken")

        prepareBundleTask.doLast{
            Log.log("RapierPlugin","prepareBundle")
            BaseMedusaTask prepareTask = BaseMedusaTask.regist(o,PrepareBundleTask.class);
            prepareTask.execute(o.projectDir,new File(o.buildDir.absolutePath+"/tmp/merge.properties"))

            o.getConfigurations().create('bundle')

            Map<String,String> map = prepareTask.getResult()

            for (String key : map.keySet()) {
                o.getDependencies().add('bundle',map.get(key)+"@apk")
                o.getDependencies().add('bundle',map.get(key)+":AndroidManifest@xml")
            }

            o.delete o.fileTree(o.projectDir.absolutePath+"/linken")
            o.copy{
                into "linken"
                from o.configurations.matching {
                    it.name.startsWith("bundle")
                }
            }
            o.fileTree(o.projectDir.absolutePath+"/linken").each {
                if(it.name.endsWith('.apk'))
                {
                    def prefix = "lib"+it.name.substring(0,it.name.length()-4)
                    Log.log("RapierPlugin","rename "+it.name+" to:"+prefix+".so")
                    it.renameTo(new File(it.parentFile,prefix+".so"))
                }
            }
        }
    }

    void hookProcessManifest(Project project,Task rTask)
    {
        BaseTask.class.getDeclaredFields().find{
            it.name.equals("androidBuilder")
        }.each {
            Log.log("RapierPlugin",'hookProcessManifest')
            rTask.inputs.dir(project.projectDir.absolutePath+"/linken")
            it.setAccessible(true)
            AndroidBuilder originBuilder = it.get(rTask);
            MedusaAndroidBuilder mBuilder = new MedusaAndroidBuilder(originBuilder,android,rTask,project);
            it.set(rTask,mBuilder)
        }
    }

    void hookMergeAssets(Project project,Task task)
    {
        String json = BundleUtil.readBundleProperty(new File(project.buildDir.absolutePath+"/tmp/merge.properties"),new File(project.projectDir.absolutePath+"/linken"))

        task.outputs.files.files.each {
            if(it.absolutePath.contains('intermediates/assets/release'))
            {
                Log.log("RapierPlugin","hookMergeAssets add bundle.json to"+it.absolutePath)
                Log.log("RapierPlugin","bundle.json:"+json)
                File file = new File(it.absolutePath+"/bundle.json")
                if(!file.getParentFile().exists())
                    file.getParentFile().mkdirs()
                FileWriter writer = new FileWriter(file)
                writer.write(json)
                writer.flush()
                writer.close()
            }
        }
    }
}