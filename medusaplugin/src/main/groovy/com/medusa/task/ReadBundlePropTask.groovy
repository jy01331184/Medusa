package com.medusa.task

import com.medusa.model.BundleModel
import com.medusa.util.BundleException
import com.medusa.util.Log
/**
 * Created by tianyang on 16/8/3.
 */
public class ReadBundlePropTask extends BaseMedusaTask{

    private File bundlePropFile
    private BundleModel bundleModel

    public void execute(File input,File output) {
        bundlePropFile = new File(project.projectDir,'bundle.properties')
        Log.log(this, String.format("read bundle prop from %s",bundlePropFile.absolutePath))

        if(!bundlePropFile.exists())
            throw new BundleException("must specify name field in bundle.properties")
        else
        {
            Properties properties = new Properties();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bundlePropFile))

            properties.load(bufferedReader)

            if(!properties.containsKey(BundlePropConstant.NAME_KEY))
                throw new BundleException("must specify name field in bundle.properties")
            StringBuilder builder = new StringBuilder()
            bufferedReader = new BufferedReader(new FileReader(bundlePropFile))
            String line = ''
            while ( (line = bufferedReader.readLine() ) != null)
            {
                builder.append(line+"\n")
            }


            bundleModel = new BundleModel()
            bundleModel.name = properties.get(BundlePropConstant.NAME_KEY)
            bundleModel.packageId = properties.get(BundlePropConstant.PACKAGEID_KEY)
            bundleModel.group = properties.get(BundlePropConstant.GROUP_KEY)
            bundleModel.version = properties.get(BundlePropConstant.VERSION_KEY)
            bundleModel.raw = builder.toString()
            bufferedReader.close()

            Log.log(this,"make bundle model:"+bundleModel+" - ")
        }
    }

    @Override
    Object getResult() {
        return bundleModel
    }

    class BundlePropConstant{
        static final String NAME_KEY = "name"
        static final String PACKAGEID_KEY = "packageId"
        static final String GROUP_KEY = "group"
        static final String VERSION_KEY = "version"
    }
}
