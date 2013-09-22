package graphLTS;


public class gTransition {
	public gTransition(Event lab, gState src, gState trg) {
		super();
		this.lab = lab;
		this.src = src;
		this.trg = trg;
	}
	public gTransition() {
		// TODO Auto-generated constructor stub
	}
	
	public Event lab;
	public gState src;	
	public gState trg;	
}