package com.medusa.model;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by tianyang on 16/8/10.
 */
public class BundleProperty {

    public String artifactId;
    public String groupId;
    public String version;

    public String path;
    public List<String> activities;

    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("groupId",groupId);
        jsonObject.put("artifactId",artifactId);
        jsonObject.put("version",version);
        jsonObject.put("path",path);
        jsonObject.put("activities",activities);

        return jsonObject;
    }
}

