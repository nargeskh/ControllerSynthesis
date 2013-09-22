package graphTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Match{
	Map<Edge, List<Edge>> edgeMap = new HashMap<Edge, List<Edge>>();
	Edge[] edgesL = null;
	Graph key;
	List<Morphism> matches = new ArrayList<Morphism>();
	Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
	Node[] nodesL = null;
	List<Node> unmatched = new ArrayList<Node>();
	Graph value;
	public Match() {
	}
	public Match(Graph left, Graph graph){
		initalize(left, graph);
		siftGraph(key, value);
	}
	void addEdge(Edge sift, Edge edge) {
		if(edgeMap.containsKey(sift) && !edgeMap.get(sift).contains(edge))
			edgeMap.get(sift).add(edge);
		else{
			List<Edge> value = new ArrayList<Edge>();
			value.add(edge);
			edgeMap.put(sift, value);
		}
	}
	private void addMatch(Morphism copied) {
		matches.add(copied);
	}
	public void addNode(Node sift, Node node) {
		if(nodeMap.containsKey(sift) && !nodeMap.get(sift).contains(node))
			nodeMap.get(sift).add(node);
		else{
			List<Node> value = new ArrayList<Node>();
			value.add(node);
			nodeMap.put(sift, value);
		}
	}
	public List<Morphism> findISOMatch() {
		List<Node> matchedNode = new ArrayList<Node>();
		List<Edge> matchedEdge = new ArrayList<Edge>();
		if(edgesL.length > 0 && edgeMap != null && nodeMap != null)
			findISOMatch(0, new Morphism(), matchedNode, matchedEdge);
		return matches;
	}
	private void findISOMatch(int i, Morphism mor, List<Node> matchedNode, List<Edge> matchedEdge) {
		Edge key = edgesL[i];
		for(Edge value : edgeMap.get(key)){
			if(matchedEdge.contains(value))
				return;
			Node src = mor.nodeMap.get(key.src);
			Node trg = mor.nodeMap.get(key.trg);
			if((src != null && src != value.src) ||
					(trg != null && trg != value.trg))
				continue;
			if((src == null && matchedNode.contains(value.src)) || (trg == null && matchedNode.contains(value.trg)))
				return;
			mor.put(key, value);
			if(src == null){
				mor.put(key.src, value.src);
				unmatched.remove(key.src);
				matchedNode.add(value.src);
			}
			if(trg == null){
				mor.put(key.trg, value.trg);
				unmatched.remove(key.trg);
				matchedNode.add(value.trg);
			}
			if(i + 1 == edgesL.length){
				if(unmatched.isEmpty()){
					addMatch(new Morphism(mor));
					return;
				}else{
					findISONodeMatch(0, mor, matchedNode);
					if(matches.size() != 0)
						return;
				}
			}else
				findMatch(i + 1, mor);
			mor.remove(key);
			if(src == null){
				mor.remove(key.src);
				unmatched.add(key.src);
				matchedNode.remove(value.src);
			}
			if(trg == null){
				mor.remove(key.trg);
				unmatched.add(key.trg);
				matchedNode.remove(value.trg);
			}				
		}
	}
	private void findISONodeMatch(int i, Morphism mor, List<Node> matchedNode) {
		Node key = null;
		for (; i < nodesL.length; i++) {
			key = nodesL[i];
			if(unmatched.contains(key))
				break;
		}
		if(i == nodesL.length){
			matches.add(new Morphism(mor));
			return;
		}
		else{
			for(Node value : nodeMap.get(key)){
				if(matchedNode.contains(value))
					continue;
				mor.put(key, value);
				matchedNode.add(value);
				if(i + 1 == nodesL.length){
					Morphism copied = new Morphism(mor);
					copied.put(key, value);
					addMatch(copied);
					return;
				}else{
					findISONodeMatch(i + 1, mor, matchedNode);
					if(!matches.isEmpty())
						return;
				}
				matchedNode.remove(value);
				mor.remove(key);
			}
		}
	}
	public List<Morphism> findMatch() {
		if(edgesL.length > 0 && edgeMap != null && nodeMap != null)
			findMatch(0, new Morphism());
		return matches;
	}
	private void findMatch(int i, Morphism mor) {
		Edge key = edgesL[i];
		if(edgeMap.get(key) == null)
			return;
		for(Edge value : edgeMap.get(key)){
			Node src = mor.nodeMap.get(key.src);
			Node trg = mor.nodeMap.get(key.trg);
			if((src != null && src != value.src) ||
					(trg != null && trg != value.trg))
				continue;
			mor.put(key, value);
			if(src == null){
				mor.put(key.src, value.src);
				unmatched.remove(key.src);
			}
			if(trg == null){
				mor.put(key.trg, value.trg);
				unmatched.remove(key.trg);
			}
			if(i + 1 == edgesL.length){
				if(unmatched.isEmpty()){
					addMatch(new Morphism(mor));
				}else
					findNodeMatch(0, mor);
			}else
				findMatch(i + 1, mor);
			mor.remove(key);
			if(src == null){
				mor.remove(key.src);
				unmatched.add(key.src);
			}
			if(trg == null){
				mor.remove(key.trg);
				unmatched.add(key.trg);
			}				
		}
	}
	private void findNodeMatch(int i, Morphism mor) {
		Node key = null;
		for (; i < nodesL.length; i++) {
			key = nodesL[i];
			if(unmatched.contains(key))
				break;
		}
		if(i == nodesL.length)
			matches.add(new Morphism(mor));
		else{
			for(Node value : nodeMap.get(key)){
				mor.put(key, value);
				if(i + 1 == nodesL.length){
					Morphism copied = new Morphism(mor);
					copied.put(key, value);
					addMatch(copied);
				}else
					findNodeMatch(i + 1, mor);
				mor.remove(key);
			}
		}
	}
	public void initalize(Graph left, Graph graph){
		key = left;
		value = graph;
		edgesL = key.edges.toArray(new Edge[key.edges.size()]);
		nodesL = key.nodes.toArray(new Node[key.nodes.size()]);
		unmatched.addAll(key.nodes);
	}
	boolean isEdgeNeeded(Edge left, Edge right) {
		return isNodeNeeded(left.src, right.src) && isNodeNeeded(left.trg, right.trg) && left.name.equals(right.name);
	}

	boolean isNodeNeeded(Node left, Node right) {
		return left.name.equals(right.name);
	}

	private void siftGraph(Graph left, Graph right) {
		for(Edge sift : left.edges){
			for(Edge edge : right.edges)
				if(isEdgeNeeded(sift, edge))
					addEdge(sift, edge);
					if(!edgeMap.containsKey(sift)){
						edgeMap = null;
						return;
					}
		}
		for(Node sift : left.nodes){
			for(Node edge : right.nodes)
				if(isNodeNeeded(sift, edge))
					addNode(sift, edge);

					if(!nodeMap.containsKey(sift)){
						nodeMap = null;
						return;
					}
		}
	}
}