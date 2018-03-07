package com.medusa.model;

import org.json.JSONObject;

/**
 * Created by tianyang on 16/8/10.
 */
public class BundleProperty {

    public String artifactId;
    public String groupId;
    public String version;
    public String path;
    public boolean slink;


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("groupId", groupId);
        jsonObject.put("artifactId", artifactId);
        jsonObject.put("version", version);
        jsonObject.put("path", path);
        jsonObject.put("slink", slink);

        return jsonObject;
    }
}

