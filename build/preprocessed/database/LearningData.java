package database;

import java.io.*;
import javax.microedition.rms.*;
import search_engine.*;

public class LearningData {
    private static final String STATIC_ML_FILE = "/ml/initial_ml_data.dat";
    private static final String LEARNING_DATA_FILE = "ml_database";
    
    private static final int MAX_EXAMPLES = 300;
    
    private PathParameters params;
    private static PathFeatures[] features = new PathFeatures[MAX_EXAMPLES];
    private static double[] expected = new double[MAX_EXAMPLES];
    
    private int examples;
    private int curr_index;
    private int static_examples;
    
    public LearningData() throws IOException {
        examples = 0;
        static_examples = 0;
        curr_index = 0;
        
        params = null;
        try {
            loadData();
        } catch (IOException e) {
            throw e;
        }
    }
    
    private void loadData() throws IOException {
        InputStream in = null;
        RecordStore store = null;
        try {
            in = this.getClass().getResourceAsStream(STATIC_ML_FILE);
            if (in == null) {
                throw new IOException("File not found : " + STATIC_ML_FILE);
            }
            
            DataInputStream din = new DataInputStream(in);
            params = (PathParameters)PathParameters.readFrom(din);
            loadDataFrom(din, true);
            din.close();
            
            store = RecordStore.openRecordStore(LEARNING_DATA_FILE, true);
            if (store.getNumRecords() != 0) {       // not our first time
                ByteArrayInputStream bins = new ByteArrayInputStream(store.getRecord(1));
                DataInputStream dins = new DataInputStream(bins);
                params = null;  // delete the old one
                params = (PathParameters)PathParameters.readFrom(dins);
                loadDataFrom(dins, false);
                bins.close();
                dins.close();
            }
        } catch (IOException e) {
            throw e;            
        } catch (RecordStoreException rse) {
            throw new IOException(rse.getMessage());
        } finally {
            try {
                if (in != null)
                    in.close();
                if (store != null)
                    store.closeRecordStore();
            } catch (Exception ioe) {
                
            }
        }
    }
    
    private void loadDataFrom(DataInputStream din, boolean static_file) throws IOException {
        int num = din.readInt();
        for (int i = 0; i < num; i++) {
            PathFeatures pfs = (PathFeatures)PathFeatures.readFrom(din);
            double expect = din.readDouble();
            addTrainingExample(pfs, expect);
            if (static_file) static_examples++;
        }
    }
    
    public PathParameters getParameters() {
        return params.clone();
    }
    
    public void setParameters(PathParameters pPath) {
        params = pPath.clone();
    }
    
    public void addTrainingExample(PathFeatures pf, double expected) {
        if (curr_index == MAX_EXAMPLES) 
            curr_index = static_examples;   // go back and overwrite some data
        features[curr_index] = pf;
        this.expected[curr_index++] = expected;
        
        if (examples < MAX_EXAMPLES) examples++;
    }
    
    public int getNumTrainingExamples() {
        return examples;
    }
    
    public PathFeatures[] getAllPathFeatures() {        
        return features;
    }
    
    public double[] getAllExpectedOutcomes() {        
        return expected;
    }
    
    public void saveData() {
        RecordStore store = null;
        
        if (static_examples == examples) return;        // no new data
        
        try {
            try {
                RecordStore.deleteRecordStore(LEARNING_DATA_FILE);
                
            } catch (RecordStoreNotFoundException e) { }
            
            store = RecordStore.openRecordStore(LEARNING_DATA_FILE, true);
            ByteArrayOutputStream bouts = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bouts);
            
            params.writeTo(dout);
            
            dout.writeInt(examples);
            for (int i = static_examples; i < examples; i++) {
                features[i].writeTo(dout);
                dout.writeDouble(expected[i]);
            }
            
            dout.flush();
            
            byte[] byteForm = bouts.toByteArray();
            try {
                store.setRecord(1, byteForm, 0, byteForm.length);
            } catch (InvalidRecordIDException ir) {
                store.addRecord(byteForm, 0, byteForm.length);
            }
            dout.close();
            bouts.close();
        } catch (Exception e) {
            // ignore
        } finally {
            try {
                if (store != null)
                    store.closeRecordStore();
            } catch (Exception e) {
                
            }
        }
        
    }
}