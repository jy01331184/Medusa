package com.medusa.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.tasks.ZipAlign
import com.android.builder.core.AndroidBuilder
import com.android.ide.common.xml.AndroidManifestParser
import com.android.io.FolderWrapper
import com.android.xml.AndroidManifest
import com.medusa.MedusaAndroidBuilder
import com.medusa.RapierConstant
import com.medusa.RapierDependencySet
import com.medusa.model.RapierExtention
import com.medusa.task.AddRapierMFTask
import com.medusa.task.BaseMedusaTask
import com.medusa.task.PrepareBundleTask
import com.medusa.task.PrepareDependencyTask
import com.medusa.util.BundleUtil
import com.medusa.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.file.FileTree
import org.gradle.api.internal.artifacts.DefaultDependencySet
import org.gradle.api.internal.artifacts.configurations.DefaultConfiguration

import java.lang.reflect.Field

public class RapierPlugin implements Plugin<Project> {

    AppExtension android;

    @Override
    void apply(Project o) {
        o.extensions.create('rapier', RapierExtention)
        Task assemableRapierTask = o.task('assemableRapier')
        Task installRapierTask = o.task('installRapier')
        RapierConstant.INIT(o)
        android = o.extensions.findByName("android")
        android.sourceSets {
            main.assets.srcDirs = [main.assets.srcDirs, o.file(RapierConstant.BUNDLE_JSON).parentFile.absolutePath]
        }

        println("rapier plugin:" + RapierConstant.PLUGIN_VERSION)
        o.configurations.create('bundle')
        o.configurations.create('medusa')

        Configuration bundleConf = o.configurations.getByName('bundle')

        DefaultDependencySet hookedSet = new RapierDependencySet(o, bundleConf.dependencies)
        Field filed = DefaultConfiguration.class.getDeclaredField('dependencies')
        filed.setAccessible(true)
        filed.set(bundleConf, hookedSet)


        assemableRapierTask.group = 'bundle'
        installRapierTask.group = 'bundle'
        assemableRapierTask.doLast {
            hookProcessManifest(o, o.tasks.findByName("processReleaseManifest"))
            hookDex(o, o.tasks.findByName('transformClassesWithDexForRelease'))
        }


        installRapierTask.doLast {
            hookProcessManifest(o, o.tasks.findByName("processReleaseManifest"))
            hookDex(o, o.tasks.findByName('transformClassesWithDexForRelease'))
            o.tasks.findByName('installRelease').doLast {
                o.tasks.findByName('processReleaseManifest').outputs.files.files.each {
                    if (it.name.contains("AndroidManifest.xml")) {
                        def data = AndroidManifestParser.parse(AndroidManifest.getManifest(new FolderWrapper(it.parentFile)))
                        def mArgs = ['shell', 'am', 'start', '-n', data.package + "/" + data.launcherActivity.name, '-a', 'android.intent.action.MAIN', '-c', 'android.intent.category.LAUNCHER']
                        Log.log("RapierPlugin", 'adb install' + android.adbExe.absolutePath + ' args:' + mArgs)

                        o.exec {
                            executable = android.adbExe.absolutePath
                            args = mArgs
                        }
                    }
                }
            }
        }

        Task prepareBundleTask = o.task('prepareBundle') //生成bundle的依赖
        makePrepareBundleTask(prepareBundleTask, o)

        Task prepareDependencyTask = o.task('prepareDependency')
        makePrepareDependencyTask(prepareDependencyTask, o)

        Task prepareBundleJsonTask = o.task('prepareBundleJson')
        makePrepareBundleJsonTask(prepareBundleJsonTask, o)


        prepareBundleTask.finalizedBy prepareDependencyTask
        prepareDependencyTask.finalizedBy prepareBundleJsonTask

        assemableRapierTask.dependsOn.add(prepareBundleTask)
        installRapierTask.dependsOn.add(prepareBundleTask)

        BaseMedusaTask addRapierMF = BaseMedusaTask.regist(o, AddRapierMFTask.class);
        o.afterEvaluate {
            Task packageRelease = o.tasks.findByName('packageRelease')
            ZipAlign zipalignRelease = o.tasks.findByName('zipalignRelease')

            File input = packageRelease.outputs.files.files[0]
            File output = o.file(input.parentFile.absolutePath+"/"+o.name+"-rapier-unaligned.apk")
            Task addManifestTask = o.task('manifestTask')
            packageRelease.finalizedBy addManifestTask
            addManifestTask.inputs.file(input)
            addManifestTask.outputs.file(output)

            zipalignRelease.setInputFile(addManifestTask.outputs.files.files[0])
            zipalignRelease.inputs.file(addManifestTask.outputs.files.files[0])

            addManifestTask.doLast{
                addRapierMF.init(o)
                addRapierMF.execute(it.inputs.files.files[0],it.outputs.files.files[0])
            }

            Configuration conf = o.configurations.findByName("bundle");
            DependencySet deps = conf.getDependencies();
            List<String> list = new ArrayList<>()
            for (Dependency dep : deps) {
                list.add(dep.group + ":" + dep.name + ":" + dep.version)
            }
            Collections.sort(list)
            prepareBundleTask.inputs.property("bundle", list.toString())
            RapierExtention rapierExtention = o.extensions.findByName('rapier');
            prepareBundleJsonTask.inputs.property("slink", rapierExtention.staticLink.toString())


            installRapierTask.finalizedBy o.tasks.findByName('installRelease')
            assemableRapierTask.finalizedBy o.tasks.findByName('assembleRelease')

            Task mergeAssetTask = o.tasks.findByName('mergeReleaseAssets')
            mergeAssetTask.inputs.file(o.file(RapierConstant.BUNDLE_JSON).parentFile)

            o.tasks.matching { it.name.startsWith("lintVital") }.each {
                Log.log("RapierPlugin", "disable lint " + it.name)
                it.enabled = false
            }

        }
    }

    void makePrepareDependencyTask(Task prepareDependencyTask, Project o) {

        prepareDependencyTask.doLast {
//            Log.log("RapierPlugin","prepareDependencyTask")

            BaseMedusaTask prepareTask = BaseMedusaTask.regist(o, PrepareDependencyTask.class);
            prepareTask.execute(o.file(RapierConstant.TEMP_PROPERTY), null)
            List<String> list = prepareTask.getResult()

            for (String dep : list) {
                o.getDependencies().add('medusa', dep + "@apk")
                o.getDependencies().add('medusa', dep + ":AndroidManifest@xml")
            }

            Configuration bundleConf = o.configurations.getByName('medusa')
            RapierExtention rapierExtention = project.extensions.findByName('rapier');
            Task mergeJniLib = o.tasks.findByName("mergeReleaseJniLibFolders")
            bundleConf.files.each {
                if (it.name.endsWith('.apk')) {
                    mergeJniLib.inputs.file(it)
                }
            }
            mergeJniLib.inputs.property("slink", rapierExtention.staticLink.toString())

            mergeJniLib.doLast {
                File file = null
                mergeJniLib.outputs.files.files.each {
                    if (it.absolutePath.contains('intermediates/jniLibs/release'))
                        file = it
                }
                Log.log("RapierPlugin", 'mergeBundleToLibTask copy to ' + file.absolutePath)

                ResolvedConfiguration resolvedConfiguration = bundleConf.getResolvedConfiguration();

                if (rapierExtention.staticLink != null) {
                    resolvedConfiguration.resolvedArtifacts.each {
                        ModuleComponentIdentifier componentIdentifier = it.id.componentIdentifier
                        String prefix = componentIdentifier.group + ":" + componentIdentifier.module
                        if (it.extension.equals("apk")) {
                            if (rapierExtention.staticLink.contains(prefix)) {//slink bundle remove classes.dex

                                String path = it.file.absolutePath
                                String soName = "lib" + it.file.name.substring(0, it.file.name.length() - 4) + ".so"

                                Task soZip = o.tasks.create(path + "-soZip", org.gradle.api.tasks.bundling.Zip)


                                FileTree tree = project.zipTree(path)

                                soZip.from(tree)
//                                soZip.exclude("AndroidManifest.xml")
//                                soZip.exclude("META-INF/*")
                                soZip.exclude("classes.dex")
                                soZip.archiveName = soName
                                soZip.destinationDir = project.file(file.absolutePath + "/armeabi/")
                                soZip.execute()

                            } else {
                                String name = it.file.name
                                String path = it.file.absolutePath
                                String soName = "lib" + it.file.name.substring(0, it.file.name.length() - 4) + ".so"

                                o.copy {
                                    from path
                                    into file.absolutePath + "/armeabi/" // 目标位置
                                    rename(name, soName)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void makePrepareBundleTask(Task prepareBundleTask, Project o) {
        prepareBundleTask.inputs.files(RapierConstant.LOCAL_PROPERTY)
        prepareBundleTask.outputs.file(RapierConstant.TEMP_PROPERTY)

        prepareBundleTask.doLast {
            Log.log("RapierPlugin", "prepareBundle")
            BaseMedusaTask prepareTask = BaseMedusaTask.regist(o, PrepareBundleTask.class);
            prepareTask.init(o)
            prepareTask.execute(o.projectDir, o.file(RapierConstant.TEMP_PROPERTY))
        }
    }

    void makePrepareBundleJsonTask(Task task, Project o) {
        task.inputs.files(RapierConstant.TEMP_PROPERTY)
        task.outputs.file(RapierConstant.BUNDLE_JSON)

        task.doLast {
            String json = BundleUtil.readBundleProperty(o, o.file(RapierConstant.TEMP_PROPERTY))
            File file = new File(RapierConstant.BUNDLE_JSON)
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs()
            FileWriter writer = new FileWriter(file)
            writer.write(json)
            writer.flush()
            writer.close()
            Log.log("RapierPlugin", "hookMergeAssets add bundle.json to" + file.absolutePath)
            Log.log("RapierPlugin", "bundle.json:" + json)
            Task mergeAssetTask = o.tasks.findByName('mergeReleaseAssets')
            mergeAssetTask.inputs.file(o.file(RapierConstant.BUNDLE_JSON).parentFile)
        }
    }

    void hookProcessManifest(Project project, Task rTask) {
        BaseTask.class.getDeclaredFields().find {
            it.name.equals("androidBuilder")
        }.each {
            Log.log("RapierPlugin", 'hookProcessManifest')
            project.configurations.getByName('medusa').files.each {
                if (it.absolutePath.endsWith(".xml")) {
                    rTask.inputs.file(it)
                }
            }
            it.setAccessible(true)
            AndroidBuilder originBuilder = it.get(rTask);
            MedusaAndroidBuilder mBuilder = new MedusaAndroidBuilder(originBuilder, android, rTask, project);
            it.set(rTask, mBuilder)
        }
    }

    void hookDex(Project project, Task task) {
        TransformTask transformTask = task;
        RapierExtention rapierExtention = project.extensions.findByName('rapier');

        task.inputs.property("slink", rapierExtention.staticLink.toString())
        DexTransform dexTransform = transformTask.transform;

        Configuration bundleConf = project.configurations.getByName('medusa')

        ResolvedConfiguration resolvedConfiguration = bundleConf.getResolvedConfiguration();

        if (rapierExtention.staticLink != null) {
            resolvedConfiguration.resolvedArtifacts.each {
                ModuleComponentIdentifier componentIdentifier = it.id.componentIdentifier
                String prefix = componentIdentifier.group + ":" + componentIdentifier.module
                if (rapierExtention.staticLink.contains(prefix) && it.extension.equals("apk")) {
                    task.inputs.file(it.file)
                }
            }
        }

        DexTransform.class.getDeclaredFields().find {
            it.name.equals("androidBuilder")
        }.each {
            Log.log("RapierPlugin", 'hookDex')

            it.setAccessible(true)
            AndroidBuilder originBuilder = it.get(dexTransform);
            MedusaAndroidBuilder mBuilder = new MedusaAndroidBuilder(originBuilder, android, task, project);
            it.set(dexTransform, mBuilder)
        }

    }
}