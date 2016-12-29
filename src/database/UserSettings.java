package database;

import java.io.*;
import javax.microedition.rms.*;

public class UserSettings {
    public static final String LANG_ENG = "English";
    public static final String LANG_AMH = "Amharic";
    
    public static final String LOC_ENG = "en-US";
    public static final String LOC_AMH = "et-EE";
    
    public static final String ENC_ENG = "US-ASCII"; // ASCII encoding
    public static final String ENC_AMH = "UTF-8"; // UNICODE encoding
    
    private static final String SETTINGS = "SETTINGS_RECORD";
    
    private String language;
    private boolean first_time;
    
    public UserSettings() {
        try {
            loadSettings();
        } catch (Exception e) {
            loadDefaultSettings();
        }        
    }
    
    private void loadSettings() throws Exception {
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(SETTINGS, true);
            if (store.getNumRecords() == 0) {
                first_time = true;
                throw new Exception("First Time");
            }
            ByteArrayInputStream bins = new ByteArrayInputStream(store.getRecord(1));
            DataInputStream din = new DataInputStream(bins);
            
            language = din.readUTF();
            
            din.close();
            bins.close();
            first_time = false;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (store != null)
                    store.closeRecordStore();
            } catch (RecordStoreException e) {
            }
        }
    }
            
    private void loadDefaultSettings() {
        language = LANG_ENG;
        first_time = true;
    }
    
    public boolean isFirstTime() { return first_time; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public void saveSettings() {
        RecordStore store = null;
        try {
            try {
                RecordStore.deleteRecordStore(SETTINGS);
            } catch (RecordStoreException e) { }
            
            store = RecordStore.openRecordStore(SETTINGS, true);
            
            ByteArrayOutputStream bouts = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bouts);
            
            dout.writeUTF(language);
            dout.flush();
            
            byte[] byteForm = bouts.toByteArray();
            
            try {
                store.setRecord(1, byteForm, 0, byteForm.length);
            } catch (InvalidRecordIDException e){
                store.addRecord(byteForm, 0, byteForm.length);
            }           
            
            dout.close();
            bouts.close();
        } catch (IOException e) {
            
        } catch (RecordStoreException e) {
            
        } finally {
            try {
                if (store != null)
                    store.closeRecordStore();
            } catch (RecordStoreException e) {
                
            }
        }
    }
}
