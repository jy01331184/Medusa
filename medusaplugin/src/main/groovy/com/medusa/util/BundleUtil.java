package com.medusa.util;

import com.medusa.model.BundleProperty;
import com.medusa.model.RapierExtention;

import org.gradle.api.Project;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static String readBundleProperty(Project project, File mergeFile) {
        List<BundleProperty> properties = new ArrayList<>();

        if (mergeFile == null || !mergeFile.exists())
            throw new BundleException("mergeFile not exist!");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(mergeFile));

            String line = null;
            RapierExtention rapierExtention = (RapierExtention) project.getExtensions().findByName("rapier");

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!Utils.isEmpty(line)) {
                    String[] arr = line.split(":");
                    String groupId = arr[0];
                    final String artifactId = arr[1];
                    String version = arr[2];

                    BundleProperty property = new BundleProperty();
                    property.groupId = groupId;
                    property.artifactId = artifactId;
                    property.version = version;

                    property.path = "lib" + artifactId + "-" + version + ".so";

                    property.slink = rapierExtention.staticLink.contains(groupId + ":" + artifactId);

//                    Set<File> bundleDeps = project.getConfigurations().getByName("bundle").getFiles();
//                    for (File file : bundleDeps) {
//                        if (file.getName().equals(artifactId + "-" + version + "-" + "AndroidManifest.xml")) {
//                            property.activities = parseActivities(file);
//                        }
//                    }

                    properties.add(property);
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();

        for (BundleProperty property : properties) {
            //jsonArray.put(property.toJson());
            jsonObject.put(property.artifactId, property.toJson());
        }

        return jsonObject.toString();
    }

    public static List<String> parseActivities(File manifest) throws Exception {
        List<String> activities = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        FileInputStream input = new FileInputStream(manifest);
        Document doc = builder.parse(input);

        Element root = doc.getDocumentElement();
        NodeList childs = root.getChildNodes();

        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeName().equals("application")) {
                NodeList nodeChilds = node.getChildNodes();

                for (int j = 0; j < nodeChilds.getLength(); j++) {
                    Node appNode = nodeChilds.item(j);
                    String nodeName = appNode.getNodeName();
                    if (nodeName.equals("activity") || nodeName.equals("service") || nodeName.equals("receiver") || nodeName.equals("provider")) {
                        NamedNodeMap attrs = appNode.getAttributes();

                        for (int x = 0; x < attrs.getLength(); x++) {
                            Node attr = attrs.item(x);
                            if (attr.getNodeName().equals("android:name")) {
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

    public static byte[] computeMD5Hash(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);

        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[16384];

            int bytesRead;
            while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                e.update(buffer, 0, bytesRead);
            }

            byte[] var5 = e.digest();
            return var5;
        } catch (NoSuchAlgorithmException var14) {
            throw new IllegalStateException(var14);
        } finally {
            try {
                bis.close();
            } catch (Exception var13) {
                var13.printStackTrace();
            }

        }
    }


    public static byte[] computeMD5Hash(File file) throws FileNotFoundException, IOException {
        return computeMD5Hash((InputStream) (new FileInputStream(file)));
    }

    public static String md5AsBase64(File file) throws FileNotFoundException, IOException {
        return java.util.Base64.getEncoder().encodeToString(computeMD5Hash(file));
    }
}
