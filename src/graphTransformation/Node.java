package graphTransformation;
import java.util.ArrayList;
import java.util.List;


public class Node {
	int index;
	List<Edge> ins = new ArrayList<Edge>(), outs = new ArrayList<Edge>();
	String name;
	public Node(int index2, String name2) {
		index = index2;
		name = name2;
	}
	public Node(Node key) {
		index = key.index;
		name = key.name;
	}
	@Override
	public String toString(){
		return "" + index + ":" + name;
	}
}
