package search_engine;

import java.util.Hashtable;
import java.util.Enumeration;

public class Node {
    // the short name is used for comparision purposes
    private String long_name, short_name;
    private Hashtable connections;
    
    public Node() {
        this("", "");
    }
    
    public Node(String long_name, String short_name) {
        this.long_name = long_name;
        this.short_name = short_name;
        connections = new Hashtable();
    }
    
    public void setNeighbour(Node n, Link l) {
        connections.put(n, l);
    }
    
    public int getNodePopularity() {
        return connections.size();
    }
    public Enumeration getNeighbours() {
        return connections.keys();
    }
    
    public Link getLinkBtwn(Node other) {
        return (Link)connections.get(other);
    }
    
    public int hashCode() {
        return 0;
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (this.getClass() != o.getClass()))
            return false;
        
        Node other = (Node)o;
        return short_name.equals(other.short_name);
    }
    
    public String getName() { return long_name; }
}
