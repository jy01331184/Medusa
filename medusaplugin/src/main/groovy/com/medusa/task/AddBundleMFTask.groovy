package com.medusa.task

import com.medusa.model.BundleExtention
import com.medusa.util.BundleUtil
import com.medusa.util.Log
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.json.JSONObject

/**
 * Created by tianyang on 16/8/3.
 */
public class AddBundleMFTask extends BaseMedusaTask{

    File manifestFile

    @Override
    BaseMedusaTask init(Project project) {
        super.init(project)
        project.tasks.findByName('processReleaseManifest').outputs.files.files.each {
            if(it.name.equals("AndroidManifest.xml"))
                manifestFile = it
        }

        return this
    }

    public void execute(File input, File output)
    {
        if(input != null && input.exists())
        {
            Log.log(this,"add Bundle.MF to "+input)

            if(output.exists())
                output.delete();


            String pyBaseDir,pyPath;

            if(project.parent != null)
                pyBaseDir = project.parent.projectDir.absolutePath+'/tools'
            else
                pyBaseDir = project.projectDir.absolutePath+'/tools'
            pyPath = pyBaseDir +"/mf.py"

            File bundleFile = writeBundleMf(input,output)
            def mArgs = [input.absolutePath,output.absolutePath,bundleFile.absolutePath]
            Log.log(this,'mf.py:'+pyPath+" args:"+mArgs)

            project.exec {
                workingDir pyBaseDir
                executable = 'chmod'
                args = ['777','mf.py']
            }

            project.exec {
                executable = pyPath
                args = mArgs
            }
        }
        else{
            Log.log(this,"add Bundle.MF fail, target: " + input)
        }
    }

    private File writeBundleMf(File input,File output)
    {

        BundleExtention bundleExtention = project.extensions.findByName('bundle')

        Configuration depConfig = project.configurations.getByName("bundle")
        File file = new File(output.getParent(),'BUNDLE.MF')

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))
        bufferedWriter.writeLine("name="+bundleExtention.name)
        bufferedWriter.writeLine("group="+bundleExtention.groupId)
        bufferedWriter.writeLine("version="+bundleExtention.version)
        bufferedWriter.writeLine("packageId="+bundleExtention.packageId)
        bufferedWriter.writeLine("priority="+bundleExtention.priority)

        JSONObject jsonObject = new JSONObject(bundleExtention.medusaBundles)
        bufferedWriter.writeLine("medusaBundles="+jsonObject.toString())

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("dependency=")
        boolean firstFlag = true;

        depConfig.allDependencies.each {
            stringBuilder.append((firstFlag?"":",")+it.name)
            firstFlag = false;
        }

        stringBuilder.append("\nexportPackages=")
        stringBuilder.append(bundleExtention.exportPackages)

        stringBuilder.append("\nactivities=")
        List<String> activities = BundleUtil.parseActivities(manifestFile)
        boolean first = true
        for (String act : activities) {
            if(!first)
                stringBuilder.append(",")
            stringBuilder.append(act)
            first = false
        }

        bufferedWriter.writeLine(stringBuilder.toString())
        //Log.log(this,'bundle '+stringBuilder.toString())
        Log.log(this,'write BUNDLE.MF TO TEMP :'+file.absolutePath)
        bufferedWriter.close()

        return file
    }

    @Override
    String toString() {
        return getClass().name
    }
}
