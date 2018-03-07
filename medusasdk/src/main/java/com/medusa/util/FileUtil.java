package com.medusa.util;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.medusa.bundle.BundleConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

/**
 * Created by tianyang on 16/8/11.
 */
public class FileUtil {
    public static void copyFile(BufferedInputStream inputStream, File file) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, false);
            byte[] buffer = new byte[8192];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
        } finally {
            if (null != fileOutputStream) {
                fileOutputStream.close();
            }
        }
    }

    public static void copyFile(FileChannel inChannel, String str) throws IOException {
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(str).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (null != outChannel) {
                outChannel.close();
            }
        }
    }

    public static String readAssetFile(Context context, String path) {
        try {
            InputStream is = context.getAssets().open("bundle.json");
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            StringBuilder builder = new StringBuilder();

            String line = null;
            while ((line = bis.readLine()) != null)
                builder.append(line);
            bis.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(File file) {
        if (file == null || !file.exists())
            return null;
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            StringBuilder builder = new StringBuilder();

            String line = null;
            while ((line = bis.readLine()) != null)
                builder.append(line);
            bis.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean writeToFile(File file, String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    public static void writeToFileAsync(final File file, final BundleConfig bundleConfig) {
        new Thread() {
            @Override
            public void run() {

                String str = JSON.toJSONString(bundleConfig);
                File tempFile = new File(file.getParentFile().getAbsolutePath() + "/" + file.getName() + ".bak");
                if (writeToFile(tempFile, str)) {
                    tempFile.renameTo(file);
                    Log.log("FileUtil", "writeToFileAsync to " + file.getAbsolutePath());
                }
            }
        }.start();
    }
}
