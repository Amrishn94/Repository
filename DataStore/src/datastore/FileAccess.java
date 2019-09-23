/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datastore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import static datastore.DataStore.filePath;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amris
 */
public class FileAccess {

    public synchronized boolean writeJson(String filePath, List<Data> jsonData, ConcurrentHashMap<String, Integer> dataStore, String keyValue, int operation) {
        try {

            if (operation == 0) {
                if (dataStore.containsKey(keyValue)) {
                    FileWriter out = new FileWriter(filePath);
                    Gson gson = new Gson();
                    gson.toJson(jsonData, out);
                    out.close();
                } else {
                    return false;
                }
            } else if(operation == 1) {
                if(dataStore.containsKey(keyValue)) {
                    return false;
                } else {
                    FileWriter out = new FileWriter(filePath);
                    Gson gson = new Gson();
                    gson.toJson(jsonData, out);
                    out.close();
                }
            } else{
                return false;
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(FileAccess.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public synchronized void writeJsonExpiry(String filePath, List<Data> jsonData) {
        try {
            FileWriter out = new FileWriter(filePath);
            Gson gson = new Gson();
            gson.toJson(jsonData, out);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(FileAccess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public synchronized List<Data> readJson(String filePath) {
        Gson gson = new Gson();
        Reader reader = null;
        List<Data> oldData = new ArrayList<Data>();
        Path path = Paths.get(filePath);
        try {
            reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(FileAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        oldData = gson.fromJson(reader, new TypeToken<List<Data>>() {
        }.getType());
        return oldData;
    }
}
