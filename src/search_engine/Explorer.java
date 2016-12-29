package search_engine;

import java.util.*;
import database.*;

public class Explorer implements Runnable {
    private Node start, stop;
    private SearchListener listener;
    
    private PathParameters params;
    private RoadMap road_map;
    private LearningData l_db;
    
    private boolean running;
    
    private MyPriorityQueue search;
    private MyPriorityQueue hits;
    
    private Path[] results;
    
    public Explorer(RoadMap road_map, LearningData ldb) {
        listener = new SearchListener() {       // default is do nothing
            public void notifySearchFinished() { }
        };
        running = false;
        this.road_map = road_map;
        this.l_db = ldb;
        params = ldb.getParameters();
        
        search = new MyPriorityQueue();
        hits = new MyPriorityQueue();
        results = null;
    }
    
    public void setSearchListener(SearchListener sl) {
        listener = sl;
    }
    
    public void setEndPoints(Node start, Node end) {
        this.start = start;
        this.stop = end;
    }
    
    public void startSearch() {
        running = true;
        new Thread(this).start();
    }
    
    public void quitSearch() {
        running = false;
    }
    
    public Path[] getSearchResults() { 
        if (results != null) return results;
        results = new Path[hits.size()];
        int i = 0;
        while (hits.size() != 0) {
            results[i++] = (Path)hits.dequeue();
        }
        return results;
    }
    
    public void run() {
        search.enqueue(new Path(start));
        while (running && (search.size() != 0)) {
            Path path = (Path)search.dequeue();
            for (Enumeration nodes = path.getLastStop().getNeighbours(); nodes.hasMoreElements(); ) {
                Node neighbour = (Node)nodes.nextElement();
                Path newPath = new Path(path, neighbour);
                newPath.setParameters(params);
                if (neighbour.equals(stop)) {
                    hits.enqueue(newPath);
                } else if (path.isVisited(neighbour) == false) {
                    search.enqueue(newPath);
                }
            }
        }
        
        if (running == false) return;
        results = null;
        listener.notifySearchFinished();        
    }
    
    public void setParameters(PathParameters p) {
        params = p.clone();
    }
}
