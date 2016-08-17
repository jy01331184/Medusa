package com.medusa.util;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Created by xinwen on 15-3-27.
 */
public class MD5Util {

    public static final String TAG = "Md5Util";

    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static boolean checkMd5(InputStream is1, InputStream is2) {
        boolean bRet = false;
        if (null != is1 && null != is2) {
            String md5Sum1 = genInputStreamMd5sum(is1);
            String md5Sum2 = genInputStreamMd5sum(is2);
            bRet = !TextUtils.isEmpty(md5Sum1)
                    && !TextUtils.isEmpty(md5Sum2)
                    && TextUtils.equals(md5Sum1, md5Sum2);
        }

        return bRet;
    }

    /**
     * 生成文件的Md5摘要
     *
     * @param file
     * @return
     */
    public static String genFileMd5sum(File file) {
        if (null == file || !file.exists()) {
            return null;
        }

        try {
            InputStream fis = new FileInputStream(file);
            return genInputStreamMd5sum(fis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String genInputStreamMd5sum(InputStream is) {
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = is.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            return byteArrayToHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
