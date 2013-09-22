package graphTransformation;

import static java.lang.System.out;

import java.util.List;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Graph graph = new Graph("cache", "(3,2)1:s0;2:s1;3:s2|1->3:n;1->2:n");
		out.println("The main graph:\n" + graph.toDot());

		String L = "(3,2)1:s0;2:s1;3:s2|1->2:n;1->3:n";
		out.println("Left graph:\n" + new Graph("left",L).toDot());
		String R = "(4,2)1:s0;2:s1;3:s2;4:s3|1->2:n;1->3:n";
		out.println("Right graph:\n" + new Graph("right",R).toDot());

		String I = "1<->1;2<->2;3<->3|1->3:n<->1->3:n";

		GraphTransform xform = new GraphTransform();

		Rule rule = new Rule("ruleX", L, R, I);
		Match match = new Match(rule.left, graph);
		List<Morphism> matches = match.findMatch();
		Graph result = xform.apply(graph, rule, matches.get(0));
		out.println("The addition graph:\n" + result.toDot());
		
		I = "1<->1;2<->2|1->2:n<->1->2:n";
		String delR = "(2,1)1:s0;2:s1|1->2:n";
		Rule delRule = new Rule("ruleY", L, delR, I);
		match = new Match(delRule.left, graph);
		matches = match.findMatch();
		result = xform.apply(graph, delRule, matches.get(0));
		out.println("The removal graph:\n" + result.toDot());


		/*		Graph graph1 = new Graph("buffer", "(3,3)1:C;3:C;5:C;7:C;4:B|1->5:n;5->7:n;7->3:n;3->1:n;4->1:f;4->3:l;3->4:e;1->4:e;5->4:e;7->4:e");


		xform.addRule(new Rule("Get", "(2,2)0:C;1:O;2:B;3:C|0->1:v;0->3:n;2->0:f", "(2,2)0:C;2:B;3:C|0->2:e;0->3:n;2->3:f", "0<->0;2<->2;3<->3|0->3:n<->0->3:n"));
		xform.addRule(new Rule("Put", "(2,2)0:C;2:B;3:C|3->2:e;0->3:n;2->0:l", "(2,2)0:C;1:O;2:B;3:C|0->3:n;2->3:l;3->1:v;", "0<->0;2<->2;3<->3|0->3:n<->0->3:n"));
		//		xform.xform(graph);
		graph.name = "buffer";
		ShapeTransform sform = new ShapeTransform(xform);
		Shape init = new Shape(graph);
		sform.xform(init);
		 */}

}
