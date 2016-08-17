#coding=utf-8
#从bundle.properties & local.properties中生成 bundle.gradle
import sys
import os
import stat
import shutil

bundleInfos = {}
localBundlePostfix = ":0.0.0"

def topStr():
	str = """/**
 * 插件自动生成 请勿手动改动
 */

buildscript {
    repositories {
        jcenter()
        mavenLocal()
    }
    dependencies {
        classpath 'com.medusa.plugin:medusaplugin:1.0.0'
    }
}

allprojects {
    repositories {
        jcenter()
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'medusa.linken'
dependencies {
"""
	return str

if __name__ == '__main__':
	if len(sys.argv) < 5:
		raise ValueError("参数不对 bundleFile localFile outputFile,topStr,mergeFile")
	bundleFile = sys.argv[1].strip()
	localFile = sys.argv[2].strip()
	outputFile = sys.argv[3].strip()
	
	

	gradleStr = ""
	if len(sys.argv) > 4:
		gradleStr = sys.argv[4].strip()
	mergeFile = sys.argv[5].strip()

	if not os.path.exists(bundleFile):
		raise ValueError("bundle.properties not exsit in rapier")

	bundleFileInput = open(bundleFile)
	for line in bundleFileInput:
		content = line.strip().replace("\n","")
		if not content.startswith('#') and len(content) > 0:
			tempInfos = content.split(':')
			groupId = tempInfos[0]
			artifactId = tempInfos[1]
			bundleKey = groupId+":"+artifactId
			bundleInfos[bundleKey] = content
	bundleFileInput.close()
	if os.path.exists(localFile):
		localFileInput = open(localFile)
		for line in localFileInput:
			content = line.strip()
			if not content.startswith('#') and len(content) > 0:
				tempInfos = content.split(':')
				groupId = tempInfos[0]
				artifactId = tempInfos[1].replace("\n","")
				bundleKey = groupId+":"+artifactId
				bundleInfos[bundleKey] = content+localBundlePostfix
		localFileInput.close()
	#generate bundle.gradle
	mergeFileDir = os.path.normpath(os.path.join(mergeFile, os.path.pardir))
	if not os.path.exists(mergeFileDir):
			os.makedirs(mergeFileDir)
		
	gradleFileInput = open(outputFile,"w")

	mergeFileInput = open(mergeFile,"w")
	if len(gradleStr) > 0 :
		gradleFileInput.write(gradleStr+"\n")
	else :
		gradleFileInput.write(topStr())
	for (k,v) in bundleInfos.items():
		gradleFileInput.write("\tcompile '"+v+"@apk'\n")
		gradleFileInput.write("\tcompile '"+v+":AndroidManifest@xml'\n")
		mergeFileInput.write(v+"\n")
	gradleFileInput.write("}")
	gradleFileInput.close()
	mergeFileInput.close()



	print bundleInfos





