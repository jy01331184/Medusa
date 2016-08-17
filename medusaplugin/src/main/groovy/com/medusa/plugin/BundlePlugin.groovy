package com.medusa.plugin

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.builder.core.AndroidBuilder
import com.medusa.MedusaAndroidBuilder
import com.medusa.model.BundleModel
import com.medusa.task.AddBundleMFTask
import com.medusa.task.BaseMedusaTask
import com.medusa.task.ReadBundlePropTask
import com.medusa.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.artifacts.DefaultPublishArtifactSet
import org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import org.gradle.jvm.tasks.Jar

public class BundlePlugin implements Plugin<Project> {

    TestedExtension android;

    BaseMedusaTask readBundleProp ;
    BaseMedusaTask addBundleMF ;

    static final String LOCAL_VERSION = '0.0.0'

    @Override
    void apply(Project o) {
        android = o.extensions.findByName("android")
        readBundleProp = BaseMedusaTask.regist(o,ReadBundlePropTask.class)
        addBundleMF = BaseMedusaTask.regist(o,AddBundleMFTask.class);
        readBundleProp.init(o)
        readBundleProp.execute(null, null)
        BundleModel bundleModel = readBundleProp.getResult()
        println("bundle:"+bundleModel+'-->'+this.toString())


        Task bundleTask = o.task('assembleBundle')
        Task readBundlePropTask = o.task('readBundlePropTask')
        readBundlePropTask.finalizedBy o.tasks.findByName('assembleRelease')
        bundleTask.outputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-bundle-release.apk")
        bundleTask.dependsOn.add(readBundlePropTask)

        Task addMfTask = o.task('BundleAddMfTask')

        addMfTask.inputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-release.apk")
        addMfTask.inputs.file(o.projectDir.absolutePath + "/bundle.properties")
        addMfTask.inputs.file(o.projectDir.absolutePath + "/build.gradle")
        addMfTask.outputs.file(o.buildDir.absolutePath + "/outputs/apk/" + o.name + "-bundle-release.apk")
        addMfTask.doLast {
            addBundleMF.init(o)
            addBundleMF.execute(it.inputs.files.files.getAt(0), it.outputs.files.files.getAt(0))
        }
        bundleTask.finalizedBy addMfTask

        makeInstallBundleLocal(o, bundleTask,bundleModel)
        makeInstallBundleRemote(o, bundleTask,bundleModel)
        o.afterEvaluate {
            hookProcessResource(o, o.tasks.findByName("processReleaseResources"))
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
                DefaultPublishArtifact apkArtifact = new DefaultPublishArtifact(bundleModel.name, 'apk', 'apk', '', date, apkFile, new Object[0])
                ass.add(apkArtifact)
                Log.log("installBundle", "add apk path:" + apkFile.absolutePath)

                File manifestFile = new File(o.buildDir.absolutePath + "/intermediates/manifests/full/release/AndroidManifest.xml")
                DefaultPublishArtifact manifestArtifact = new DefaultPublishArtifact(bundleModel.name, 'xml', 'xml', 'AndroidManifest', date, manifestFile, new Object[0])
                ass.add(manifestArtifact)
                Log.log("installBundle", "add manifest path:" + manifestFile.absolutePath)

                if (minify) {
                    transformDex.inputs.files.files[0].eachFileRecurse {
                        if (it.name.endsWith(".jar")) {
                            DefaultPublishArtifact jarFileArtifact = new DefaultPublishArtifact(bundleModel.name, 'jar', 'jar', '', date, it, new Object[0])
                            ass.add(jarFileArtifact)
                            Log.log("installBundle", "add jar path:" + it.absolutePath)
                        }
                    }
                } else {
                    DefaultPublishArtifact jarFileArtifact = new DefaultPublishArtifact(bundleModel.name, 'jar', 'jar', '', date, new File(o.buildDir.absolutePath + "/jars/main.jar"), new Object[0])
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

    private MedusaAndroidBuilder hookProcessResource(project, rTask) {
        MedusaAndroidBuilder mBuilder

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

    private void makeInstallBundleRemote(Project project, Task bundleTask,BundleModel bundleModel) {
        Task installBundleRemoteTask = project.task('installBundleRemote')
        installBundleRemoteTask.group = 'bundle'
        installBundleRemoteTask.dependsOn.add(bundleTask)
        Task uploadMaven = project.tasks.findByName('uploadArchives')
        installBundleRemoteTask.finalizedBy uploadMaven

        DefaultMavenFileLocations defaultMavenFileLocations = new DefaultMavenFileLocations()
        DefaultMavenSettingsProvider defaultMavenSettingsProvider = new DefaultMavenSettingsProvider(defaultMavenFileLocations)
        DefaultLocalMavenRepositoryLocator defaultLocalMavenRepositoryLocator = new DefaultLocalMavenRepositoryLocator(defaultMavenSettingsProvider)

        installBundleRemoteTask.doFirst {
            Log.log('BundlePlugin', 'installBundleRemote:' + defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath)
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: project.uri(defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath))
                        pom.version = bundleModel.version
                        pom.artifactId = bundleModel.name
                        pom.groupId = bundleModel.group
                    }
                }
            }
        }
    }

    private void makeInstallBundleLocal(Project project, Task bundleTask,BundleModel bundleModel) {
        Task installBundleLocalTask = project.task('installBundleLocal')
        installBundleLocalTask.group = 'bundle'
        installBundleLocalTask.dependsOn.add(bundleTask)
        Task uploadMaven = project.tasks.findByName('uploadArchives')
        installBundleLocalTask.finalizedBy uploadMaven

        DefaultMavenFileLocations defaultMavenFileLocations = new DefaultMavenFileLocations()
        DefaultMavenSettingsProvider defaultMavenSettingsProvider = new DefaultMavenSettingsProvider(defaultMavenFileLocations)
        DefaultLocalMavenRepositoryLocator defaultLocalMavenRepositoryLocator = new DefaultLocalMavenRepositoryLocator(defaultMavenSettingsProvider)

        installBundleLocalTask.doFirst {
            Log.log('BundlePlugin', 'installBundleLocal:' + defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath)
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: project.uri(defaultLocalMavenRepositoryLocator.localMavenRepository.absolutePath))
                        pom.version = LOCAL_VERSION
                        pom.artifactId = bundleModel.name
                        pom.groupId = bundleModel.group
                    }
                }
            }
        }
    }
}