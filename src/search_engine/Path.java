package search_engine;

import java.util.*;

public class Path implements Comparable {
    private Vector stops;
    private Hashtable visited;  // to record the visited, to make fast checks
    private double rank;
    private boolean rank_modified;
    
    private PathFeatures features;
    private boolean cal_features = false;
    
    private Link[] links;
    private PathParameters params;    
    
    private Object val = new Object();      // maps don't accept a null val, so this is our workaround
    
    protected Path() {      // for the inheriting classes
        
    }
    public Path(Node n) {
        stops = new Vector();
        visited = new Hashtable();
        init(n);
    }
    
    public Path(Path prevPath, Node n) {
        stops = new Vector();
        visited = new Hashtable();
        
        for (int i = 0; i < prevPath.stops.size(); i++) 
            stops.addElement(prevPath.stops.elementAt(i));
        for (Enumeration nodes = prevPath.visited.keys(); nodes.hasMoreElements(); )
            visited.put(nodes.nextElement(), val);
        init(n);
    }
    
    private void init(Node n) {
        stops.addElement(n);
        visited.put(n, val);
        rank_modified = false;
    }
    
    public Node getLastStop() {
        return (Node)stops.lastElement();
    }
    
    public boolean isVisited(Node n) { return visited.containsKey(n); }
    public int length() { return stops.size(); }
    
    
    public void setParameters(PathParameters p) {
        params = p;
        rank_modified = true;
    }
    
    public PathParameters getParameters() { return params; }
    
    public PathFeatures getPathFeatures() {
        if (cal_features) return features;
    
        features = PathFeatures.extractPathFeatures(this);
        cal_features = true;
        return features;
    }
    
    private double calculateRank(PathParameters params) {
        return getPathFeatures().applyHypothesis(params);        
    }
    
    /*
     * using the parameters, computes a rank
     */
    public double getRank(PathParameters params) {
        if (!rank_modified)
            return rank;
        rank = calculateRank(params);
        rank_modified = false;
        return rank;
    }    
    
    public int compareTo(Object o) {
        Path other = (Path) o;
        double this_rank = getRank(params), other_rank = other.getRank(params);
        int diff = (int)(other_rank - this_rank);      // we want the better to be the first
        
        if (diff < 0) return Comparable.PRECEDE;
        else if (diff == 0) return Comparable.EQUAL;
        else return Comparable.FOLLOW;            
    }
    
    public Enumeration getNodes() {
        return stops.elements();
    }
    
    protected Link[] getAllLinks() {
        if (links != null) return links;
        
        links = new Link[length() - 1];
        for (int i = 0; i < (length() - 1); i++) {
            links[i] = ((Node)stops.elementAt(i)).getLinkBtwn((Node)stops.elementAt(i + 1));
        }
        return links;
    }
    
    /*
     * likes could also be a negative value, i.e dislike it
     */
    public void likePath(int num_likes) {
        
    }
}