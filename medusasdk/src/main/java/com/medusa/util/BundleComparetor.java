package com.medusa.util;

import com.medusa.bundle.Bundle;

import java.util.Comparator;

/**
 * Created by tianyang on 16/8/15.
 */
public class BundleComparetor implements Comparator<Bundle> {

    @Override
    public int compare(Bundle lhs, Bundle rhs) {
        if(lhs == null && rhs == null)
            return 0;
        if(lhs == null)
            return -1;
        if(rhs == null)
            return 1;

        return lhs.priority - rhs.priority;
    }

}
