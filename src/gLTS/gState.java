package gLTS;

import graphTransformation.Graph;

public class gState {

	public Graph g = new Graph();
	public State s = new State();

	public gState(State s1, Graph model) {
		this.g = model;
		this.s = s1;
		// TODO Auto-generated constructor stub
	}

	public gState() {
		// TODO Auto-generated constructor stub
	}

	public gState copy() {

		return new gState(new State(this.s.ID), g);
	}

}
