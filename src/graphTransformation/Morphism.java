package graphTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Morphism {

	Map<Edge, Edge> edgeMap = null;
	Map<Node, Node> nodeMap = null;
	public Morphism() {
		nodeMap = new HashMap<Node, Node>();
		edgeMap = new HashMap<Edge, Edge>();
	}
	public Morphism(Graph left, Graph right, String string) {
		if(left != null && right != null){
			int sep = string.indexOf('|');
			if(sep == -1){
				System.out.println("Malformation");
			}else{
				String[] nodes = string.substring(0, sep).split(";");
				String[] edges = string.substring(sep + 1).split(";");
				nodeMap = getNodeMap(nodes, left, right);
				edgeMap = getEdgeMap(edges, left, right);
			}
		}
	}
	public Morphism(Morphism mor) {
		nodeMap = new HashMap<Node, Node>();
		edgeMap = new HashMap<Edge, Edge>();
		for(Entry<Node, Node> entry : mor.nodeMap.entrySet())
			nodeMap.put(entry.getKey(), entry.getValue());
				for(Entry<Edge, Edge> entry : mor.edgeMap.entrySet())
					edgeMap.put(entry.getKey(), entry.getValue());
	}
	public Edge getEdgeMap(Edge e){
		return edgeMap.get(e);
	}
	private Map<Edge, Edge> getEdgeMap(String[] edges, Graph left, Graph right) {
		Map<Edge, Edge> results = new HashMap<Edge, Edge>();
		for (int i = 0; i < edges.length; i++) {
			String cur = edges[i];
			int sep = cur.indexOf("<->");
			if(sep == -1){
				System.out.println("Malformation when parsing edges");
				return null;
			}
			String src = cur.substring(0, sep);
			String trg = cur.substring(sep + 3);

			Edge source = left.findEdge(src);
			Edge target = right.findEdge(trg);
			if(source == null || target == null){
				System.out.println("cannot find Edge " + src + " or " + trg);
				return null;
			}
			if(results.containsKey(source)){
				System.out.println(src + " already have a morphism");
				return null;
			}
			results.put(source, target);
		}
		return results;
	}
	public Node getNodeMap(Node n){
		return nodeMap.get(n);
	}
	private Map<Node, Node> getNodeMap(String[] nodes, Graph left, Graph right) {
		Map<Node, Node> results = new HashMap<Node, Node>();
		for (int i = 0; i < nodes.length; i++) {
			String cur = nodes[i];
			int sep = cur.indexOf("<->");
			if(sep == -1){
				System.out.println("Malformation when parsing nodes");
				return null;
			}
			int src = -1, trg = -1;
			try{
				src = Integer.parseInt(cur.substring(0, sep));
				trg = Integer.parseInt(cur.substring(sep + 3));
			}catch(NumberFormatException e){
				System.out.println(e + " when parsing nodes");
				return null;
			}
			Node source = left.findNode(src);
			Node target = right.findNode(trg);
			if(source == null || target == null){
				System.out.println("cannot find node " + src + " or " + trg);
				return null;
			}
			if(results.containsKey(source)){
				System.out.println(src + " already have a morphism");
				return null;
			}
			results.put(source, target);
		}
		return results;
	}
	public void put(Edge key, Edge value) {
		if(edgeMap != null)
			edgeMap.put(key, value);
	}
	public void put(Node key, Node value) {
		if(nodeMap != null)
			nodeMap.put(key, value);
	}
	public void remove(Edge key) {
		if(edgeMap != null)
			edgeMap.remove(key);
	}
	public void remove(Node key) {
		if(nodeMap != null)
			nodeMap.remove(key);
	}
	public String toDot(final Graph left, final Graph right) {
		List<Node> nodes = new ArrayList<Node>();
		nodes.addAll(left.nodes);
		nodes.addAll(right.nodes);
		StringBuffer result = new StringBuffer();
		result.append("digraph G{\n\tsubgraph clusterleft{\n\t\tlabel=\"left\";\n");
		toNodeDot(left, result, "l");
		toEdgeDot(left, result, "l");
		result.append("\t}\n\tsubgraph clusterright{\n\t\tlabel=\"right\"\n");
		toNodeDot(right, result, "r");
		toEdgeDot(right, result, "r");

//		int size = nodes.size();
//		Collections.sort(nodes, new Comparator<Node>(){
//			public int compare(Node o1, Node o2) {
//				boolean lefto1 = left.nodes.contains(o1), lefto2 = left.nodes.contains(o2);
//				int x1 = lefto1 ? left.x : right.x;
//				int x2 = lefto2 ? left.x : right.x;
//				int result = o1.index/x1 - o2.index/x2;
//				if(result == 0){
//					if(lefto1 && !lefto2)
//						return -1;
//					else if(!lefto1 && lefto2)
//						return 1;
//					else
//						result = o1.index - o2.index;
//				}
//				return result;
//			}}	
//				);
//		result.append("");
//		for (int i = 0; i < size; i++) {
//			Node cur = nodes.get(i);
//			result.append((left.nodes.contains(cur) ? "l" : "r") + cur.index + "; ");
//			if(i + 1 < size){
//				int x1 = left.nodes.contains(cur) ? left.x : right.x;
//				int x2 = left.nodes.contains(nodes.get(i + 1)) ? left.x : right.x;
//				if(cur.index / x1 != nodes.get(i + 1).index / x2)
//					result.append("};\n\t{rank = same; ");
//			}
//		}
		result.append("};\n");
		for(Entry<Node, Node> entry : nodeMap.entrySet())
			result.append("\tl" + entry.getKey().index + " -> r" + entry.getValue().index + "[style=dotted];\n");
				//		for(Entry<Edge, Edge> entry : edgeMap.entrySet())
				//			result.append("\tl" + entry.getKey().src.index + ":l" + entry.getKey().trg.index + " -> r" + entry.getValue().src.index + ":r" + entry.getValue().trg.index + ";\n");
				result.append("}");
				return result.toString();
	}
	public void toEdgeDot(Graph graph, StringBuffer result, String l){
		int size = graph.edges.size();
		for (int i = 0; i < size; i++) {
			Edge cur = graph.edges.get(i);
			result.append("\t\t" + l + cur.src.index);
			result.append(" -> " + l);
			result.append(cur.trg.index);
			result.append("[label=\"");
			result.append(cur.name);
			result.append("\"];\n");
		}
		
	}
	public void toNodeDot(Graph graph, StringBuffer result, String l){
		int size = graph.nodes.size();
		graph.sortNodes();
		result.append("\t\t{rank = same; ");
		for (int i = 0; i < size; i++) {
			Node cur = graph.nodes.get(i);
			result.append("\t\t" + l + cur.index);
			result.append("[label=\"");
			result.append(cur.name);
			result.append("\"]; ");
			if(i + 1 < size && cur.index / graph.x != graph.nodes.get(i + 1).index / graph.x)
				result.append("};\n\t\t{rank = same; ");
		}
		result.append("};\n");
	}
}
