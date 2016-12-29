package search_engine;

import java.io.*;
import java.util.*;

/*
 * PathFeatures and PathParameters should extend this
 * class and add their feautres.
 */
public class PathData extends Path {
    protected double[] array_form = null;
    
    protected double reserved;      // inherithing class have this form themselves
    protected double num_stops;
    protected double total_link_likes;
    protected double _1st_third_link_likes;
    protected double _2nd_third_link_likes;
    protected double _3rd_third_link_likes;
    protected double mean_link_likes;
    protected double std_link_likes;
    protected double total_node_popularity;
    protected double mean_node_popularity;
    protected double std_node_popularity;
    
    protected static final int NUM_PROP = 11;
    
    protected PathData(double val) {
        reserved = 1;       // this is the default, don't change it to another value
        num_stops = total_link_likes = _1st_third_link_likes = 
                _2nd_third_link_likes = _3rd_third_link_likes = mean_link_likes =
                std_link_likes = total_node_popularity = mean_node_popularity = std_node_popularity = val;
    }
    
    public double[] toArrayForm() {
        if (array_form != null) return array_form;  // cache the result;
        
        array_form = new double[NUM_PROP];
        
        array_form[0] = reserved;
        array_form[1] = num_stops;
        array_form[2] = total_link_likes;
        array_form[3] = _1st_third_link_likes;
        array_form[4] = _2nd_third_link_likes;
        array_form[5] = _3rd_third_link_likes;
        array_form[6] = mean_link_likes;
        array_form[7] = std_link_likes;
        array_form[8] = total_node_popularity;
        array_form[9] = mean_node_popularity;
        array_form[10] = std_node_popularity;
        
        return array_form;
    }
    
    protected double[] copyArray(double[] arr) {
        array_form = new double[NUM_PROP];
        System.arraycopy(arr, 0, array_form, 0, NUM_PROP);
        return array_form;
    }
    
    public void fromArrayForm(double[] arr) {
        if (arr.length != NUM_PROP) {
            //throw new Exception("Incompatable PathData");
            return;
        }
        
        reserved = arr[0];
        num_stops = arr[1];
        total_link_likes = arr[2];
        _1st_third_link_likes = arr[3];
        _2nd_third_link_likes = arr[4];
        _3rd_third_link_likes = arr[5];
        mean_link_likes = arr[6];
        std_link_likes = arr[7];
        total_node_popularity = arr[8];
        mean_node_popularity = arr[9];
        std_node_popularity = arr[10];
        
        array_form = copyArray(arr);
    }
    
    public void writeTo(DataOutputStream dos) throws IOException {
        double[] arr = toArrayForm();
        for (int i = 0; i < arr.length; i++) {
            dos.writeDouble(arr[i]);
        }
    }
    
    protected static PathData readFrom(DataInputStream dis) throws IOException {
        PathData result = new PathData(0.0);
        double[] arr = new double[PathData.NUM_PROP];
        
        for (int i = 0; i < PathData.NUM_PROP; i++) {
            arr[i] = dis.readDouble();
        }
        
        result.fromArrayForm(arr);
        return result;
    }
    
    /*
     * Extracts the features of a path. The path doesn't know what 
     * the features are, so this is where it should be done.
     */
    protected static PathData extractPathData(Path p) {
        PathData data = new PathData(1.0);
        
        int link_total = 0, _1st_third = 0, _2nd_third = 0, _3rd_third = 0;
        double link_mean, link_std_dev;
        
        Link[] _links = p.getAllLinks();
        int num_links = _links.length;
        
        double third = 1.0/3;
        double two_third = 2*third;
        
        for (int i = 0; i < num_links; i++) {
            int likes = _links[i].getLikes();
            link_total += likes;
            
            if ((double)i/num_links < third) _1st_third += likes;
            else if ((double)i/num_links < two_third) _2nd_third += likes;
            else _3rd_third += likes;
        }
        
        link_mean = ((double)link_total)/num_links;
        
        link_std_dev = 0;
        for (int i = 0; i < num_links; i++) {
            double tmp = _links[i].getLikes() - link_mean;
            link_std_dev += tmp * tmp;
        }
        link_std_dev = Math.sqrt(link_std_dev)/num_links; // square-root it
        
        int node_total = 0;
        double node_mean, node_std_dev;
        for (Enumeration en = p.getNodes(); en.hasMoreElements(); ) {
            node_total += ((Node)en.nextElement()).getNodePopularity();
        }
        
        node_mean = ((double)node_total)/p.length();
        
        node_std_dev = 0;
        for (Enumeration en = p.getNodes(); en.hasMoreElements(); ) {
            Node n = (Node)en.nextElement();
            double tmp = n.getNodePopularity() - node_mean;
            node_std_dev += tmp * tmp;
        }
        node_std_dev = Math.sqrt(node_std_dev)/p.length();
        
        data.num_stops = p.length();
        data.total_link_likes = link_total;
        data._1st_third_link_likes = _1st_third;
        data._2nd_third_link_likes = _2nd_third;
        data._3rd_third_link_likes = _3rd_third;
        data.mean_link_likes = link_mean;
        data.std_link_likes = link_std_dev;
        data.total_node_popularity = node_total;
        data.mean_node_popularity = node_mean;
        data.std_node_popularity = node_std_dev;
        
        return data;
    }
}
