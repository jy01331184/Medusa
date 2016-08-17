package com.medusa.util;

import com.medusa.model.BundleProperty;

import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by tianyang on 16/8/10.
 */
public class BundleUtil {
//
//    public static void main(String[] args) throws Exception{
//
//        String str = readBundleProperty(new File("/Users/tianyang/AndroidStudioProjects/Medusa/app/build/tmp/merge.properties"), new File("/Users/tianyang/AndroidStudioProjects/Medusa/app/linken"));
//
//        System.out.println(str);
//    }

    public static String readBundleProperty(File mergeFile,File linkenDir)
    {
        List<BundleProperty> properties = new ArrayList<>();

        if(mergeFile == null || !mergeFile.exists())
            throw new BundleException("mergeFile not exist!");
        if(linkenDir == null || !linkenDir.exists())
            throw new BundleException("linkenDir not exist!");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(mergeFile));

            String line = null;

            while( (line = reader.readLine()) != null )
            {
                line = line.trim();
                if(!Utils.isEmpty(line))
                {
                    String[] arr = line.split(":");
                    String groupId = arr[0];
                    String artifactId = arr[1];
                    String version = arr[2];

                    BundleProperty property = new BundleProperty();
                    property.groupId = groupId;
                    property.artifactId = artifactId;
                    property.version = version;

                    File[] files = linkenDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {

                            if( ("lib"+artifactId).equals(name.split("-")[0]))
                                return true;

                            return artifactId.equals(name.split("-")[0]);
                        }
                    });

                    for (File file : files) {
                        if(file.getName().endsWith(".xml"))
                            property.activities = parseActivities(file);
                        else if(file.getName().endsWith(".so"))
                            property.path = file.getName();
                    }

                    properties.add(property);
                }
            }

            reader.close();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();

        for (BundleProperty property : properties) {
            jsonArray.put(property.toJson());
        }

        return jsonArray.toString();
    }

    private static List<String> parseActivities(File manifest) throws Exception
    {
        List<String> activities = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=factory.newDocumentBuilder();
        FileInputStream input = new FileInputStream(manifest);
        Document doc = builder.parse(input);

        Element root = doc.getDocumentElement();
        NodeList childs = root.getChildNodes();

        for (int i = 0;i<childs.getLength();i++)
        {
            Node node = childs.item(i);
            if(node.getNodeName().equals("application"))
            {
                NodeList nodeChilds = node.getChildNodes();

                for(int j = 0;j < nodeChilds.getLength();j++)
                {
                    Node appNode = nodeChilds.item(j);
                    String nodeName = appNode.getNodeName();
                    if(nodeName.equals("activity") || nodeName.equals("service") || nodeName.equals("receiver"))
                    {
                        NamedNodeMap attrs = appNode.getAttributes();

                        for (int x = 0;x < attrs.getLength();x++)
                        {
                            Node attr = attrs.item(x);
                            if(attr.getNodeName().equals("android:name"))
                            {
                                activities.add(attr.getNodeValue());
                            }
                        }
                    }
                }

            }
        }



        input.close();
        return activities;
    }

}
