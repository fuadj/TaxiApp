package database;

import java.util.*;
import java.io.*;
import search_engine.*;
import javax.microedition.rms.*;


public class RoadMap{
    private static final String NODES_FILE = "/roadData/Roads.txt";
    private static final String LINKS_FILE = "/roadData/Links.txt";
    private static final String LINKS_UPDATE_FILE_NAME = "update_links";
    
    private static final String NODE_FILE_ENCODING = "UTF-8";
    private static final String LINK_FILE_ENCODING = "US-ASCII";
    
    private boolean is_lang_eng;
    
    private Hashtable regions2nodes;         // maps from region to a vector of long_node names
    private Hashtable node_short2long;     // maps from short hand to long_name
    private Hashtable node_names_map;        // maps from string to Node type
    
    private Hashtable link_names_map;       // maps from string to Link type
    
    private static char[] p_start = {'('};
    private static char[] p_end = {')'};
    private static char[] b_start = {'['};
    private static char[] b_end = {']'};
    private static char[] p_b_n_start = {'(', '[', ';'};    // could be new node, old node, or line break i.e ';'
    private static char[] splitter = {'|'};
    private static char[] comma = {','};
    private static char[] p_end_comma = {')', ','};
    
    public RoadMap(UserSettings setting) throws Exception {
        if (setting.getLanguage().equals(UserSettings.LANG_ENG))
            is_lang_eng = true;
        else 
            is_lang_eng = false;
        
        regions2nodes = new Hashtable();
        node_names_map = new Hashtable();
        node_short2long = new Hashtable();        
        
        link_names_map = new Hashtable();
        
        InputStream nodes_file = null, links_file = null;
        RecordStore links_update = null;
        
        nodes_file = this.getClass().getResourceAsStream(NODES_FILE);
        links_file = this.getClass().getResourceAsStream(LINKS_FILE);
        links_update = RecordStore.openRecordStore(LINKS_UPDATE_FILE_NAME, true);
        try {
            loadNodeFileData(nodes_file);
            loadLinkFileData(links_file);
            updateLinkData(links_update);
        } catch (Exception e) {
            throw new Exception("Loading data Exception");
        } finally {
            try {
                if (nodes_file != null) nodes_file.close();
                if (links_file != null) links_file.close();
                if (links_update != null) links_update.closeRecordStore();
            } catch (Exception e) {
                // nigga, don't throw any exception, you don't know what streams can't be closed, IMPORTANT!! error
                // was trying to close the streams without the catch statement. That was generating an exception
            }
        }
    }
    
    private void loadNodeFileData(InputStream in) throws IOException {
        InputStreamReader insr = new InputStreamReader(in, NODE_FILE_ENCODING);
        MyBufferedReader reader = new MyBufferedReader(insr);

        while (true) {
            char tmp = reader.readUntil(p_start);   // start reading (Region)
            if (tmp == MyBufferedReader.EOF) break;        // EOF
            
            String region_name = readCorrectName(reader, is_lang_eng);
            regions2nodes.put(region_name, new Vector());

            boolean eof = false, newline = false;
            while (!newline) {
                tmp = reader.readUntil(p_b_n_start);
                if (tmp == (char)-1) {
                    eof = true;
                    break;
                }

                switch (tmp) {
                    case '(': {     // new Node found
                        String node_name = readCorrectName(reader, is_lang_eng);
                        reader.readUntil(b_start);      // start reading the '['
                        String short_name = readShortNodeName(reader);

                        String n = (String)node_short2long.get(short_name);
                        if ((n != null) && (n.equals(node_name) == false)) {
                            throw new IOException("RoadMap: Multiple definitions for " + short_name);
                        } else if (n == null) {
                            node_short2long.put(short_name, node_name);
                        }

                        Node node = new Node(node_name, short_name);
                        node_names_map.put(node_name, node);

                        ((Vector)regions2nodes.get(region_name)).addElement(node_name);
                        break;
                    }

                    case '[': {     // referece to previous node
                        String short_name = readShortNodeName(reader);
                        String long_name = (String)node_short2long.get(short_name);
                        if (long_name == null)
                            throw new IOException("RoadMap: Unknown references to " + short_name);

                        ((Vector)regions2nodes.get(region_name)).addElement(long_name);
                        break;
                    }

                    case ';': {     // line break
                        newline = true;
                        break;
                    }
                }

            }

            if (eof) break;
        }

        insr.close();
    }
    
    /*
     * assumes the reader is just starting to read after '('
     */
    private String readCorrectName(MyBufferedReader r, boolean is_eng) throws IOException {
        String s = null;
        r.readUntil(splitter);
        if (is_eng)
            s = r.getString();
        r.readUntil(p_end);         // you must move the reader even if it is english
        if (!is_eng)
            s = r.getString();
        return s;
    }
    
    /*
     * assumes the reader is just starting to read after '['
     */
    private String readShortNodeName(MyBufferedReader r) throws IOException {
        r.readUntil(b_end);
        return r.getString().trim().toUpperCase();
    }
    
    /*
     * loads the link data. The data should be in this format
     * (Lx, Nx, NX, [n, [(t|f)]])
     * Lx: the name of the link
     * Nx: the starting node short name
     * NX: the ending node short name
     * n: the initial likes to give the link
     * (t|f): t = bidirectional, f = unidirectional link
     */
    private void loadLinkFileData(InputStream in) throws IOException {
        InputStreamReader insr = new InputStreamReader(in, LINK_FILE_ENCODING);
        MyBufferedReader reader = new MyBufferedReader(insr);
    
        while (true) {
            char tmp = reader.readUntil(p_start);
            if (tmp == MyBufferedReader.EOF)    // EOF
                break;
            
            String link_name, start_node, end_node;
            int likes = Link.DEFAULT_LIKES;
            boolean bidirectional = true;
            
            reader.readUntil(comma);
            link_name = reader.getString().trim().toUpperCase();
            reader.readUntil(comma);
            start_node = reader.getString().trim().toUpperCase();
            tmp = reader.readUntil(p_end_comma);
            end_node = reader.getString().trim().toUpperCase();
            
            if (tmp == ',') {
                tmp = reader.readUntil(p_end_comma);
                likes += Integer.parseInt(reader.getString().trim());
                if (tmp == ',') {
                    reader.readUntil(p_end);
                    if (reader.getString().trim().toLowerCase().equals("f"))
                        bidirectional = false;
                }
            }
            
            if (link_names_map.get(link_name) == null) {        // we don't override link names
                Node a = (Node)node_names_map.get((String)node_short2long.get(start_node));
                Node b = (Node)node_names_map.get((String)node_short2long.get(end_node));

                Link l = new Link(a, b);

                l.setLikes(likes);
                a.setNeighbour(b, l);
                if (bidirectional)
                    b.setNeighbour(a, l);

                link_names_map.put(link_name, l);
            }
        }
    }
    
    /*
     * loads the RMS record data about the links, thereby updating
     * the original data loaded by loadLinkFileData().
     * the Exception thrown is RecordStoreException, but for 
     * convenience we throw it as IOException
     */
    private void updateLinkData(RecordStore store) throws IOException {
        try {    
            if (store.getNumRecords() == 0) return;     // our first time

            ByteArrayInputStream bins = new ByteArrayInputStream(store.getRecord(1));
            DataInputStream din = new DataInputStream(bins);

            int num = din.readInt();
            for (int i = 0; i < num; i++) {
                int link = din.readInt();
                int likes = din.readInt();

                String link_name = "L" + String.valueOf(link);
                ((Link)link_names_map.get(link_name)).setLikes(likes);
            }

            din.close();
            bins.close();
        } catch (RecordStoreException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public void saveData() {
        RecordStore store = null;
        
        try {
            try {
                RecordStore.deleteRecordStore(LINKS_UPDATE_FILE_NAME);  
            } catch (RecordStoreNotFoundException e) { }
            
            store = RecordStore.openRecordStore(LINKS_UPDATE_FILE_NAME, true);
            ByteArrayOutputStream bouts = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bouts);
            
            dout.writeInt(link_names_map.size());
            Enumeration links = link_names_map.keys();
            while (links.hasMoreElements()) {
                String l_name = (String)links.nextElement();
                Link l = (Link)link_names_map.get(l_name);
                int link = Integer.parseInt(l_name.substring(1));
                dout.writeInt(link);
                dout.writeInt(l.getLikes());                
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
        } catch (IOException e) {
            // ignore
        } catch (RecordStoreException rse) {
            // ignore
        } finally {
            try {
                if (store != null)
                    store.closeRecordStore();
            } catch (RecordStoreException e) {
                // again, ignore
            }
        }
    }
    
    /*
     * @return Returns all the regions using the Enumeration of Strings.
     */
    public Enumeration getRegions() { return regions2nodes.keys(); }
    
    /*
     * @return Returns the nodes contained in the region as an Enumeration of Strings.
     */
    public Enumeration getNodesInRegion(String r) {
        return ((Vector)regions2nodes.get(r)).elements();
    }
    
    public Node getNode(String n) {
        Node node = (Node)node_names_map.get(n);
        if (node == null) {
            // throw an Exception, invalid node name
        }
        return node;
    }
}
