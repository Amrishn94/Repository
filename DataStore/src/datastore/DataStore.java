/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datastore;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author amris
 */
public class DataStore {

    /**
     * @param args the command line arguments
     */
    //static Scanner in = new Scanner(System.in);
    static String filePath = "";
    static String fileName = "";
    Gson gson = new Gson();
    static ConcurrentHashMap<String, Integer> dataStore;
    static long FileSizeLimitationInMB = 0;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        Scanner in = new Scanner(System.in);
        Properties load = new Properties();
        List<Data> savedData;
        DataStore object = new DataStore();
        load.load(new FileInputStream(System.getProperty("user.dir") + File.separator + "AppConfig.properties"));
        System.out.println("Have you provide the file path to initialize the application in property file [Y/N] : ");
        String fileInitialization = in.nextLine();
        //System.out.println(System.getProperty("user.dir"));
        if ("Y".equalsIgnoreCase(fileInitialization)) {
            filePath = load.getProperty("DataStore_Path");
        } else {
            //System.out.println("Kindly provide the valid path [Use double slash for the file separator] : ");
            //filePath = in.nextLine();
            filePath = System.getProperty("user.dir");
        }
        if (filePath.equals("")) {
            filePath = load.getProperty("DataStore_Path");
        }
        fileName = load.getProperty("FileName");
        filePath = filePath + File.separator + fileName;
        FileSizeLimitationInMB = Long.parseLong(load.getProperty("FileSizeLimitationInMB"));
        File dataFile = new File(filePath);

        try {
            dataFile.createNewFile();
        } catch (IOException ex) {
            System.out.println("Invalid File path, please find the below exception");
            System.out.println("Exception "+ex);
        } catch (Exception ex) {
            System.out.println("Invalid File path, please find the below exception");
            System.out.println("Exception "+ex);
        }
        FileAccess access = new FileAccess();
        savedData = access.readJson(filePath);

        dataStore = new ConcurrentHashMap<String, Integer>();
        if (savedData != null && savedData.size() > 0) {
            for (Data d : savedData) {
                dataStore.put(d.getKey(), d.getTTL());
            }

        }
        Expiry toHandleExpiry = new Expiry();
        toHandleExpiry.start();

        while (true) {
            System.out.println("What type of operation do you want to perform: \n1.Create\n2.Read\n3.Delete\n4.Exit");
            int option = in.nextInt();
            switch (option) {
                case 1:
                    object.createOperation();
                    break;
                case 2:
                    object.readOperation();
                    break;
                case 3:
                    object.deleteOperation();
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("Please provide the valid input");
                    break;
            }
        }
    }

    private void createOperation() {
        System.out.println("Please provide the key value : ");
        JsonObject forFileWriter = new JsonObject();
        JsonObject oldJsonData = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        List<Data> savedData;
        Data dataObject = new Data();
        long fileSize = 0;

        Scanner in = new Scanner(System.in);
        String keyValues = "";
        keyValues = in.nextLine();
        if (keyValues.length() > 32 || keyValues.isEmpty()) {
            System.out.println("Please provide the valid key value its size should not exceed 32 characters : ");
            keyValues = in.nextLine();

        }

        dataObject.setKey(keyValues);
        if (dataStore.containsKey(keyValues)) {
            System.out.println(keyValues + " key already present in our data store.");
            return;
        } else {
            dataStore.put(keyValues, Integer.MIN_VALUE);
        }

        forFileWriter.addProperty("Key", keyValues);
        System.out.println("Please provide the value for the corresponding key [Provide the value in below given format]\n Input : 'fresh:fresh,works:works' : ");
        String data = in.nextLine();
        if (data.length() > (16 * 1024)) {
            System.out.println("Data size has been exceeded ");
            return;
        }
        if (data.length() > 0) {
            String[] jsonArray = data.split(",");
            JsonObject value = new JsonObject();
            HashMap<String, String> dataValues = new HashMap<String, String>();
            for (int i = 0; i < jsonArray.length; i++) {
                String[] jsonData = jsonArray[i].split(":");
                String objectName = jsonData[0];
                String objectValue = jsonData[1];

                value.addProperty(objectName, objectValue);
                dataValues.put(objectName, objectValue);
            }
            
            forFileWriter.add("Value", value);
            dataObject.setValue(dataValues);
        }

        System.out.println("Enter the Time to live value for this key in minutes [Optional] : ");
        String TTL = in.nextLine();
        int timeToLive = 0;
        try {
        timeToLive = Integer.parseInt(TTL);
        } catch(NumberFormatException ex) {
        }
        forFileWriter.addProperty("TTL", timeToLive);
        dataObject.setTTL(timeToLive);
        dataStore.put(keyValues, timeToLive);
        dataObject.setExpiryDateTime();
        forFileWriter.addProperty("expiryDateTime", dataObject.getExpiryDateTime().toString());

        File toGetSize = new File(filePath);
        fileSize = toGetSize.length() + forFileWriter.toString().length();
        if (fileSize > (FileSizeLimitationInMB * 1024 * 1024)) {
            System.out.println("Data Store file size has been exceeded");
        } else {
            FileAccess toCreateObject = new FileAccess();
            savedData = toCreateObject.readJson(filePath);
            savedData.add(dataObject);
            try {
                //FileWriter out = new FileWriter(filePath);
                boolean output = toCreateObject.writeJson(filePath, savedData, dataStore, keyValues, 1);
                if (output) {
                    System.out.println("Provided key and value has been added in our Data Store");
                } else {
                    System.out.println("Sorry for your Inconvenience, Corresponding Key has been already created by some other user");
                }
                //gson.toJson(savedData, out);
                //out.close();
            } catch (Exception ex) {
                Logger.getLogger(DataStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void readOperation() {
        List<Data> savedData;
        Scanner in = new Scanner(System.in);
        FileAccess toReadData = new FileAccess();
        boolean isAvailable = false;
        savedData = toReadData.readJson(filePath);

        System.out.println("Please provide the key to get the Json Object");
        String toFetchJson = in.nextLine();

        for (Data d : savedData) {
            if (d.getKey().equals(toFetchJson)) {
                System.out.println("Value is " + d.getValue());
                isAvailable = true;
                
            }
        }
        if (!isAvailable) {
            System.out.println("Entered Key is not available in the data store");
        }
    }

    private void deleteOperation() {
        List<Data> savedData;
        Scanner in = new Scanner(System.in);
        FileAccess toDeleteData = new FileAccess();
        savedData = toDeleteData.readJson(filePath);
        boolean isDeleted = false;

        System.out.println("Please provide the key to delete it from the DataStore");
        String toFetchJson = in.nextLine();

        for (Data d : savedData) {
            if (d.getKey().equals(toFetchJson)) {
                savedData.remove(d);
                isDeleted = true;
                break;
            }
        }
        if (isDeleted) {
            try {
                boolean output = toDeleteData.writeJson(filePath, savedData, dataStore, toFetchJson, 0);
                                
                if (output) {
                    dataStore.remove(toFetchJson);
                    System.out.println("Provided key has been removed from the DataStore");
                } else {
                    System.out.println("Sorry for your inconvenience, Corresponding key has been already deleted by another user or get expired");
                }
            } catch (Exception ex) {
                Logger.getLogger(DataStore.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("Entered Key is not available in the data store");
        }
    }

}
