package graphLTS;

import basics.ACTIVE;

public class Event {

	public Event(ACTIVE type, String rID, String name, String args) {
		super();
		this.type = type;
		this.name = name;
		this.args = args;
		this.recID = rID;
		//	this.owner = o;
	}
	public Event() {
		// TODO Auto-generated constructor stub
	}
	public ACTIVE type;
	public String recID;
	public String name;	
	public String args;

	//	public String owner;

	public String print_event()
	{
		return recID + "." + name + "(" +  args + ")" + (type==ACTIVE.INPUT ? "?":type==ACTIVE.OUTPUT?"!":"") ;
	}
	public boolean isReconfigAction() {

		return name.equals("add") ||
				name.equals("del") ||
				name.equals("dis") ||
				name.equals("conn");
	}
}
