package com.medusa.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyang on 17/2/23.
 */
public class PrepareDependencyTask extends BaseMedusaTask {

    List<String> dependency = new ArrayList<>();

    @Override
    public void execute(File input, File output) {

        dependency.clear();

        try {

            BufferedReader reader = new BufferedReader(new FileReader(input));

            String line;
            while ((line = reader.readLine()) != null) {
                String content = line.trim().replace("\n", "");
                dependency.add(content);
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Object getResult() {
        return dependency;
    }
}
