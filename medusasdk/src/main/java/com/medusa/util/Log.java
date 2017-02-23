package com.medusa.util;

/**
 * Created by tianyang on 16/8/3.
 */
public class Log {

    public static boolean Debug = true;

    public static void log(Object caller,String str)
    {
        if(Debug)
            System.out.println("    ["+caller+"] :"+str);
    }

    public static void error(Object caller,String str)
    {
        System.err.println("    ["+caller+"] :"+str);
    }
}
