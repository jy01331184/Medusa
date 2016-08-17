package com.medusa.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by tianyang on 16/8/1.
 */
public class Utils {

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
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
}
