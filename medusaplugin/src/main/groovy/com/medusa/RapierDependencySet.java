package com.medusa;

import com.medusa.util.Log;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.DelegatingDomainObjectSet;
import org.gradle.api.internal.artifacts.DefaultDependencySet;

import java.lang.reflect.Field;

/**
 * Created by tianyang on 17/2/8.
 */
public class RapierDependencySet extends DefaultDependencySet {

    Project project;

    public RapierDependencySet(Project project, DefaultDependencySet origin) {
        super(origin.toString(), (DomainObjectSet<Dependency>) v(origin,"backingSet"));
        this.project = project;
    }

    @Override
    public boolean add(Dependency o) {
        String pro = o.getGroup()+":"+o.getName()+":"+o.getVersion()+"@jar";
        Log.log("RapierPlugin","add provided "+pro);
        project.getDependencies().add("provided",pro);
        return super.add(o);
    }

    private static <T> T v(Object obj, String name) {
        try {
            Field field = DelegatingDomainObjectSet.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
