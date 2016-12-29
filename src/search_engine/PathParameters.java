package search_engine;

import java.io.*;

/*
 * Used to hold the parameters of machine learning applied
 * to a path. The number and type of parameters is dictated by
 * the super class PathData.
 */
public class PathParameters extends PathData {
    // the super.reserved is theta_0
    
    public PathParameters() {
        super(0.0);
    }
    
    /*
     * the return type is PathParameters, so you should cast it to that
     */
    public static PathData readFrom(DataInputStream dis) throws IOException {
        PathParameters result = new PathParameters();
        PathData data = PathData.readFrom(dis);
        
        result.fromArrayForm(data.toArrayForm());
        return result;
    }
    
    public PathParameters clone() {
        PathParameters cloned = new PathParameters();
        cloned.fromArrayForm(super.toArrayForm());
        cloned.array_form = copyArray(super.toArrayForm());
        return cloned;
    }
}
