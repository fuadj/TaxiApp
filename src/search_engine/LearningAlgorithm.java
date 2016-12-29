package search_engine;

public class LearningAlgorithm {
    
    /*
     * uses Gradient Decent to minimize the cost function specified
     * by the PathParameters prev. 
     * @arg training_set The list of examples to train over.
     * @arg expected The expected outcome of each training example.
     * @arg prev The previous hypothesis.
     * @arg iter The number of iterations to do before convergence.
     * @arg alpha The learning rate
     * @returns a new Hypothesis.
     */
    public static PathParameters linearRegression_GradientDecent(int num_training_examples, PathFeatures[] training_set, double[] expected, PathParameters p_theta, int iter, double alpha) throws IllegalArgumentException {
        if ((training_set.length == 0) || (training_set.length != expected.length) ||
            (iter == 0)) {
            //throw new IllegalArgumentException("Invalid arguments gradientDecent");
            return p_theta.clone();
        }
        
        PathParameters theta = p_theta.clone();
        
        for (int i = 0; i < iter; i++) {
            double[] save_theta = (double[])theta.toArrayForm().clone();      // to apply simultaneous update
            for (int j = 0; j < save_theta.length; j++) {
                double sum = 0;
                for (int m = 0; m < num_training_examples; m++) {
                    sum += (training_set[m].applyHypothesis(theta) - expected[m]) * training_set[m].toArrayForm()[j];
                }
                save_theta[j] -= (((double)alpha)/training_set.length) * sum;
            }            
            theta.fromArrayForm(save_theta);
        }
        
        return theta;
    }
}
