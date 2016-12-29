package search_engine;

public class Link {
    public static final int DEFAULT_LIKES = 3;   
    public static final boolean DEFAULT_DIR = true;     // bi_directional
    
    private Node a, b;
    private Node stop;
    private int likes;      // num times this link was used
    
    /*
     * Constructs a Link b/n a->b or b->a, the order really doesn't
     * matter unless its a uni_directional link
     */
    public Link(Node a, Node b) {
        this.a = a;
        this.b = b;
                
        likes = DEFAULT_LIKES;      // don't want it to be zero initially        
    }
    
    public int getLikes() { return likes; }
    public void setLikes(int l) { likes = l; }
    
    public void likeLink(int val) { likes += val; }
    public void dislikeLink(int val) { likes -= val; }
    
    public boolean connects(Node x, Node y) {
        if (x.equals(a) && y.equals(b))
            return true;
        else if (y.equals(a) && (x.equals(b)))
            return true;
        else
            return false;
    }
    public String toString() {
        return "(" + likes + ")";
    }
}