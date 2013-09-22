package graphTransformation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


public class GraphTransform {


	List<Graph> results = new ArrayList<Graph>();

	List<Rule> rules;
	Map<GraphHash, List<Graph>> spaces = new TreeMap<GraphHash, List<Graph>>();
	public void addRule(Rule rule) {
		if(rules == null)
			rules = new ArrayList<Rule>();
		if(rule.left != null && rule.right != null){
			rules.add(rule);
			Utils.putIntoFile(rule.mor.toDot(rule.left, rule.right), rule.name);
		}
	}
	public Graph apply(Graph graph, Rule rule, Morphism mor) {
		Map<Node, List<Node>> reverseV= new HashMap<Node, List<Node>>();
		for(Entry<Node, Node> entry : mor.nodeMap.entrySet()){
			Node key = entry.getKey();
			Node value = entry.getValue();
			List<Node> cur = reverseV.get(value);
			if(cur == null){
				cur = new ArrayList<Node>();
				cur.add(key);
				reverseV.put(value, cur);
			}else
				cur.add(key);
		}
		Set<Node> ipV = new HashSet<Node>();
		for(Entry<Node, List<Node>> entry : reverseV.entrySet()){
			if(entry.getValue().size() > 1)
				ipV.addAll(entry.getValue());
		}

		Map<Edge, List<Edge>> reverseE= new HashMap<Edge, List<Edge>>();
		for(Entry<Edge, Edge> entry : mor.edgeMap.entrySet()){
			Edge key = entry.getKey();
			Edge value = entry.getValue();
			List<Edge> cur = reverseE.get(value);
			if(cur == null){
				cur = new ArrayList<Edge>();
				cur.add(key);
				reverseE.put(value, cur);
			}else
				cur.add(key);
		}
		Set<Edge> ipE = new HashSet<Edge>();
		for(Entry<Edge, List<Edge>> entry : reverseE.entrySet()){
			if(entry.getValue().size() > 1)
				ipE.addAll(entry.getValue());
		}

		Set<Node> dangle = new HashSet<Node>();
		for(Entry<Node, Node> entry : mor.nodeMap.entrySet()){
			Node a = entry.getValue();
			if((a.ins != null  && !a.ins.isEmpty() && !mor.edgeMap.values().containsAll(a.ins))  || (a.outs != null && !a.outs.isEmpty()  && !mor.edgeMap.values().containsAll(a.outs)))
				if(!dangle.contains(entry.getKey()))
					dangle.add(entry.getKey());
		}

		if(rule.commonLV.containsAll(ipV) && rule.commonLE.containsAll(ipE) && rule.commonLV.containsAll(dangle)){
			//put common into new Graph and Delete elements
			Map<Node, Node> copied = new HashMap<Node, Node>();
			Graph target = copyGraph(copied, graph, reverseV, reverseE, rule, mor);
			target.name = graph.name + "_" + rule.name;
			target.x = graph.x;
			target.y = graph.y;
			//Add elements
			for(Node node : rule.addV){
				Node n = new Node(node);
				copied.put(node, n);
		//		n.index = graph.getNextIndex();
				target.addNode(n);
			}
			for(Edge edge : rule.addE){
				Edge e = new Edge(copied.get(edge.src), copied.get(edge.trg), edge.name);
				target.edges.add(e);
			}
			return target;
		}
		return null;
	}
	protected boolean checkMorphism(Rule rule, Match match2, Morphism copied, Graph right, Graph graph) {
		Morphism checkNac = new Morphism();
		Match match = new Match();
		match.initalize(right, graph);
		for(Edge e : rule.commonLE){
			checkNac.put(rule.getCommonEdgeMap(e), copied.getEdgeMap(e));
		}

		for(Node n : rule.commonLV){
			checkNac.put(rule.getCommonNodeMap(n), copied.getNodeMap(n));
		}

		for(Edge e : right.edges){
			if(!rule.commonRE.contains(e)){
				Node src = e.src;
				Node trg = e.trg;
				Boolean sIsCommon = rule.commonRV.contains(src), tIsCommon = rule.commonRV.contains(trg);
				if(sIsCommon && tIsCommon){
					List<Edge> targets = new ArrayList<Edge>();
					List<Edge> targets2 = checkNac.getNodeMap(trg).ins;
					for(Edge iter : checkNac.getNodeMap(src).outs){
						if(targets2.contains(iter) && iter.name.equals(e.name))
							targets.add(iter);
					}
					if(targets.isEmpty())
						return true;
					match.edgeMap.put(e, targets);
				}else if(!tIsCommon && sIsCommon){
					List<Edge> targets = new ArrayList<Edge>();
					for(Edge iter : checkNac.getNodeMap(src).outs){
						if(iter.name.equals(e.name)){
							targets.add(iter);
							List<Node> temp = match.nodeMap.get(e.trg);
							if(temp == null){
								temp = new ArrayList<Node>();
								temp.add(iter.trg);
								match.nodeMap.put(e.trg, temp);
							}
						}
					}
					if(targets.isEmpty())
						return true;
					match.edgeMap.put(e, targets);
				}else if (tIsCommon && !sIsCommon){
					List<Edge> targets = new ArrayList<Edge>();
					for(Edge iter : checkNac.getNodeMap(trg).ins){
						if(iter.name.equals(e.name)){
							targets.add(iter);
							List<Node> temp = match.nodeMap.get(src);
							if(temp == null){
								temp = new ArrayList<Node>();
								temp.add(iter.src);
								match.nodeMap.put(src, temp);
							}
						}
					}
					if(targets.isEmpty())
						return true;
					match.edgeMap.put(e, targets);
				}else{
					for(Edge sift : match.key.edges){
						for(Edge edge : match.value.edges){
							if(match.isEdgeNeeded(sift, edge))
								match.addEdge(sift, edge);
						}
						if(!match.edgeMap.containsKey(sift))
							return true;
					}
					for(Node sift : match.key.nodes){
						for(Node edge : match.value.nodes){
							if(match.isNodeNeeded(sift, edge))
								match.addNode(sift, edge);
						}
						if(!match.nodeMap.containsKey(sift))
							return true;
					}
				}
			}
		}
		List<Morphism> result = match.findMatch();
		int a = 0;
		for(Morphism m : result)
			Utils.putIntoFile(m.toDot(rule.right, graph), graph.name + "_check_match_" + rule.name + a++);
		return result.size() == 0;
	}

	protected Graph copyGraph(Map<Node, Node> copied, Graph graph, Map<Node, List<Node>> reverseV, Map<Edge, List<Edge>> reverseE, Rule rule, Morphism m) {
		Map<Node, Node> assist = new HashMap<Node, Node>();
		Graph target = new Graph();
		for(Node node : graph.nodes){
			List<Node> value = reverseV.get(node);
			if(value == null || rule.commonLV.contains(value.get(0))){
				Node n = new Node(node);
				assist.put(node, n);
				target.addNode(n);
				if(value != null){
					for(Node iter : value){
						copied.put(iter, n);
						copied.put(rule.getCommonNodeMap(iter), n);
					}
				}
			}
		}
		for(Edge edge : graph.edges){
			List<Edge> value = reverseE.get(edge);
			if(value == null || rule.commonLE.contains(value.get(0))){
				Edge n = new Edge(assist.get(edge.src), assist.get(edge.trg), edge.name);
				target.edges.add(n);
			}
		}
		return target;
	}

	public void transform(Graph graph) {
		int matched = 0;
		String name = new String(graph.name);
		for(Rule rule : rules){
			Match match = new Match(rule.left, graph);
			List<Morphism> matches = match.findMatch();
			for(Morphism m : matches){
				graph.name = name + "_" + rule.name;
				Utils.putIntoFile(m.toDot(rule.left, graph),  graph.name + "_m_" + rule.name);
				if(checkMorphism(rule, match, m, rule.right, graph) == false)
					continue;
				++matched;
				Graph result = apply(graph, rule, m);
				if(result != null){
					GraphHash hash = result.getGraphHash();
					List<Graph> graphs = spaces.get(hash);
					boolean iso = false;
					if(graphs != null){
						for(Graph iter : graphs){
							if(result.iso(iter)){
								iso = true;
								break;
							}
						}
					}else
						graphs = new ArrayList<Graph>();
					if(!iso){
						graphs.add(result);
						if(graphs.size() == 1)
							spaces.put(hash, graphs);
						//MODIFIED: commented out since we only need to apply the reule once
//						transform(result);
					}
				}
			}
		}
		if(matched == 0)
			results.add(graph);
	}

	public List<Graph> xform(Graph graph){
		List<Graph> init = new ArrayList<Graph>();
		init.add(graph);
		spaces.put(graph.getGraphHash(), init);
		if(rules.isEmpty())
			return results;
		transform(graph); 
		return results;
	}
}
