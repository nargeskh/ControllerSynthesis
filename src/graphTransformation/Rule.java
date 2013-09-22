package graphTransformation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;



public class Rule {

	List<Edge> addE = new ArrayList<Edge>(), removeE = new ArrayList<Edge>(), commonLE = new ArrayList<Edge>(), commonRE = new ArrayList<Edge>();
	List<Node> addV = new ArrayList<Node>(), removeV = new ArrayList<Node>(), commonLV = new ArrayList<Node>(), commonRV = new ArrayList<Node>();
	public Graph left;
	Graph right;
	Morphism mor;
	String name;
	public Rule(String n, String l, String r, String string){
		name = n;
		left = new Graph(n + "left", l);
		right = new Graph(n + "right", r);
		mor = new Morphism(left, right, string);
		initialAddAndRemove();
	}
	public Edge getCommonEdgeMap(Edge iter) {
		return mor.getEdgeMap(iter);
	}
	public Node getCommonNodeMap(Node iter) {
		return mor.getNodeMap(iter);
	}
	private void initialAddAndRemove() {
		Set<Node> leftNodes = mor.nodeMap.keySet();
		Collection<Node> rightNodes = mor.nodeMap.values();
		Set<Edge> leftEdges = mor.edgeMap.keySet();
		Collection<Edge> rightEdges = mor.edgeMap.values();
		if(left.nodes != null && leftNodes != null)
			for(Node node : left.nodes){
				if(!leftNodes.contains(node))
					removeV.add(node);
				else{
					commonLV.add(node);
					commonRV.add(mor.getNodeMap(node));
				}
				
			}
		if(right.nodes != null && rightNodes != null)
			for(Node node : right.nodes){
				if(!rightNodes.contains(node))
					addV.add(node);
			}
		if(left.edges != null && leftEdges != null)
			for(Edge edge : left.edges){
				if(!leftEdges.contains(edge))
					removeE.add(edge);
				else{
					commonLE.add(edge);
					commonRE.add(mor.getEdgeMap(edge));
				}
			}
		if(right.edges != null && rightEdges != null)
			for(Edge edge : right.edges){
				if(!rightEdges.contains(edge))
					addE.add(edge);
			}
	}
}
