#!/usr/bin/python
#coding=utf-8
#向指定apk中 添加bundle.mf
import sys
import os
import stat
import datetime
import zipfile
import shutil

bundleFile = ''

if __name__ == '__main__':
    source_apk = sys.argv[1].strip()
    target_apk = sys.argv[2].strip()
    bundleFile = sys.argv[3].strip()

    # #source_apk = '/Users/tianyang/AndroidStudioProjects/MyApplication/applib2/build/outputs/apk/applib2-debug.apk'
    # #target_dir = '/Users/tianyang/AndroidStudioProjects/MyApplication/applib2/build/outputs/apk/applib2-bundle-debug.apk'


    shutil.copy(source_apk,  target_apk)
    zipped = zipfile.ZipFile(target_apk, 'a', zipfile.ZIP_DEFLATED)
    if len(sys.argv) == 4:
        zipEntryName = "META-INF/BUNDLE.MF"
    else:
        zipEntryName = sys.argv[4].strip()
    zipped.write(bundleFile, zipEntryName)
    zipped.close()
