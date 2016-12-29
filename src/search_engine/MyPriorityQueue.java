package search_engine;

public class MyPriorityQueue {
    private Elem head;
    private int num_elems;
    
    public MyPriorityQueue() {
        head = new Elem(new Object(), null);
        num_elems = 0;
    }
    
    public Object dequeue() {
        if (num_elems == 0) return null;
        num_elems--;
        
        Object o = head.next.val;
        head = head.next;
        
        return o;
    }
    
    public void enqueue(Object o) {
        num_elems++;
        Elem new_elem = new Elem(o, null);
        
        Elem n = head;
        boolean found = false;
        
        while (n.next != null) {
            Comparable a = (Comparable)o;
            Comparable b = (Comparable)(n.next.val);
            
            switch (a.compareTo(b)) {
                case Comparable.PRECEDE: 
                case Comparable.EQUAL: {
                    new_elem.next = n.next;
                    n.next = new_elem;         
                    found = true;
                    break;
                }
            }
            
            if (found) break;
            n = n.next;
        }
        
        if (found == false) {
            n.next = new_elem;
            new_elem.next = null;
        }
    }
    
    public int size() {
        return num_elems;
    }
    
    class Elem {
        public Object val;
        public Elem next;
        
        public Elem(Object v, Elem n) { val = v; next = n; }
    }
}
