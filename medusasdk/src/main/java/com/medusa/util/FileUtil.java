package com.medusa.util;

import android.content.Context;

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
            if(!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file,false);
            byte[] buffer = new byte[8192];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
        } finally {
            if (null != fileOutputStream) {
                fileOutputStream.close();
                fileOutputStream = null;
            }
        }
    }

    public static void copyFile(FileChannel inChannel, String str) throws IOException{
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(str).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (null != outChannel) {
                outChannel.close();
                outChannel = null;
            }
        }
    }

    public static String readAssetFile(Context context, String path)
    {
        try {
            InputStream is = context.getAssets().open("bundle.json");
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            StringBuilder builder = new StringBuilder();

            String line = null;
            while( (line = bis.readLine()) != null )
                builder.append(line);
            bis.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(File file)
    {
        if(file == null || !file.exists())
            return null;
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            StringBuilder builder = new StringBuilder();

            String line = null;
            while( (line = bis.readLine()) != null )
                builder.append(line);
            bis.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeToFile(File file,String content)
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void writeToFileAsync(final File file, final String content){
        new Thread(){
            @Override
            public void run() {
                writeToFile(file,content);
            }
        }.start();
    }
}
