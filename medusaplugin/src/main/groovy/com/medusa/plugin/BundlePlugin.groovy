package com.medusa.plugin

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.AaptOptions
import com.android.build.gradle.internal.tasks.BaseTask
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
import org.gradle.jvm.tasks.Jar

import java.lang.reflect.Field

public class BundlePlugin implements Plugin<Project> {

    TestedExtension android;

    BaseMedusaTask addBundleMF ;

    static final String LOCAL_VERSION = '0.0.0'
    static final String REMOTE_MAVEN_URL = "http://localhost:8081/repository/maven-releases/"

    @Override
    void apply(Project o) {
        android = o.extensions.findByName("android")
        o.extensions.create('bundle',BundleExtention)
        BundleConstant.INIT(o)

        addBundleMF = BaseMedusaTask.regist(o,AddBundleMFTask.class);

        println("bundle plugin:"+RapierConstant.PLUGIN_VERSION)
        o.configurations.create('bundle')
        o.configurations.create('bundleApk')

        makePublicXml(o)
        makeAaptParms(o)

        Task bundleTask = o.task('assembleBundle')

        bundleTask.outputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-bundle-release.apk")
        bundleTask.dependsOn.add(o.tasks.findByName('assembleRelease'))

        Task addMfTask = o.task('BundleAddMfTask')

        addMfTask.inputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-release.apk")
        addMfTask.inputs.file(o.projectDir.absolutePath + "/build.gradle")
        addMfTask.outputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-bundle-release.apk")
        addMfTask.doLast {
            addBundleMF.init(o)
            addBundleMF.execute(it.inputs.files.files.getAt(0), it.outputs.files.files.getAt(0))
        }
        bundleTask.finalizedBy addMfTask

        o.afterEvaluate {
            BundleExtention bundleExtention = o.extensions.findByName('bundle')

            if(bundleExtention == null)
                throw new RuntimeException("no bundle extention in build.gradle")
            bundleExtention.vertify()
            makeInstallBundleLocal(o, bundleTask,bundleExtention)
            makeInstallBundleRemote(o, bundleTask,bundleExtention)

            Task uploadMaven = o.tasks.findByName('uploadArchives')
            addMfTask.finalizedBy uploadMaven

            Task processResTask = o.tasks.findByName("processReleaseResources")
            hookProcessResource(o,processResTask )

            o.tasks.matching {it.name.startsWith("lintVital")}.each {
                Log.log("BundlePlugin","disable lint "+it.name)
                it.enabled = false
            }
            def minify = true
            android.buildTypes.each {
                if (it.name.equals("release")) {
                    minify = it.minifyEnabled
                }
            }

            Task uploadArchivesTask = o.tasks.findByName("uploadArchives")
            Task transformDex = o.tasks.findByName('transformClassesWithDexForRelease')

            if (!minify) {
                Task mergeJarTask = o.task("mergeJar", type: Jar) {
                    baseName = 'main'
                    from {
                        transformDex.inputs.files.files.collect {
                            it.isDirectory() ? it : o.zipTree(it)
                        }
                    }
                    setDestinationDir(new File(o.buildDir.absolutePath + "/jars"))
                }
                Log.log("BundlePlugin","add mergeJarTask to uploadArchives")
                uploadArchivesTask.dependsOn.add(mergeJarTask)
            }

            uploadArchivesTask.doFirst {
                Date date = new Date()
                File apkFile = bundleTask.outputs.files.files.getAt(0)
                DefaultPublishArtifactSet ass = it.configuration.getArtifacts()
                ass.clear()
                DefaultPublishArtifact apkArtifact = new DefaultPublishArtifact(bundleExtention.name, 'apk', 'apk', '', date, apkFile, new Object[0])
                ass.add(apkArtifact)
                Log.log("installBundle", "add apk path:" + apkFile.absolutePath)

                File manifestFile = new File(o.buildDir.absolutePath + "/intermediates/manifests/full/release/AndroidManifest.xml")
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
            }

            o.tasks.findByName('clean').doLast{
                Log.log("BundlePlugin","clean "+o.buildDir.absolutePath + "/jars")
                o.delete o.fileTree(o.buildDir.absolutePath + "/jars") {}
            }
        }

    }

    private MedusaAndroidBuilder hookProcessResource(Project project,Task rTask) {
        MedusaAndroidBuilder mBuilder

        rTask.inputs.file(project.file(project.projectDir.absolutePath + "/build.gradle"))

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

    private void makeInstallBundleRemote(Project project, Task bundleTask,BundleExtention bundleModel) {
        Task installBundleRemoteTask = project.task('installBundleRemote')
        installBundleRemoteTask.group = 'bundle'
        installBundleRemoteTask.finalizedBy bundleTask

        DefaultMavenFileLocations defaultMavenFileLocations = new DefaultMavenFileLocations()
        DefaultMavenSettingsProvider defaultMavenSettingsProvider = new DefaultMavenSettingsProvider(defaultMavenFileLocations)
        DefaultLocalMavenRepositoryLocator defaultLocalMavenRepositoryLocator = new DefaultLocalMavenRepositoryLocator(defaultMavenSettingsProvider)

        installBundleRemoteTask.doFirst {
            Log.log('BundlePlugin', 'installBundleRemote:' + defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath)
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        //repository(url: project.uri(defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath))
                        repository (url: (Utils.isEmpty(bundleModel.mavenUrl)?REMOTE_MAVEN_URL:bundleModel.mavenUrl)){
                            authentication(userName: "admin", password: "123")
                        }
                        pom.version = bundleModel.version
                        pom.artifactId = bundleModel.name
                        pom.groupId = bundleModel.groupId
                    }
                }
            }
        }
    }

    private void makeInstallBundleLocal(Project project, Task bundleTask,BundleExtention bundleModel) {
        Task installBundleLocalTask = project.task('installBundleLocal')
        installBundleLocalTask.group = 'bundle'

        installBundleLocalTask.finalizedBy bundleTask

        DefaultMavenFileLocations defaultMavenFileLocations = new DefaultMavenFileLocations()
        DefaultMavenSettingsProvider defaultMavenSettingsProvider = new DefaultMavenSettingsProvider(defaultMavenFileLocations)
        DefaultLocalMavenRepositoryLocator defaultLocalMavenRepositoryLocator = new DefaultLocalMavenRepositoryLocator(defaultMavenSettingsProvider)

        installBundleLocalTask.doFirst {
            bundleModel.version = LOCAL_VERSION
            Log.log('BundlePlugin', 'installBundleLocal:' + defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath)
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: project.uri(defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath))
                        pom.version = bundleModel.version
                        pom.artifactId = bundleModel.name
                        pom.groupId = bundleModel.groupId
                    }
                }
            }
        }
    }

    private void makePublicXml(Project project){
        project.afterEvaluate{
            for (variant in android.applicationVariants) {
                def scope = variant.getVariantData().getScope()
                String mergeTaskName = scope.getMergeResourcesTask().name
                Task mergeTask = project.tasks.getByName(mergeTaskName)

                mergeTask.doLast {
                    project.copy {
                        int i=0
                        from(android.sourceSets.main.res.srcDirs) {
                            include 'values/public.xml'
                            rename 'public.xml', (i++ == 0? "public.xml": "public_${i}.xml")
                        }

                        into(mergeTask.outputDir)
                    }
                }
            }
        }
    }

    private void makeAaptParms(Project project){

        Configuration bundleConf = project.configurations.getByName('bundle')

        DefaultDependencySet hookedSet = new HookedDependencySet(project,bundleConf.dependencies)
        Field filed = DefaultConfiguration.class.getDeclaredField('dependencies')
        filed.setAccessible(true)
        filed.set(bundleConf,hookedSet)

        project.afterEvaluate {
            AaptOptions opt = android.aaptOptions
            project.configurations.each {
                if(it.name.startsWith("bundleApk")){
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