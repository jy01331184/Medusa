package com.medusa.plugin

import com.medusa.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

public class LinkenPlugin implements Plugin<Project> {

    @Override
    void apply(Project o) {
        o.task('prepareBundle',type:Copy){
            Log.log("LinkenPlugin",'prepareBundle')
            from o.configurations.runtime
            into 'linken' // 目标位置
        }.group = 'bundle'
    }
}