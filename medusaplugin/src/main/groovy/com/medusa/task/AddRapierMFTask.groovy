package com.medusa.task

import com.medusa.RapierConstant
import com.medusa.util.BundleUtil
import com.medusa.util.Log
import org.gradle.api.Project
/**
 * Created by tianyang on 16/8/3.
 */
public class AddRapierMFTask extends BaseMedusaTask {

    @Override
    BaseMedusaTask init(Project project) {
        super.init(project)

        return this
    }

    public void execute(File input, File output) {
        if (input != null && input.exists()) {
            //Log.log(this, "add Bundle.MF to " + input)

            String pyBaseDir, pyPath;

            if (project.parent != null)
                pyBaseDir = project.parent.projectDir.absolutePath + '/tools'
            else
                pyBaseDir = project.projectDir.absolutePath + '/tools'
            pyPath = pyBaseDir + "/mf.py"

            File bundleFile = writeRapierMf(input,output)
            def mArgs = [input.absolutePath,output.absolutePath, bundleFile.absolutePath,"META-INF/RAPIER.MF"]
            Log.log(this, 'mf.py:' + pyPath + " args:" + mArgs)

            project.exec {
                workingDir pyBaseDir
                executable = 'chmod'
                args = ['777', 'mf.py']
            }

            project.exec {
                executable = pyPath
                args = mArgs
            }
        } else {
            Log.log(this, "add RAPIER.MF fail, target: " + input)
        }
    }

    private File writeRapierMf(File input,File output) {

        File file = new File(output.getParent(), 'RAPIER.MF')

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))
        bufferedWriter.writeLine("#generate by RapierPlugin "+RapierConstant.PLUGIN_VERSION)

        bufferedWriter.writeLine("md5="+BundleUtil.md5AsBase64(input))
        Log.log(this, 'write RAPIER.MF TO :' + file.absolutePath)
        bufferedWriter.close()

        return file
    }

    @Override
    String toString() {
        return getClass().name
    }


}
