package com.medusa.util;

import com.google.gson.Gson;

/**
 * Created by tianyang on 16/8/12.
 */
public class GsonUtil {

    private static Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }
}
