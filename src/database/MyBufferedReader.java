package database;

import java.io.*;

public class MyBufferedReader {
    public static final char EOF = (char)-1;
    
    private static char[] buffer = new char[200];       // more than enough
    private int length;
    
    private InputStreamReader input;
    
    public MyBufferedReader() {
        this(null);
    }
    
    public MyBufferedReader(InputStreamReader inr) {
        length = 0;
        input = inr;
    }
    
    public void setInputStream(InputStreamReader inr) {
        this.input = inr;
    }
    
    public char readUntil(char[] delimiter) throws IOException {
        if (input == null)
            throw new IOException("MyBufferedReaderException: no InputStreamReader set!!");
        
        length = 0;     // reset the internal buffer
        int val;
        while ((val = input.read()) != -1) {
            for (int i = 0; i < delimiter.length; i++) {
                if (delimiter[i] == (char)val)
                    return delimiter[i];
            }
            buffer[length++] = (char)val;
        }
        
        return (char)val;
    }
    
    public String getString() {
        return new String(buffer, 0, length);
    }
}
