/* Time spent on a7:  hh hours and mm minutes.

 * Name:
 * Netid: 
 * What I thought about this assignment:
 *
 *
 */


package student;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import game.Edge;
import game.Node;

/** This class contains Dijkstra's shortest-path algorithm and some other methods. */
public class Paths {


    /** Return a list of the nodes on the shortest path from start to end, or
     * the empty list (a list of size 0) if a path from start to end does not exist. */
    public static LinkedList<Node> dijkstra(Node start, Node end) {
        /* TODO Implement Dijkstras's shortest-path algorithm presented
         in the slides titled "Final Algorithm" in the slides for lecture 19.
         In particular, a min-heap (as implemented in assignment A6) should be
         used for the frontier set. We provide a declaration of the frontier.

         Maintaining information about shortest paths will require maintaining
         for each node in the settled and frontier sets the backpointer for
         that node (as described in the handout) along with the length of the
         shortest path (thus far, for nodes in the frontier set). For this
         purpose, we provide static class NodeInfo. We leave it to you to
         declare the HashMap variable for this and describe carefully what it
         means. 

         Note 1: Do not attempt to create a data structure to contain the
             far-off set.
         Note 2: Read the list of notes on pages 2..3 of the handout carefully.
         */

        // The frontier set, as discussed in lecture
        MinHeap<Node> frontier= new MinHeap<Node>();

        // TODO complete this method
        
        HashMap<Node,NodeInfo> map = new HashMap<Node,NodeInfo>();
        
        frontier.add(start,0);
        
        int totaldist = 0;
        map.put(frontier.peek(), new NodeInfo(null,totaldist));
        
        while(!frontier.isEmpty() && map.get(end)==null){
        	
        	Node first = frontier.poll();
        	
        	for(Edge e : first.getExits()){
      
        		Node sink = e.getOther(first);
        		totaldist = e.length + map.get(first).distance;
        		
        		if (!map.containsKey(sink)){
        			
        			map.put(sink, new NodeInfo(first, totaldist));
        			frontier.add(sink, totaldist);
        			
        		}else {

        			NodeInfo sinkinfo = map.get(sink);
        			if(totaldist < sinkinfo.distance){
        				sinkinfo.backPointer = first;
        				sinkinfo.distance = totaldist;
        				frontier.updatePriority(sink, totaldist);
        			}
        		}
        	}
        	
        }
        
        if(frontier.isEmpty())
        	return new LinkedList<Node>();
        
        return buildPath(end, map);
    }

    /** Return the path from the start node to end.
     * Precondition: nodeInfo contains all the necessary information about
     * the path. */
    public static LinkedList<Node> buildPath(Node end, HashMap<Node, NodeInfo> nodeInfo) {
        LinkedList<Node> path= new LinkedList<Node>();
        Node p= end;
        while (p != null) {
            path.addFirst(p);
            p= nodeInfo.get(p).backPointer;
        }
        return path;
    }

    /** Return the sum of the weight of the edges on path p. */
    public static int pathLength(LinkedList<Node> path) {
        synchronized(path){
            if (path.size() == 0) return 0;

            Iterator<Node> iter= path.iterator();
            Node p= iter.next();  // First node on path
            int s= 0;
            // invariant: s = sum of weights of edges from start up to p
            while (iter.hasNext()) {
                Node q= iter.next();
                s= s + p.getConnect(q).length;
                p= q;
            }
            return s;
        }
    }

    /** An instance contains information about a node: the previous
     * node on a shortest path from the start node to this node and the distance
     * of this node from the start node. */
    private static class NodeInfo {
        private Node backPointer;
        private int distance;

        /** Constructor: an instance with distance d from the start node and
         * backpointer p.*/
        private NodeInfo(Node p, int d) {
            backPointer= p;  // Backpointer on the path (null if start node)
            distance= d; //Distance from start node to this one.
        }

        /** Constructor: an instance with a null previous node and distance 0. */
        private NodeInfo() {}

        /** return a representation of this instance. */
        public String toString() {
            return "distance " + distance + ", bckptr " + backPointer;
        }
    }


}
