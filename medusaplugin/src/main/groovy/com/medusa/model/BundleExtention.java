package com.medusa.model;

import com.medusa.util.Utils;

/**
 * Created by tianyang on 17/5/4.
 */
public class BundleExtention {

    public String name;
    public int packageId = 27;
    public int priority = 0;

    public String exportPackages;
    public String version;
    public String groupId;

    public void vertify(){

        if(Utils.isEmpty(name))
            throw new RuntimeException("no bundle name in bundle extention");
        if(Utils.isEmpty(groupId))
            throw new RuntimeException("no bundle groupId in bundle extention");
        if(Utils.isEmpty(version))
            throw new RuntimeException("no bundle version in bundle extention");
    }
}
