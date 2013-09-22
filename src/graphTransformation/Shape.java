package graphTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;



public class Shape extends Graph{

	public Shape() {
		super();
	}
	public Shape(Graph graph){
		super();
		x = graph.x;
		y = graph.y;
		name = "shape_" + graph.name;
		Map<Node, Node> hash = new HashMap<Node, Node>();
		for(Node node : graph.nodes){
			ShapeNode n = new ShapeNode(node);
			nodes.add(n);
			hash.put(node, n);
		}
		initialEdge(graph.edges, hash);
		compose();
		Utils.putIntoFile(toDot(), name);
	}
	public Shape(Shape shape) {
		x = shape.x;
		y = shape.y;
		name = shape.name;
		nodes = new ArrayList<Node>(shape.nodes.size());
		for(Node node : shape.nodes){
			nodes.add(new ShapeNode(node));
		}
	}
	public Shape(Shape shape, int size) {
		x = shape.x;
		y = shape.y;
		name = shape.name;
		nodes = new ArrayList<Node>(size);
	}
	Edge existEdge(Node src, Node trg, String name) {
		for(Edge e : trg.ins)
			if(e.src == src && name.equals(e.name))
				return e;
		return null;
	}
	public void addEdge(ShapeNode src, ShapeNode trg, String name){
		Edge edge = existEdge(src, trg, name);
		if(edge == null)
			edges.add(new Edge(src, trg, name));
		increase(trg.insm, name);
		increase(src.outsm, name);
	}
	private void increase(Map<String, Integer> insm, String name) {
		Integer value = insm.get(name);
		if(null != value)
			value++;
		else
			insm.put(name, new Integer(1));
	}
	private void decrease(Map<String, Integer> insm, String name) {
		Integer value = insm.get(name);
		value--;
		if(0 != value)
			insm.remove(name);
	}
	public void initialEdge(List<Edge> edges, Map<Node, Node> hash){
		for(Edge e : edges)
			addEdge((ShapeNode) hash.get(e.src), (ShapeNode) hash.get(e.trg), e.name);
	}
	public void composeEdge(List<Edge> edges, Map<Node, Node> hash){
		List<Edge> newAdded = new ArrayList<Edge>();
		for(Edge e : edges)	{
			ShapeNode src = (ShapeNode) hash.get(e.src), trg = (ShapeNode) hash.get(e.trg);
			Edge edge = existEdge(src, trg, e.name);
			if(edge == null)
				newAdded.add(new ShapeEdge(src, trg, e.name, e.src == e.trg));
		}
		edges.addAll(newAdded);
	}

	public void compose(){
		Map<Node, List<Node>> hash = new TreeMap<Node, List<Node>>();
		for(Node node : nodes){
			List<Node> value = hash.get(node);
			if(value == null)
				value = new ArrayList<Node>();
			value.add(node);
			if(value.size() == 1)
				hash.put(node, value);
		}
		if(hash.size() == this.nodes.size()) return;
		Map<Node, Node> copyMap = new HashMap<Node, Node>();
		for(Entry<Node, List<Node>>  entry: hash.entrySet()){
			ShapeNode n = (ShapeNode)entry.getKey();
			int m = 0;
			for(Node node : entry.getValue()){
				ShapeNode shape = (ShapeNode) node;
				m += shape.count;
				copyMap.put(node, n);
			}
			n.count = m;
		}
		composeEdge(edges, copyMap);
		for(Entry<Node, List<Node>>  entry: hash.entrySet()){
			ShapeNode n = (ShapeNode)entry.getKey();
			for(Node node : entry.getValue()){
				if(node != n)
					remove(node);
			}
		}
	}
	private void remove(Node node) {
		for(Edge edge : node.ins){
			Node src = edge.src;
			if(src != node)
				src.outs.remove(edge);
			edges.remove(edge);
		}
		for(Edge edge : node.outs){
			Node src = edge.trg;
			if(src != node)
				src.ins.remove(edge);
			edges.remove(edge);
		}
		nodes.remove(node);
	}
	public Shape partShape(){
		Map<Node, List<Node>> hash = new TreeMap<Node, List<Node>>();
		for(Node node : nodes){
			List<Node> value = hash.get(node);
			if(value == null)
				value = new ArrayList<Node>();
			value.add(node);
			if(value.size() == 1)
				hash.put(node, value);
		}
		if(hash.size() == this.nodes.size())
			return this;
		Map<Node, Node> copyMap = new HashMap<Node, Node>();
		Shape copied = new Shape(this, hash.size());
		for(Entry<Node, List<Node>>  entry: hash.entrySet()){
			ShapeNode n = new ShapeNode((ShapeNode)entry.getKey());
			Integer m = new Integer(0);
			for(Node node : entry.getValue()){
				ShapeNode shape = (ShapeNode) node;
				m += shape.count;
			}
			if(entry.getValue().size() > 1)
				n.count = m;
			copied.nodes.add(n);
			for(Node s : entry.getValue())
				copyMap.put(s, n);
		}
		copied.initialEdge(edges, copyMap);
					
					Utils.putIntoFile(copied.toDot(), copied.name + "_parted");
					return copied;
	}
	void initialEdgeWithoutChangeMultiplicity(List<Edge> edges,	Map<Node, Node> hash) {
		for(Edge edge : edges)
			addEdgeWithoutChangeMulitplicty((ShapeNode) hash.get(edge.src), (ShapeNode) hash.get(edge.trg), edge.name);
	}
	@Override
	public void toNodeDot(StringBuffer result){
		int size = nodes.size();
		sortNodes();
		result.append("\t{rank = same; ");
		for (int i = 0; i < size; i++) {
			Node cur = nodes.get(i);
			result.append(cur.index);
			result.append("[label=\"");
			result.append(cur.name);
			result.append("\" ");
			if(cur instanceof ShapeNode){
				ShapeNode node = (ShapeNode) cur;
				if(node.count > 1)
					result.append(",color=lightblue,style=filled");
			}
			result.append("]; ");
			if(i + 1 < size && cur.index / x != nodes.get(i + 1).index / x)
				result.append("};\n\t{rank = same; ");
		}
		result.append("};\n");
	}

	@Override
	public void toEdgeDot(StringBuffer result){
		int size = edges.size();
		for (int i = 0; i < size; i++) {
			result.append("\t");
			Edge cur = edges.get(i);
			result.append(cur.src.index);
			result.append("->");
			result.append(cur.trg.index);
			result.append("[label=\"");
			result.append(cur.name + "\"");
			if(((ShapeNode)cur.src).count > 1 && ((ShapeNode)cur.trg).insm.get(cur.name) > 1)
				result.append(",color=lightblue");
			result.append("];\n");
		}
	}
	public void addEdgeWithoutChangeMulitplicty(ShapeNode src, ShapeNode trg, String name) {
		Edge edge = existEdge(src, trg, name);
		if(edge == null)
			edges.add(new Edge(src, trg, name));
	}
	public void initialEdgeWithoutChangeMultiplicity(List<Edge> edges, Morphism idL) {
		Map<Node, Node> hash = idL.nodeMap;
		for(Edge edge : edges){
			Node src = hash.get(edge.src);
			Node trg = hash.get(edge.trg);
			Edge cur = existEdge(src, trg, edge.name);
			if(cur == null){
				cur = new Edge(src, trg, edge.name);
				this.edges.add(cur);
			}
			idL.edgeMap.put(edge, cur);
		}
	}
}
