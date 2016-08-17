package com.medusa.task;

import com.medusa.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyang on 16/8/15.
 */
public class PrepareBundleTask extends BaseMedusaTask {


    private Map<String,String> map = new HashMap<>();
    private static final String localBundlePostfix = ":0.0.0";

    @Override
    public void execute(File input, File output) {
        try
        {
            if(input != null)
            {
                collectInfo(input);
            }
            if(output != null)
            {
                writeInfo(output);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeInfo(File output) throws Exception
    {
        Log.log(this,"prepareBundle write merge.properties to "+output.getAbsolutePath());
        if(!output.getParentFile().exists())
            output.getParentFile().mkdirs();

        BufferedWriter writer = new BufferedWriter(new FileWriter(output));

        for (String key : map.keySet()) {
            writer.write(map.get(key)+"\n");
        }
        writer.close();
    }

    private void collectInfo(File input) throws Exception
    {
        map.clear();
        File localFile = new File(input,"local.properties");

        File bundleFile = new File(input,"bundle.properties");
        Log.log(this,"prepareBundle collect info from "+bundleFile.getAbsolutePath());
        BufferedReader reader = new BufferedReader(new FileReader(bundleFile));
        System.out.println();
        String line = null;
        while( (line = reader.readLine()) != null )
        {
            String content = line.trim().replace("\n", "");
            if( !content.startsWith("#") )
            {
                String[] tempInfos = content.split(":");
                String groupId = tempInfos[0];
                String artifactId = tempInfos[1];
                String bundleKey = groupId + ":" + artifactId;
                map.put(bundleKey,content);
            }
        }

        reader.close();

        if(localFile.exists())
        {
            reader = new BufferedReader(new FileReader(localFile));

            while( (line = reader.readLine()) != null )
            {
                String content = line.trim().replace("\n", "");
                if( !content.startsWith("#") )
                {
                    String[] tempInfos = content.split(":");
                    String groupId = tempInfos[0];
                    String artifactId = tempInfos[1];
                    String bundleKey = groupId + ":" + artifactId;
                    map.put(bundleKey,groupId+":"+artifactId+localBundlePostfix);
                }
            }

            reader.close();
        }
    }

    @Override
    public Object getResult() {
        return map;
    }
}
