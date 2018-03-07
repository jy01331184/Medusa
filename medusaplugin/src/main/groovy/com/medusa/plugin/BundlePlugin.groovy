package com.medusa.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.api.ApkVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.dsl.AaptOptions
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.tasks.MergeResources
import com.android.build.gradle.tasks.ZipAlign
import com.android.builder.core.AndroidBuilder
import com.medusa.BundleConstant
import com.medusa.HookedDependencySet
import com.medusa.MedusaAndroidBuilder
import com.medusa.RapierConstant
import com.medusa.model.BundleExtention
import com.medusa.task.AddBundleMFTask
import com.medusa.task.BaseMedusaTask
import com.medusa.util.Log
import com.medusa.util.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.DefaultDependencySet
import org.gradle.api.internal.artifacts.DefaultPublishArtifactSet
import org.gradle.api.internal.artifacts.configurations.DefaultConfiguration
import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import org.gradle.api.tasks.Upload
import org.gradle.jvm.tasks.Jar

import java.lang.reflect.Field
import java.text.SimpleDateFormat

public class BundlePlugin implements Plugin<Project> {

    TestedExtension android;

    static final String LOCAL_VERSION = '0.0.0'
    static final String REMOTE_MAVEN_URL = "http://localhost:8081/repository/maven-releases/"

    @Override
    void apply(Project o) {
        android = o.extensions.findByName("android")
        Map<String, String> plugins = new HashMap<>()
        plugins.put('plugin', 'maven')
        o.apply(plugins)
        o.extensions.create('bundle', BundleExtention)
        BundleConstant.INIT(o)

        BaseMedusaTask addBundleMF = BaseMedusaTask.regist(o, AddBundleMFTask.class);

        println("bundle plugin:" + RapierConstant.PLUGIN_VERSION)
        o.configurations.create('bundle')
        o.configurations.create('bundleApk')

        makePublicXml(o)
        makeAaptParms(o)

        Task bundleTask = o.task('assembleBundle')
        Task assembleRelease = o.tasks.findByName('assembleRelease')

        //bundleTask.outputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-release.apk")

        Task aaptPrepare = o.task("aapt-pkg")
        aaptPrepare.doLast {
            BundleExtention bundleExtention = project.extensions.findByName('bundle')
            def packageId = bundleExtention == null ? 0x7f : bundleExtention.packageId
            android.aaptOptions.additionalParameters.add('--package-id')
            android.aaptOptions.additionalParameters.add(String.valueOf(packageId))
            android.aaptOptions.additionalParameters.add('-x')
//            android.aaptOptions.additionalParameters.add('--non-constant-id')
        }
        bundleTask.dependsOn aaptPrepare
        bundleTask.dependsOn assembleRelease

        Task addMfTask = o.task('BundleAddMfTask')

        o.afterEvaluate {
            Task packageRelease = o.tasks.findByName('packageRelease')
            ZipAlign zipalignRelease = o.tasks.findByName('zipalignRelease')

            bundleTask.outputs.file(zipalignRelease.outputs.files.files[0])

            addMfTask.inputs.file(packageRelease.outputs.files.files.getAt(0))
            addMfTask.inputs.file(o.projectDir.absolutePath + "/build.gradle")
            File finalDir = o.file(o.buildDir.absolutePath + "/outputs/apk/")
            finalDir.mkdirs()
            addMfTask.outputs.file(finalDir.absolutePath + "/" + o.name + "-bundle-unaligned.apk")
            zipalignRelease.setInputFile(addMfTask.outputs.files.files[0])
            zipalignRelease.inputs.file(addMfTask.outputs.files.files[0])
            addMfTask.doLast {
                addBundleMF.init(o)
                addBundleMF.execute(packageRelease.outputs.files.files.getAt(0), it.outputs.files.files.getAt(0))
            }
            packageRelease.finalizedBy addMfTask
            BundleExtention bundleExtention = o.extensions.findByName('bundle')

            if (bundleExtention == null)
                throw new RuntimeException("no bundle extention in build.gradle")
            bundleExtention.vertify()
            makeInstallBundleLocal(o, bundleTask, bundleExtention)
            makeInstallBundleRemote(o, bundleTask, bundleExtention)

            Upload upload = o.tasks.findByName('uploadBundle')
            if (upload == null) {
                upload = o.tasks.create('uploadBundle', Upload)
            }

            upload.configuration = o.configurations.findByName('archives')

            Task processResTask = o.tasks.findByName("processReleaseResources")
            hookProcessResource(o, processResTask)

            o.tasks.matching { it.name.startsWith("lintVital") }.each {
                Log.log("BundlePlugin", "disable lint " + it.name)
                it.enabled = false
            }
            def minify = true
            android.buildTypes.each {
                if (it.name.equals("release")) {
                    minify = it.minifyEnabled
                }
            }

            Task transformDex = o.tasks.findByName('transformClassesWithDexForRelease')

            if (!minify && transformDex != null) {
                Task mergeJarTask = o.task("mergeJar")

                transformDex.inputs.files.files.each {
                    if (it.isDirectory() || !it.exists()) {
                        mergeJarTask.inputs.file(it)
                    } else {
                        mergeJarTask.inputs.files.add(o.zipTree(it))
                    }
                }

                mergeJarTask.outputs.file(new File(o.buildDir.absolutePath + "/jars/main.jar"))


                mergeJarTask.doLast {
                    Jar domerge = o.tasks.create('domerge', Jar)
                    domerge.baseName = 'main'
                    domerge.destinationDir = (new File(o.buildDir.absolutePath + "/jars"))
                    domerge.from(it.inputs.files.files)
                    domerge.execute()
                }

                Log.log("BundlePlugin", "add mergeJarTask to uploadBundle")
                upload.dependsOn.add(mergeJarTask)
            }


            upload.doFirst {
                Date date = new Date()
                File apkFile = bundleTask.outputs.files.files.getAt(0)
                DefaultPublishArtifactSet ass = upload.configuration.getArtifacts()
                ass.clear()
                DefaultPublishArtifact apkArtifact = new DefaultPublishArtifact(bundleExtention.name, 'apk', 'apk', '', date, apkFile, new Object[0])
                ass.add(apkArtifact)
                Log.log("installBundle", "add apk path:" + apkFile.absolutePath)

                Task processManifest = o.tasks.findByName('processReleaseManifest')
                File manifestFile = null
                processManifest.outputs.files.files.each {
                    if (manifestFile == null && it.name.equals("AndroidManifest.xml")){
                        if(it.exists()){
                            manifestFile = it
                        }
                    }
                }

                DefaultPublishArtifact manifestArtifact = new DefaultPublishArtifact(bundleExtention.name, 'xml', 'xml', 'AndroidManifest', date, manifestFile, new Object[0])
                ass.add(manifestArtifact)
                Log.log("installBundle", "add manifest path:" + manifestFile.absolutePath)

                if (minify) {
                    transformDex.inputs.files.files[0].eachFileRecurse {
                        if (it.name.endsWith(".jar")) {
                            DefaultPublishArtifact jarFileArtifact = new DefaultPublishArtifact(bundleExtention.name, 'jar', 'jar', '', date, it, new Object[0])
                            ass.add(jarFileArtifact)
                            Log.log("installBundle", "add jar path:" + it.absolutePath)
                        }
                    }
                } else {
                    DefaultPublishArtifact jarFileArtifact = new DefaultPublishArtifact(bundleExtention.name, 'jar', 'jar', '', date, new File(o.buildDir.absolutePath + "/jars/main.jar"), new Object[0])
                    ass.add(jarFileArtifact)
                    Log.log("installBundle", "add jar path:" + o.buildDir.absolutePath + "/jars/main.jar")
                }
                Log.log('BundlePlugin', 'upload version:' + upload.repositories.mavenDeployer.pom.version)
            }

            o.tasks.findByName('clean').doLast {
                Log.log("BundlePlugin", "clean " + o.buildDir.absolutePath + "/jars")
                o.delete o.fileTree(o.buildDir.absolutePath + "/jars") {}
            }
        }

    }

    private MedusaAndroidBuilder hookProcessResource(Project project, Task rTask) {
        MedusaAndroidBuilder mBuilder
        BundleExtention bundleExtention = project.extensions.findByName('bundle')
        rTask.inputs.property('bundle', bundleExtention.toString())

        BaseTask.class.getDeclaredFields().find {
            it.name.equals("androidBuilder")
        }.each {
            it.setAccessible(true)
            AndroidBuilder originBuilder = it.get(rTask);
            mBuilder = new MedusaAndroidBuilder(originBuilder, android, rTask, project);
            it.set(rTask, mBuilder)
        }

        return mBuilder
    }

    private void makeInstallBundleRemote(Project project, Task bundleTask, BundleExtention bundleModel) {
        Task installBundleRemoteTask = project.task('installBundleRemote')
        installBundleRemoteTask.group = 'bundle'
        installBundleRemoteTask.finalizedBy bundleTask

        DefaultMavenFileLocations defaultMavenFileLocations = new DefaultMavenFileLocations()
        DefaultMavenSettingsProvider defaultMavenSettingsProvider = new DefaultMavenSettingsProvider(defaultMavenFileLocations)
        DefaultLocalMavenRepositoryLocator defaultLocalMavenRepositoryLocator = new DefaultLocalMavenRepositoryLocator(defaultMavenSettingsProvider)

        Upload upload = project.tasks.findByName('uploadBundle')

        installBundleRemoteTask.doFirst {
            Log.log('BundlePlugin', 'installBundleRemote:' + defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath)
            String finalVersion = bundleModel.version + (bundleModel.autoVersion ? "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) : "")
            upload.repositories {
                mavenDeployer {
                    repository(url: (Utils.isEmpty(bundleModel.mavenUrl) ? REMOTE_MAVEN_URL : bundleModel.mavenUrl)) {
                        authentication(userName: "admin", password: "123")
                    }
                    pom.version = finalVersion
                    pom.artifactId = bundleModel.name
                    pom.groupId = bundleModel.groupId
                }
            }
        }

        installBundleRemoteTask.finalizedBy upload
    }

    private void makeInstallBundleLocal(Project project, Task bundleTask, BundleExtention bundleModel) {
        Task installBundleLocalTask = project.task('installBundleLocal')
        installBundleLocalTask.group = 'bundle'

        Task changeVersionTask = project.task('a-changeLocalVersion')
        changeVersionTask.doLast {
            bundleModel.version = LOCAL_VERSION
        }

        installBundleLocalTask.dependsOn changeVersionTask
        installBundleLocalTask.dependsOn bundleTask

        DefaultMavenFileLocations defaultMavenFileLocations = new DefaultMavenFileLocations()
        DefaultMavenSettingsProvider defaultMavenSettingsProvider = new DefaultMavenSettingsProvider(defaultMavenFileLocations)
        DefaultLocalMavenRepositoryLocator defaultLocalMavenRepositoryLocator = new DefaultLocalMavenRepositoryLocator(defaultMavenSettingsProvider)

        Upload upload = project.tasks.findByName('uploadBundle')

        installBundleLocalTask.doFirst {
            bundleModel.version = LOCAL_VERSION
            Log.log('BundlePlugin', 'installBundleLocal:' + defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath)
            upload.repositories {
                mavenDeployer {
                    repository(url: project.uri(defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath))
                    pom.version = bundleModel.version
                    pom.artifactId = bundleModel.name
                    pom.groupId = bundleModel.groupId
                }
            }
        }

        installBundleLocalTask.finalizedBy upload
    }

    private void makePublicXml(Project project) {
        project.afterEvaluate {
            VariantScope scope;
            if (android instanceof AppExtension) {
                android.applicationVariants.each {
                    ApkVariantImpl variant = it;
                    scope = variant.getVariantData().getScope()
                }
            } else if (android instanceof LibraryExtension) {
                android.libraryVariants.each {
                    LibraryVariantImpl variant = it;
                    scope = variant.getVariantData().getScope()
                }
            }

            String mergeTaskName = scope.getMergeResourcesTask().name
            MergeResources mergeTask = project.tasks.getByName(mergeTaskName)
            mergeTask.doLast {
                project.copy {
                    int i = 0
                    from(android.sourceSets.main.res.srcDirs) {
                        include 'values/public.xml'
                        rename 'public.xml', (i++ == 0 ? "public.xml" : "public_${i}.xml")
                    }
                    into(mergeTask.outputDir)
                }
            }
        }
    }

    private void makeAaptParms(Project project) {

        Configuration bundleConf = project.configurations.getByName('bundle')

        DefaultDependencySet hookedSet = new HookedDependencySet(project, bundleConf.dependencies)
        Field filed = DefaultConfiguration.class.getDeclaredField('dependencies')
        filed.setAccessible(true)
        filed.set(bundleConf, hookedSet)

        project.afterEvaluate {
            AaptOptions opt = android.aaptOptions
            project.configurations.each {
                if (it.name.startsWith("bundleApk")) {
                    List<String> parms = new ArrayList<>()
                    it.files.each {
                        parms.add('-I')
                        parms.add(it.absolutePath)
                    }
                    opt.setAdditionalParameters(parms)
                }
            }
            Log.log('BundlePlugin', 'add aapt parm:' + opt.additionalParameters)
        }
    }
}