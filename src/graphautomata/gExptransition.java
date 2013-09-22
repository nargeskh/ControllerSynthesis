package graphautomata;

import gLTS.gState;
import gLTS.gTransition;
import gLTS.State;

public class gExptransition {

	public String label;
	public State src = new State();
	public State trg = new State();

	public gExptransition(String expr, State s0, State s1) {
		// TODO Auto-generated constructor stub
		src = s0;
		trg = s1;
		label = expr;
	}

	//check the entailement of (s1,t,s2) |= label 
	public boolean evaluateExpr(gState s1, gTransition t, gState s2)
	{
		return false;
	}
}
