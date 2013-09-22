package graphTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



public class ShapeTransform  extends GraphTransform{

	public ShapeTransform(GraphTransform xform) {
		rules = xform.rules;
	}

	@Override
	public Graph apply(Graph s, Rule rule, Morphism p) {
		if(s instanceof Shape){
			Shape splus = new Shape();
			splus.x = rule.left.x;
			splus.y = rule.left.y;
			splus.name = s.name;
			Collection<Node> nodes = p.nodeMap.values();
			List<Node> toBeSplitedNodes = new ArrayList<Node>();
			Map<Node, ShapeNode> copyMap = new HashMap<Node, ShapeNode>();
			for(Node iter : s.nodes){
				if(nodes.contains(iter) && ((ShapeNode)iter).count > 1)
					toBeSplitedNodes.add(iter);
				else{
					ShapeNode next = new ShapeNode(iter);
					splus.addNode(next);
					copyMap.put(iter, next);
				}
			}
			List<ShapeNode[]> splitedNodes = new ArrayList<ShapeNode[]>();
			for(Node iter : toBeSplitedNodes){
				ShapeNode[] two = new ShapeNode[2];
				two[0] = new ShapeNode(iter);
				two[0].count = 1;
				two[1] = new ShapeNode(iter);
				two[1].count = ((ShapeNode)iter).count - 1;
				splitedNodes.add(two);
			}
			for(Edge iter : s.edges){
				if(toBeSplitedNodes.contains(iter.src) || toBeSplitedNodes.contains(iter.trg))
					continue;
				else
					splus.addEdgeWithoutChangeMulitplicty(copyMap.get(iter.src), copyMap.get(iter.trg), iter.name);
			}
			Morphism idL = new Morphism();
			for(Entry<Edge, Edge> entry : p.edgeMap.entrySet()){
				Edge value = entry.getValue();
				Node src = value.src, trg = value.trg;
				ShapeNode nSrc = (ShapeNode) src, nTrg = (ShapeNode) trg;
				if(toBeSplitedNodes.contains(src)){
					nSrc = splitedNodes.get(toBeSplitedNodes.indexOf(src))[0];
					if(!splus.nodes.contains(nSrc)){
						idL.nodeMap.put(entry.getKey().src, nSrc);
						splus.nodes.add(nSrc);
					}
				}
				if(toBeSplitedNodes.contains(trg)){
					nTrg = splitedNodes.get(toBeSplitedNodes.indexOf(trg))[0];
					if(!splus.nodes.contains(nTrg)){
						idL.nodeMap.put(entry.getKey().trg, nTrg);
						splus.nodes.add(nTrg);
					}
				}
				Edge edge = splus.existEdge(nSrc, nTrg, value.name);
				if(edge == null){
					Edge n = new Edge(nSrc, nTrg, value.name);
					splus.edges.add(n);
					idL.edgeMap.put(entry.getKey(), n);
				}
			}
			for(Node node : rule.left.nodes){
				ShapeNode n = new ShapeNode(node);
				splus.nodes.add(n);
				idL.put(node, n);
				n.insm = new  HashMap<String, Integer>(((ShapeNode)p.getNodeMap(node)).insm);
			}
			splus.initialEdgeWithoutChangeMultiplicity(rule.left.edges, idL);
			mixShape(splus, idL, rule.left, p, (Shape) s);
			Utils.putIntoFile(splus.toDot(), splus.name + "_mixed");

			Shape result = applyShape(splus, rule, idL);
			if(result != null)
				return result;
		}
		return null;
	}


	private Shape applyShape(Shape splus, Rule rule, Morphism mor) {
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
			Shape target = copyShape(copied, splus, reverseV, reverseE, rule, mor);
			target.x = splus.x;
			target.y = splus.y;
			//Add elements
			if(target.name.equals("shape_buffer_Put_Get"))
				System.out.println("here");
			for(Node node : rule.addV){
				Node n = new ShapeNode(node);
				copied.put(node, n);
				n.index = splus.getNextIndex();
				target.addNode(n);
			}
			for(Edge edge : rule.addE){
				ShapeNode node = (ShapeNode) copied.get(edge.trg);
				String label = edge.name;
				if(rule.commonRV.contains(edge.trg)){
					Integer mul = node.insm.get(label);
					if(mul != null){
						mul++;
					}else{
						mul = new Integer(1);
						node.insm.put(label, mul);
					}
				}
				Edge e = new Edge(copied.get(edge.src), copied.get(edge.trg), edge.name);
				target.edges.add(e);
			}
			Utils.putIntoFile(target.toDot(), target.name + "_applied");
			return target.partShape();
		}
		return null;
	}

	private Shape copyShape(Map<Node, Node> copied, Shape splus,
			Map<Node, List<Node>> reverseV, Map<Edge, List<Edge>> reverseE,
			Rule rule, Morphism mor) {
		Map<Node, Node> assist = new HashMap<Node, Node>();
		Shape target = new Shape();
		target.name = splus.name;
		for(Node node : splus.nodes){
			List<Node> value = reverseV.get(node);
			if(value == null || rule.commonLV.contains(value.get(0))){
				Node n = new ShapeNode((ShapeNode)node);
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
		for(Edge edge : splus.edges){
			List<Edge> value = reverseE.get(edge);
			if(value == null || rule.commonLE.contains(value.get(0))){
				Edge n = new Edge(assist.get(edge.src), assist.get(edge.trg), edge.name);
				target.edges.add(n);
			}
		}
		for(Edge edge : rule.removeE){
			if(!rule.removeV.contains(edge.trg)){
				ShapeNode node = (ShapeNode) copied.get(edge.trg);
				String label = edge.name;
				Integer mul = node.insm.get(label);
				if(mul != null){
					mul--;
					if(mul == 0)
						node.insm.remove(label);
				}
			}
		}
		return target;
	}

	private void mixShape(Shape splus, Morphism idL, Graph left, Morphism p, Shape s) {
		Morphism copieMor = new Morphism();
		Map<Node, List<Node>> rp = Utils.reverseMap(p.nodeMap);
		Map<Edge, List<Edge>> rpe = Utils.reverseMap(p.edgeMap);
		for(Node node : s.nodes){
			ShapeNode shapeNode = (ShapeNode) node;
			boolean contained = rp.containsKey(shapeNode);
			boolean isMultiOne = shapeNode.count == 0;
			ShapeNode copied = null;
			if(!contained || !isMultiOne){
				copied = new ShapeNode(shapeNode);
				if(contained && !isMultiOne){
					copied.count--;
				}
				if(splus.findNode(copied.index) != null)
					copied.index = splus.getNextIndex();
				splus.nodes.add(copied);
			}
			copieMor.nodeMap.put(node, copied);
		}
		for(Edge edge : s.edges){
			boolean srcCont = rp.containsKey(edge.src);
			boolean tarCont = rp.containsKey(edge.trg);
			ShapeNode src = (ShapeNode) copieMor.getNodeMap(edge.src);
			ShapeNode trg = (ShapeNode) copieMor.getNodeMap(edge.trg);
			if(src != null && trg != null)
				splus.addEdgeWithoutChangeMulitplicty(src, trg, edge.name);
			if(srcCont && !tarCont){
				if(trg != null)
					for(Node node : rp.get(edge.src)){
						splus.addEdgeWithoutChangeMulitplicty((ShapeNode) idL.getNodeMap(node), trg, edge.name);
					}
			}else if(!srcCont && tarCont){
				if(src != null)
					for(Node node : rp.get(edge.trg)){
						splus.addEdgeWithoutChangeMulitplicty(src, (ShapeNode) idL.getNodeMap(node), edge.name);
					}

			}else if(srcCont && tarCont){
				if(!rpe.containsKey(edge)){
					if(edge.src == edge.trg){
						if(trg != null)
							for(Node node : rp.get(edge.src)){
								splus.addEdgeWithoutChangeMulitplicty((ShapeNode) idL.getNodeMap(node), trg, edge.name);
							}
					}
					else {
						if(trg != null){
							for(Node node : rp.get(edge.src)){
								splus.addEdgeWithoutChangeMulitplicty((ShapeNode) idL.getNodeMap(node), trg, edge.name);
							}
						}
						if(src != null){
							for(Node node : rp.get(edge.trg)){
								splus.addEdgeWithoutChangeMulitplicty(src, (ShapeNode) idL.getNodeMap(node), edge.name);
							}
						}
						for(Node node : rp.get(edge.src)){
							for(Node node1 : rp.get(edge.trg)){
								splus.addEdgeWithoutChangeMulitplicty((ShapeNode) idL.getNodeMap(node), (ShapeNode) idL.getNodeMap(node1), edge.name);
							}
						}
					}
				}else{
					if(src != null)
						for(Node node : rp.get(edge.trg)){
							splus.addEdgeWithoutChangeMulitplicty(src, (ShapeNode) idL.getNodeMap(node), edge.name);
						}
				}
			}
		}
	}
}
