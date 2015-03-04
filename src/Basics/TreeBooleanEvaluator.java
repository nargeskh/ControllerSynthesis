package basics;

//import Expression;
import graphLTS.Event;
import graphLTS.gState;
import graphLTS.gTransition;
import graphTransformation.Graph;
import graphTransformation.Match;
import graphTransformation.Morphism;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.astesana.javaluator.AbstractEvaluator;
import net.astesana.javaluator.BracketPair;
import net.astesana.javaluator.Operator;
import net.astesana.javaluator.Parameters;

public class TreeBooleanEvaluator extends AbstractEvaluator<String> {
	/** The logical AND operator.*/
	final static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
	/** The logical OR operator.*/
	final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);

	final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);

	public gTransition context = new gTransition();

	private static final Parameters PARAMETERS;

	static {
		// Create the evaluator's parameters
		PARAMETERS = new Parameters();
		// Add the supported operators
		PARAMETERS.add(AND);
		PARAMETERS.add(NEGATE);
		PARAMETERS.add(OR);
		// Add the parentheses
		PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
	}
	public TreeBooleanEvaluator() {
		super(PARAMETERS);
	}

	@Override
	protected String toValue(String literal, Object evaluationContext) {
		return literal;		
	}

	private boolean getValue(String literal) {

		if (literal.endsWith("=true") || "true".equals(literal))
			return true;
		else if ("false".equals(literal) || literal.endsWith("=false")) 
			return false;

		// returns true if it is identical to the event of context and
		// its graph constraints are satisfied by the source state
		String e = context.lab.recID + "." + context.lab.name +  "[" + context.lab.args + "]";

		//System.out.println ("transition:"+ e + " graph: " + context.src.g.toString() + "\n expressions: " + literal);
		//System.out.println ("transition:"+ e + ".......... expressions: " + literal);

		boolean satGraphConst = false;
		if(literal.contains("|"))
		{
			Graph g = new Graph("g",literal);
			Match match = new Match(g, context.src.g);
			List<Morphism> matches = match.findMatch();
			satGraphConst = (matches.size()>0);
	//		System.out.println ("expressions: " + literal + "      " + Boolean.toString(satGraphConst));
		}

		if (satGraphConst || e.equals(literal))
			return true;

		else if (!e.equals(literal)) return false;
		throw new IllegalArgumentException("Unknown literal : "+literal);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected String evaluate(Operator operator, Iterator<String> operands,
			Object evaluationContext) {
		List<String> tree = (List<String>) evaluationContext;
		String eval = "";
		String o1 = operands.next();
		Boolean result;
		if (operator == OR) {
			String o2 = operands.next();
			result = getValue(o1) || getValue(o2);
			eval = "("+o1+" "+operator.getSymbol()+" "+o2+")=" + result;
		} else if (operator == AND) {
			String o2 = operands.next();
			result = getValue(o1) && getValue(o2);
			eval = "("+o1+" "+operator.getSymbol()+" "+o2+")=" + result;
		} else if (operator == NEGATE) {
			result = !getValue(o1);
			eval = "("+ operator.getSymbol() + o1+")=" + result;
		} else
		{
			throw new IllegalArgumentException();
		}
		tree.add(eval);
		return eval;
	}

	/*	public static void main(String[] args) {
		TreeBooleanEvaluator evaluator = new TreeBooleanEvaluator();
		evaluator.context = new gTransition(new Event(ACTIVE.INTERNAL, "ana", "req", "x"), new gState(), new gState());

		eval(evaluator, "T && ( F || ( F && T ) )");
		eval(evaluator, "(ana.req[x] && true) || ( false && true )");
		eval(evaluator, "ana.req[x] && T");
	}*/

	public static List<String> eval(TreeBooleanEvaluator evaluator, String expression) {
		List<String> sequence = new ArrayList<String>();
		evaluator.evaluate(expression, sequence);
		/*System.out.println ("Evaluation sequence for :"+expression);
		for (String string : sequence) {
			System.out.println (string);
		}
		System.out.println ();
		*/
		return sequence;
	}
}