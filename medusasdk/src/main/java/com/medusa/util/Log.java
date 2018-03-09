package com.medusa.util;

/**
 * Created by tianyang on 16/8/3.
 */
public class Log {

    public static boolean Debug = true;

    public static void info(Object caller, String str)
    {
        if(Debug)
            System.out.println("["+caller+"] :"+str);
    }

    public static void error(Object caller,String str)
    {
        System.err.println("["+caller+"] :"+str);
    }

    public static void error(Object caller,String str,Throwable e)
    {
        System.err.println("["+caller+"] :"+str);
        e.printStackTrace(System.err);
    }
}
