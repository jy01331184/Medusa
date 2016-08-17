package com.medusa.task;

import org.gradle.api.Project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyang on 16/8/4.
 */
public abstract class BaseMedusaTask {

    protected Project project;
    private static Map<String,BaseMedusaTask> caches = new HashMap<String,BaseMedusaTask>();

    public BaseMedusaTask init(Project project)
    {
        this.project = project;
        return this;
    }

    public Object getResult()
    {
        return null;
    }

    public abstract void execute(File input,File output);

    public static <T> T regist(Project project,Class<T> cls)
    {
        try {
            if(caches.containsKey(project.toString()+cls.getName()))
                return (T) caches.get(project.toString()+cls.getName());
            Object obj = cls.newInstance();
            caches.put(project.toString()+cls.getName(), (BaseMedusaTask) obj);
            return (T) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T getResult(Project project,Class<T> cls)
    {
        return (T) caches.get(project.toString()+cls.getName()).getResult();
    }
}
