package search_engine;

public interface Comparable {
    public static final int PRECEDE = -1;
    public static final int EQUAL = 0;
    public static final int FOLLOW = 1;
    
    public int compareTo(Object o);    
}
