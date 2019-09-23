/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datastore;

import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author amris
 */

public class Data {
    private String Key = "";
    private HashMap value;
    private int TTL = 0;
    private Date expiryDateTime = null;
    static final long ONE_MIN_IN_MILLISEC = 60000;

    /**
     * @return the Key
     */
    public String getKey() {
        return Key;
    }

    /**
     * @param Key the Key to set
     */
    public void setKey(String Key) {
        this.Key = Key;
    }

    /**
     * @return the value
     */
    public HashMap getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(HashMap value) {
        this.value = value;
    }

    /**
     * @return the TTL
     */
    public int getTTL() {
        return TTL;
    }

    /**
     * @param TTL the TTL to set
     */
    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    /**
     * @return the expiryDateTime
     */
    public Date getExpiryDateTime() {
        return expiryDateTime;
    }

    /**
     * @param expiryDateTime the expiryDateTime to set
     */
    public void setExpiryDateTime() {
        Calendar date = Calendar.getInstance();
//        long setExpiry = date.getTimeInMillis();
//        setExpiry += (this.TTL * ONE_MIN_IN_MILLISEC);
//        this.expiryDateTime = new Date(setExpiry);
        if(this.TTL > 0) {
            date.add(Calendar.MINUTE, TTL);
        } else {
            date.add(Calendar.YEAR, 100);
        }
        this.expiryDateTime = date.getTime();
    }
}
