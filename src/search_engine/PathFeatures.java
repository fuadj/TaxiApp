package search_engine;

import java.io.DataInputStream;
import java.io.IOException;

public class PathFeatures extends PathData {
    /*
     * a valid PathFeature can only be generated from the factory methods
     * so this can't be accessed outside
     */
    private PathFeatures() {
        super(1.1);
        super.reserved = 1;     // x[0] should be 1
    }
    
    /*
     * the return type is PathFeatures, this is just for overloading
     */
    public static PathData readFrom(DataInputStream dis) throws IOException {
        PathFeatures result = new PathFeatures();
        PathData data = PathData.readFrom(dis);
        
        result.fromArrayForm(data.toArrayForm());
        result.reserved = 1;        // x[0] should always be 1
        return result;
    }
    
    public double applyHypothesis(PathParameters parameters) {
        double result = 0;
        double[] params = parameters.toArrayForm();
        double[] features = this.toArrayForm();
        
        if (params.length != features.length) {
            //throw new Exception("The feautres and parameters don't match");
            return 0.0;
        }
        
        for (int i = 0; i < features.length; i++) {
            result += features[i] * params[i];
        }
        
        return result;        
    }
    
    public static PathFeatures extractPathFeatures(Path p) {
        PathFeatures result = new PathFeatures();
        
        PathData data = PathData.extractPathData(p);
        result.fromArrayForm(data.toArrayForm());
        result.reserved = 1;    // x[0] should be 1, ALWAYS !!!
        
        return result;
    }
}
