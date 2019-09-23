/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datastore;

import static datastore.DataStore.dataStore;
import static datastore.DataStore.filePath;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author amris
 */
public class Expiry extends Thread {

    Thread toHandleExpiry;
    List<Data> savedData;
    List<Data> updatedJsonObject;
    final long SLEEP_TIME = 60000;
    Boolean needToUpdate = false;

    public void start() {
        //System.out.println("Thread started");
        super.start();
    }

    public void run() {
        //System.out.println("Thread executed");
        
        while (true) {
            try {

                FileAccess handleExpiry = new FileAccess();
                savedData = handleExpiry.readJson(filePath);
                if (savedData != null) {
                    updatedJsonObject = new ArrayList<Data>();
                    for (Data d : savedData) {
                        Date now = new Date();
                        int comparedValue = now.compareTo(d.getExpiryDateTime());
                        //System.out.println("Key value " + d.getKey() + " , comparedValue " + comparedValue);
                        if (comparedValue > 0) {
                            needToUpdate = true;
                            dataStore.remove(d.getKey());
                        } else {
                            updatedJsonObject.add(d);
                        }
                    }
                    if (needToUpdate) {
                        try {
                            handleExpiry.writeJsonExpiry(filePath, updatedJsonObject);
                        } catch (Exception ex) {
                            Logger.getLogger(DataStore.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(DataStore.class.getName()).log(Level.SEVERE, null, ex);
            } 
            try {
                sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(Expiry.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
