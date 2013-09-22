package graphTransformation;

public class ShapeEdge extends Edge {

	private boolean isCyclic = false;

	public ShapeEdge(Node findNode, Node findNode2, String name) {
		super(findNode, findNode2, name);
	}

	public ShapeEdge(ShapeNode src, ShapeNode trg, String name, boolean b) {
		this(src, trg, name);
		isCyclic  = b;
	}
	
}
