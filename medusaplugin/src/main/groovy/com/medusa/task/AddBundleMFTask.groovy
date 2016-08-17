package com.medusa.task

import com.medusa.model.BundleModel
import com.medusa.util.Log
import org.gradle.api.artifacts.Configuration

/**
 * Created by tianyang on 16/8/3.
 */
public class AddBundleMFTask extends BaseMedusaTask{

    public void execute(File input,File output)
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
        BundleModel bundleModel = BaseMedusaTask.getResult(project,ReadBundlePropTask.class)

        File file = new File(output.getParent(),'BUNDLE.MF')

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))

        bufferedWriter.writeLine(bundleModel.raw)

        Configuration depConfig = project.configurations.findByName("provided")

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dependency=")
        boolean firstFlag = true;

        depConfig.allDependencies.each {
            println(it.toString())
            stringBuilder.append((firstFlag?"":",")+it.name)
            firstFlag = false;
        }
        bufferedWriter.writeLine(stringBuilder.toString())
        Log.log(this,'write BUNDLE.MF TO TEMP :'+file.absolutePath)
        bufferedWriter.close()

        return file
    }

    @Override
    String toString() {
        return getClass().name
    }
}
