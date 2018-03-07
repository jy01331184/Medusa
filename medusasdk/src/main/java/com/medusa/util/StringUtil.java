package com.medusa.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by tianyang on 18/3/1.
 */
public class StringUtil {
    public static <T> String collection2String(Collection<T> collection) {
        String strRet = "";
        if (null != collection && !collection.isEmpty()) {
            StringBuilder builder = new StringBuilder("[size=" + collection.size() + ": ");
            Iterator index = collection.iterator();

            while (index.hasNext()) {
                Object t = index.next();
                if (null != t) {
                    builder.append(t.toString()).append(",");
                }
            }

            int index1 = builder.lastIndexOf(",");
            if (-1 != index1) {
                builder.deleteCharAt(index1);
            }

            builder.append("]");
            strRet = builder.toString();
        }

        return strRet;
    }
}
