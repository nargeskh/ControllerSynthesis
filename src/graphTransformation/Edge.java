package graphTransformation;


public class Edge {
	public String name;
	Node src;
	Node trg;
	public Edge(Node findNode, Node findNode2, String name) {
		this.name = name;
		setSource(findNode);
		setTarget(findNode2);
	}
	public void setSource(Node source){
		src = source;
		src.outs.add(this);
	}
	public void setTarget(Node target){
		trg = target;
		trg.ins.add(this);
	}
	public String toNameString(){
		return "" + src.name + "->" + trg.name + ":" + name;
	}
	@Override
	public String toString(){
		return "" + src.index + "->" + trg.index + ":" + name;
	}
}
