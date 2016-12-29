package database;

import java.io.*;
import java.util.*;

public class Locale {
    private static final String L_FILE_EN = "/locale/locale_en.txt";
    private static final String L_FILE_AM = "/locale/locale_am.txt";
    
    private static final String ENCODING_ENG = "US-ASCII";
    private static final String ENCODING_AMH = "UTF-8";
    
    private Hashtable map;
    
    private static char[] p_start = {'('};
    private static char[] p_end = {')'};
    private static char[] equal = {'='};
    
    public Locale(UserSettings setting) throws IOException {
        MyBufferedReader reader;
        String file_name;
        String encoding;
        
        if (setting.getLanguage().equals(UserSettings.LANG_ENG)) {
            file_name = L_FILE_EN;
            encoding = ENCODING_ENG;
        } else {        // only two languages are supported, till now
            file_name = L_FILE_AM;
            encoding = ENCODING_AMH;
        }
        
        map = new Hashtable();
        InputStream in = null;
        try {
            in = this.getClass().getResourceAsStream(file_name);
            if (in == null) {
                throw new IOException("File not found : " + file_name);
            }
            
            InputStreamReader insr = new InputStreamReader(in, encoding);
            reader = new MyBufferedReader(insr);
            
            while (true) {
                char tmp = reader.readUntil(p_start);
                if (tmp == (char)-1) break;     // EOF
                
                reader.readUntil(equal);
                String key = reader.getString().trim().toLowerCase();
                reader.readUntil(p_end);
                String val = reader.getString().trim();
                map.put(key, val);
            }
            
            insr.close();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) { }     // ignore this one
        }
    }
    
    public String getString(String key) {
        String value = (String)map.get(key.toLowerCase().trim());
        if (value == null) {
            // throw Exception, invalid key
        }
        return value;
    }    
}