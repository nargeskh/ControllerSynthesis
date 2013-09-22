package graphTransformation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Graph {

	Comparator<Node> comparator = null;
	List<Edge> edges = null;
	private GraphHash hash;
	String name;
	public List<Node> nodes = null;
	boolean sorted = false;
	int x = 0, y = 0;
	public Graph() {
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}
	public Graph(List<Node> keepedNodeInG, List<Edge> keepedEdgeInG) {
		Map<Node, Node> copied = new HashMap<Node, Node>();
		for(Node node : keepedNodeInG){
			Node n = new Node(node);
			copied.put(node, n);
			addNode(n);
		}
		for(Edge edge : keepedEdgeInG){
			Edge n = new Edge(copied.get(edge.src), copied.get(edge.trg), edge.name);
			edges.add(n);
		}
	}
	public Graph(String n, String string){
		name = n;
		int sep = string.indexOf(')');
		if(sep != -1){
			int dot = string.indexOf(',');
			try{
				x = Integer.parseInt(string.substring(1, dot));
				y = Integer.parseInt(string.substring(dot + 1, sep));
			}catch(NumberFormatException e){
				System.out.println(e + " when parsing graph");
			}
		}

		int begin = sep + 1;
		sep = string.indexOf('|', begin);
		if(sep == -1)
			System.out.println("Graph Malformation");
		else{
			String[] nodes = string.substring(begin, sep).split(";");
			String[] edges = string.substring(sep + 1).split(";");
			this.nodes = getNodes(nodes);
			//	if(sep < string.length()-1)
			this.edges = getEdges(this.nodes, edges);
		}
	}
	public void addNode(Node node) {
		if(nodes == null)
			nodes = new ArrayList<Node>();
		if(node != null)
			nodes.add(node);
	}
	public Edge findEdge(String cur) {
		int sep = cur.indexOf(":");
		if(sep == -1){
			System.out.println("Malformation when parsing edges");
			return null;
		}
		String edge = cur.substring(0, sep);
		String name = cur.substring(sep + 1);
		int sep2 = cur.indexOf("->");
		int src = -1, trg = -1;
		try{
			src = Integer.parseInt(edge.substring(0, sep2));
			trg = Integer.parseInt(edge.substring(sep2 + 2));
		}catch(NumberFormatException e){
			System.out.println(e + " when parsing edges");
			return null;
		}
		Node source = findNode(src), target = findNode(trg);
		if(source == null || target == null)
			return null;
		for(Edge iter : source.outs){
			if(iter.name.equals(name))
				return iter;
		}
		return null;
	}
	public String graphIMappingDelNode(int cur) {

		String result = "";

		for(Node node : this.nodes){
			if(node.index != cur)
				result += Integer.toString(node.index) + "<->" + Integer.toString(node.index) + ";";
		}

		result += "|";

		for(Edge edge : this.edges){
			if(edge.src.index != cur &&
					edge.trg.index != cur)
			{
				String estr = Integer.toString(edge.src.index) + "->" + Integer.toString(edge.trg.index) + ":" + edge.name;
				result += estr + "<->" + estr + ";";
			}
		}
		return result;

	}


	public Node findNode(int src) {
		if(nodes == null)
			return null;
		for(Node node : nodes)
			if(node.index == src)
				return node;
		return null;
	}
	public List<Edge> getEdges(List<Node> node, String[] edges2) {
		if(node == null)
			return null;
		List<Edge> results = new ArrayList<Edge>();
		for (int i = 0; i < edges2.length; i++) {
			String cur = edges2[i];
			int sep = cur.indexOf(':');
			if(sep == -1){
				System.out.println("Malformation when parsing edges");
				return null;
			}
			String edge = cur.substring(0, sep);
			String name = cur.substring(sep + 1);
			int sep2 = cur.indexOf("->");
			int src = -1, trg = -1;
			try{
				src = Integer.parseInt(edge.substring(0, sep2));
				trg = Integer.parseInt(edge.substring(sep2 + 2));
			}catch(NumberFormatException e){
				System.out.println(e + " when parsing edges");
				return null;
			}
			Node source = findNode(src), target = findNode(trg);
			if(source == null || target == null)
				return null;
			results.add(new Edge(source, target, name));
		}
		return results;

	}

	public GraphHash getGraphHash() {
		if(hash == null)
			hash = new GraphHash(this);
		return hash;
	}
	public int getNextIndex() {
		Collections.sort(nodes, new Comparator<Node>(){
			@Override
			public int compare(Node o1, Node o2) {
				return  o1.index - o2.index;
			}}	
				);
		int index = 0;
		for(Node node : nodes){
			if(index < node.index)
				return index;
			else if(index == node.index)
				++index;
		}

		return index;
	}
	private List<Node> getNodes(String[] nodes2) {
		List<Node> results = new ArrayList<Node>();
		for (int i = 0; i < nodes2.length; i++) {
			String cur = nodes2[i];
			int sep = cur.indexOf(':');
			if(sep == -1){
				System.out.println("Malformation when parsing nodes");
				return null;
			}
			int index = -1;
			try{
				index = Integer.parseInt(cur.substring(0, sep));
			}catch(NumberFormatException e){
				System.out.println(e + " when parsing nodes");
				return null;
			}
			String name = cur.substring(sep + 1);
			results.add(new Node(index, name));
		}
		return results;
	}
	public boolean iso(Graph iter) {
		GraphHash hash = iter.getGraphHash();
		Match match = new Match();
		match.initalize(iter, this);
		for(Entry<GraphHash.Bi, List<Node>> entry  : hash.back.entrySet()){
			GraphHash.Bi key = entry.getKey();
			List<Node> node = entry.getValue();
			List<Node> values = this.hash.back.get(key);
			for(Node i : node){
				match.nodeMap.put(i, values);
			}
		}

		for(Edge sift : match.key.edges){
			for(Edge edge : match.value.edges){
				if(match.isEdgeNeeded(sift, edge))
					match.addEdge(sift, edge);
			}
			if(!match.edgeMap.containsKey(sift))
				return false;
		}
		return match.findISOMatch().size() != 0;
	}
	public void sortNodes() {
		if(sorted == false){
			if(comparator == null)
				comparator = new Comparator<Node>(){
				@Override
				public int compare(Node o1, Node o2) {
					int result = (o1.index - o2.index)/x;
					if(result == 0)
						result = o1.index - o2.index;
					return result;
				};
			};
			Collections.sort(nodes, comparator);
		}
	}
	public String toDot(){
		if(nodes == null || edges == null)
			return null;
		StringBuffer result = new StringBuffer();
		result.append("digraph G{\n");
		toNodeDot(result);
		toEdgeDot(result);
		result.append("}");
		return result.toString();
	}
	public void toEdgeDot(StringBuffer result){
		int size = edges.size();
		for (int i = 0; i < size; i++) {
			result.append("\t");
			Edge cur = edges.get(i);
			result.append(cur.src.index);
			result.append("->");
			result.append(cur.trg.index);
			result.append("[label=\"");
			result.append(cur.name);
			result.append("\"];\n");
		}
	}
	public void toNodeDot(StringBuffer result){
		int size = nodes.size();
		sortNodes();
		result.append("\t{rank = same; ");
		for (int i = 0; i < size; i++) {
			Node cur = nodes.get(i);
			result.append(cur.index);
			result.append("[label=\"");
			result.append(cur.name);
			result.append("\"]; ");
			if(i + 1 < size && cur.index / x != nodes.get(i + 1).index / x)
				result.append("};\n\t{rank = same; ");
		}
		result.append("};\n");
	}
	@Override
	public String toString(){
		if(nodes == null || edges == null)
			return null;
		StringBuffer result = new StringBuffer();
		int size = nodes.size();
		for (int i = 0; i < size; i++) {
			Node cur = nodes.get(i);
			result.append(cur.index);
			result.append(":");
			result.append(cur.name);
			if(i != size - 1)
				result.append(";");
		}
		result.append("|");
		size = edges.size();
		for (int i = 0; i < size; i++) {
			Edge cur = edges.get(i);
			result.append(cur.src.index);
			result.append("->");
			result.append(cur.trg.index);
			result.append(":");
			result.append(cur.name);
			if(i != size - 1)
				result.append(";");
		}
		return result.toString();
	}

	public String generateIMapping()
	{
		String result = "";
		Iterator<Node> niter = this.nodes.iterator();
		while(niter.hasNext())
		{
			Node n = niter.next();
			result += Integer.toString(n.index) + "<->" + Integer.toString(n.index) + ";";
		}

		result = result.substring(0, result.length()-1) + "|";

		Iterator<Edge> eiter = this.edges.iterator();
		while(eiter.hasNext())
		{
			Edge e = eiter.next();
			String estr = Integer.toString(e.src.index) + "->" + Integer.toString(e.trg.index) + ":" + e.name;
			result += estr + "<->" + estr + ";";
		}
		return result.substring(0, result.length()-1);
	}
}
