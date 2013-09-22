package gLTS;


public class Transition {
	public Transition(Event lab, State src, State trg,String o) {
		super();
		this.lab = lab;
		this.src = src;
		this.trg = trg;
		this.owner = o;
	}
	public Transition() {
		// TODO Auto-generated constructor stub
	}
	public Event lab;
	public State src;	
	public State trg;	
	public String owner;
}

