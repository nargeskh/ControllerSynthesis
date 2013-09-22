package graphLTS;

import static java.lang.System.out;
import graphTransformation.Graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import basics.ACTIVE;

public class LTS {
	public LTS(String iD, State inits, State na, ArrayList<State> states,
			ArrayList<State> initStates,
			ArrayList<State> removalStates,
			ArrayList<Transition> trans) {
		super();
		this.states = states;
		this.trans = trans;
		this.ID = iD;
		this.s0 = inits;
		this.NA = na;
		this.initStates = initStates;
		this.removalState = removalStates;
	}

	public LTS() {
		// TODO Auto-generated constructor stub
	}

	public LTS(String iD2, State nA2) {
		// TODO Auto-generated constructor stub
		this.ID = iD2;
		this.NA = nA2;
		this.s0 = nA2;
		this.states.add(nA2);
	}

	public State s0 = new State();
	public State NA = new State();
	public ArrayList<State> states = new ArrayList<State>();
	public ArrayList<State> initStates = new ArrayList<State>();
	public ArrayList<State> removalState = new ArrayList<State>();
	public ArrayList<Transition> trans = new ArrayList<Transition>();

	public String ID;

	public ArrayList<State> getStates() {
		return states;
	}

	public void setStates(ArrayList<State> states) {
		this.states = states;
	}

	public ArrayList<Transition> getTrans() {
		return trans;
	}

	public void setTrans(ArrayList<Transition> trans) {
		this.trans = trans;
	}

	// look up for a transition with a label with the same ID, name and arg as e
	// it also checks that both components are connected

	public static Transition hasTrans(Transition it, ArrayList<Transition> ts, 
			Graph model, ArrayList<LTS> lTSs ) {

		Iterator<Transition> iter = ts.iterator();
		Event e = it.lab;
		while (iter.hasNext()) {
			Transition t = iter.next();
			Event e1 = t.lab;

			String itEdgeID = Integer.toString(LTS.getOwnerID(it.owner, lTSs)+1);
			String tEdgeID = Integer.toString(LTS.getOwnerID(t.owner, lTSs)+1);
			/*		if(e.type==Active.INPUT)
				out.println("looking for transition " + itEdgeID +"->" + tEdgeID + ":" + it.owner + t.owner);
			else
				out.println("looking for transition " + tEdgeID +"->" + itEdgeID + ":" + t.owner + it.owner);
			 */
			if (!itEdgeID.equals(tEdgeID) &&
					e.args.compareTo(e1.args)==0 && (e.name.compareTo(e1.name)==0)
					&& (e.recID.compareTo(e1.recID)==0) &&
					(e.type==ACTIVE.INPUT? model.findEdge(itEdgeID +"->" + tEdgeID + ":" + it.owner + t.owner)!=null: 
						model.findEdge(tEdgeID +"->" + itEdgeID + ":" + t.owner + it.owner)!=null))
				return t;
		} 
		//	out.println("COULD NOT FIND ANY MACTH FOR " + it.lab.recID + "." + it.lab.name + " of " + it.owner );
		return null;
	}

	public boolean hasState(State s) {
		return states.contains(s);
	}

	public String draw() {
		String g = "digraph { \n";

		Iterator<Transition> iter = trans.iterator();
		while (iter.hasNext()) {
			Transition t = iter.next();
			g += (t.src.ID) + "->";
			g += (t.trg.ID);
			g += "[label= \"" + t.lab.print_event() + "\"];";
			// a -> b[label="0.2",weight="0.2"];

		}
		return g + "\n}";

	}


	public ArrayList<Transition> getOutTransof(State e) {
		ArrayList<Transition> res = new ArrayList<Transition>();

		Iterator<Transition> iter = trans.iterator();
		while (iter.hasNext()) {
			Transition t = iter.next();
			if (t.src == e)
				res.add(t);
		}
		return res;
	}

	public static ArrayList<Transition> union_trans
	(ArrayList<Transition> currt1, 
			ArrayList<Transition> currt2, String ID1, String ID2)
			{
		ArrayList<Transition> transition = new ArrayList<Transition>();

		Iterator<Transition> iter = currt1.iterator();
		while (iter.hasNext()) {
			Transition t1 = iter.next();
			transition.add(t1);
		}

		iter = currt2.iterator();
		while (iter.hasNext()) {
			Transition t1 = iter.next();
			transition.add(t1);
		}
		return transition;
			}

	public static  ArrayList<Transition> removeTrans
	(Transition t,ArrayList<Transition> transition)
	{

		Iterator<Transition> iter = transition.iterator();
		while (iter.hasNext()) {
			Transition t1 = iter.next();
			if(t1==t)
			{
				transition.remove(t1);
				return transition;
			}
		}
		return transition;
	}

	public void normalizeLTS()
	{
		Iterator<State> iter = states.iterator();
		while (iter.hasNext()) {
			State s = iter.next();
			s.ID = s.ID.replaceAll("@", "");
		}
	}


	public State getStatebyID(String string) throws NoStateExistException {
		// TODO Auto-generated method stub
		Iterator<State> iter = states.iterator();

		while (iter.hasNext()) {
			State s = iter.next();
			if(s.ID!=null)
			{
				if (s.ID.compareTo(string) == 0)
					return s;
			}
			else
				throw new NoStateExistException(s, this);
		}
		return null;
	}

	public State getStatebyID(String string, ArrayList<State> slist) {
		// TODO Auto-generated method stub
		Iterator<State> iter = slist.iterator();

		while (iter.hasNext()) {
			State s = iter.next();
			if (s.ID.compareTo(string) == 0)
				return s;
		}
		return null;
	}

	public LTS copyLTS()
	{
		LTS res = new LTS();

		//		ArrayList<state> sl = new ArrayList<state>();
		//		ArrayList<transition> tl = new ArrayList<transition>();

		Iterator<State> iter = states.iterator();
		while(iter.hasNext())
		{
			State s = iter.next();
			State x = new State(s.ID);
			if(s==s0)
				res.s0 = x;
			else if(s==NA)
				res.NA = x;
			res.states.add(x); 
		}

		Iterator<Transition> itert = trans.iterator();
		while(itert.hasNext())
		{
			Transition t = itert.next();
			Transition t1= new Transition();
			try {
				t1.src = res.getStatebyID(t.src.ID);
				t1.trg = res.getStatebyID(t.trg.ID);
			} catch (NoStateExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			t1.lab = t.lab;	
			t1.owner = t.owner;
			res.trans.add(t1); 
		}
		res.ID = this.ID;
		return res;				
	}

	public LTS product(ArrayList<LTS> LTSs,
			Graph model, boolean cOn,
			ArrayList<State> iStates) {

		ArrayList<State> slist = new ArrayList<State>();
		ArrayList<Transition> trans = new ArrayList<Transition>();
		ArrayList<State>  initStates = new ArrayList<State>();
		ArrayList<State>  removalStates = new ArrayList<State>();

		Stack<State> stack = new Stack<State>();

		// put all possible initial states in stack and repeat the
		// algorithm for them

		for(State s: iStates)
		{
			//			State s0 = new State(this.getInitialStates(LTSs));
			State s0 = new State(s.ID);
			stack.push(s0);
			slist.add(s0);
		}

		ArrayList<String> visitedPair = new ArrayList<String>();

		while (!stack.isEmpty()) {
			State curstate = stack.pop();
			if(!visitedPair.contains(curstate.ID))
			{
				String[] sIDs = curstate.ID.split("@");
				if(cOn)
					out.println("\ncurrent state: " + curstate.ID );

				State[] currs = getStatebyID(LTSs, sIDs);

				ArrayList<Transition> transition = getOutTransof(LTSs,currs);
				if(cOn)
					out.println("Number of out tran: " + Integer.toString(transition.size()));

				Iterator<Transition> iter = transition.iterator();
				//			ArrayList<Transition> visitedTrans = new ArrayList<Transition>();

				while (iter.hasNext()) {

					Transition curtrans = iter.next();
					//			if(!visitedTrans.contains(curtrans))
					//			{
					//				visitedTrans.add(curtrans);

					Event e = curtrans.lab;
					String sid = "";
					if (e.type == ACTIVE.INTERNAL) {
						//Event matched = new Event();
						//		matched = e;
						sid = generateStateID(curtrans,getOwnerID(curtrans.owner, LTSs), null,0,currs);							
						//newtrans = new transition(e, curstate, newstate,"prod");
						if(cOn) 
							out.println(e.print_event() + " from " + curtrans.owner + " is an internal event ");

						//add new state and transition
						slist = this.addNewState( sid, slist, currs, LTSs, curstate, e);
						State newstate = getStatebyID(sid, slist);
						Transition newtrans = new Transition(e, curstate, newstate,"prod");
						trans.add(newtrans);

						if(isInitState(currs,LTSs))
							initStates.add(curstate);
						else
							if(isRemovalState(currs,LTSs))
								removalStates.add(curstate);

						//add the last added state to the stack
						stack.push(slist.get(slist.size()-1));

					} else
						if(e.type == ACTIVE.INPUT)
						{
							//finding a match for synchronization in the other LTSs
							ArrayList<Transition> synchTransList = findMacthedTrans(e, transition, curtrans.owner, currs, LTSs, model); 
							if(cOn && synchTransList.size()==0)	
								out.println("No match for " + e.print_event() + " from " + curtrans.owner);
							Iterator<Transition> iter1 = synchTransList.iterator();
							while(iter1.hasNext())
							{
								Transition synchtrans = iter1.next();
								//matched.type = ACTIVE.INTERNAL;
								sid = generateStateID(curtrans,getOwnerID(curtrans.owner, LTSs), 
										synchtrans, 
										getOwnerID(synchtrans.owner, LTSs), 
										currs);	
								//									visitedTrans.add(synchtrans);

								//add new state and transition
								slist = this.addNewState( sid, slist, currs, LTSs, curstate, synchtrans.lab);
								State newstate = getStatebyID(sid, slist);
								Transition newtrans = new Transition(synchtrans.lab, curstate, newstate,"prod");
								trans.add(newtrans);

								if(isInitState(currs,LTSs))
									initStates.add(curstate);
								else
									if(isRemovalState(currs,LTSs))
										removalStates.add(curstate);

								//add the last added state to the stack
								stack.push(newstate);
								if(cOn)	out.println(e.print_event() + " from " + curtrans.owner + " is an macthed I/O event ");

							}
							//	}
						} // handle one transition
				} // while loop to check all transitions
				visitedPair.add(curstate.ID); // the current states has been handled 
			}// check if it has been visited or not
		}// while loop of stack
		return new LTS("prod", s0, null, slist, initStates, removalStates, trans);
	}

	private ArrayList<State> addNewState(String sid, ArrayList<State> slist, 
			State[] currs, 
			ArrayList<LTS> LTSs, State curstate, 
			Event matched)
			{
		// newtrans = new transition();
		State newstate = new State("");
		State news = getStatebyID(sid, slist);
		if(news!=null)
			newstate = news;
		else
		{
			newstate.ID = sid;
			slist.add(newstate);
		}
		return slist;
			}


	private boolean isRemovalState(State[] currs, ArrayList<LTS> lTSs) {

		boolean res = true;
		for(int idx=0; idx < lTSs.size(); idx++)
			res= res || lTSs.get(idx).removalState.contains(currs[idx]);
		return res;
	}

	private boolean isInitState(State[] currs, ArrayList<LTS> lTSs) {
		boolean res = true;
		for(int idx=0; idx < lTSs.size(); idx++)
			res= res || lTSs.get(idx).initStates.contains(currs[idx]);
		return res;
	}

	public static int getOwnerID(String owner, ArrayList<LTS> lTSs) {
		// TODO Auto-generated method stub
		for(int idx=0; idx< lTSs.size();idx++)
			if(lTSs.get(idx).ID.compareTo(owner)==0)
				return idx; 
		return 0;
	}

	private ArrayList<Transition> findMacthedTrans(Event e, ArrayList<Transition> outTranslist, String owner, State[] currs, ArrayList<LTS> LTSs, Graph model) {

		Event matched = new Event();
		matched.args = e.args;	matched.recID = e.recID;
		matched.name = e.name;
		matched.type = (e.type==ACTIVE.INPUT ? ACTIVE.OUTPUT : ACTIVE.INPUT);

		Transition t = new Transition();
		t.lab = matched;		t.owner = owner;

		ArrayList<Transition> res = new ArrayList<Transition>();
		for(Transition current:outTranslist)
		{
			Event curE = current.lab;
			String curEdgeID = Integer.toString(LTS.getOwnerID(current.owner, LTSs)+1);
			String tEdgeID = Integer.toString(LTS.getOwnerID(owner, LTSs)+1);
			if (!curEdgeID.equals(tEdgeID) &&
					e.args.compareTo(curE.args)==0 && (e.name.compareTo(curE.name)==0)
					&& (e.recID.compareTo(curE.recID)==0) &&
					(e.type==ACTIVE.INPUT? model.findEdge(curEdgeID +"->" + tEdgeID + ":" + current.owner + t.owner)!=null: 
						model.findEdge(tEdgeID +"->" + curEdgeID + ":" + t.owner + current.owner)!=null))
			{
				matched.type = ACTIVE.INTERNAL;
				Transition matchedTrans = new Transition(matched,current.src,current.trg,current.owner);
				res.add(matchedTrans);
			}
		}
		return res;
	}

	private String generateStateID(Transition t1, int t1OwnerID, Transition t2, int t2OwnerID, State[] currs) {
		//if Internal: t1.owner==lts1.ID?t1.trg.ID + "@" + currs2.ID:currs1.ID + "@" + t1.trg.ID;
		String sID = "";
		if(t2==null) //its an internal event or a reconfiguration action
			for(int idx=0; idx<currs.length; idx++)
			{
				if(idx==t1OwnerID)
					sID += t1.trg.ID + "@";
				else
					sID += currs[idx].ID + "@";
			}
		else
			// its a synchronized transition
			for(int idx=0; idx<currs.length; idx++)
			{
				if(idx==t1OwnerID)
					sID += t1.trg.ID + "@";
				else
					if(idx==t2OwnerID)
						sID += t2.trg.ID + "@";
					else
						sID += currs[idx].ID + "@";
			}				
		return sID.substring(0,sID.length()-1);
	}

	private ArrayList<Transition> getOutTransof(ArrayList<LTS> LTSs,
			State[] currs) {
		// TODO Auto-generated method stub

		ArrayList<Transition> res =  new ArrayList<Transition>();
		for(int i=0; i< LTSs.size();i++)
		{
			LTS lts = LTSs.get(i);
			ArrayList<Transition> outTrans = lts.getOutTransof(currs[i]);
			res.addAll(outTrans);
			/*			Iterator<Transition> iter = outTrans.iterator();
			while (iter.hasNext()) {
				Transition t = iter.next();
				res.add(t);*/
		}
		return res;
	}

	private State[] getStatebyID(ArrayList<LTS> LTSs, String[] sIDs) {

		State[] res = new State [LTSs.size()];

		for(int i=0; i< LTSs.size();i++)
		{
			LTS lts = LTSs.get(i);
			try {
				res[i] = lts.getStatebyID(sIDs[i]);
			} catch (NoStateExistException e) {
				// TODO Auto-generated catch block
				out.println( "The state with ID " + sIDs[i] + " does not exist in LTS " + e.lts.ID );
				e.printStackTrace();
			}
		}
		return res;
	}

	static String getInitialStates(ArrayList<LTS> LTSs) {
		// TODO Auto-generated method stub
		String sID = "";
		for(int idx=0; idx<LTSs.size(); idx++)
			sID +=  LTSs.get(idx).s0.ID + "@" ;
		return sID.substring(0,sID.length()-1);
	}

	public LTS buildInactiveLTS() {
		// TODO Auto-generated method stub
		LTS res = new LTS();

		State src = new State(NA.ID);
		res.NA = src;
		res.s0 = src;
		res.states.add(src);

		Iterator<State> itert = initStates.iterator();
		while(itert.hasNext())
		{
			State s = itert.next();
			res.initStates.add(new State(s.ID));
		}

		itert = removalState.iterator();
		while(itert.hasNext())
		{
			State s = itert.next();
			res.removalState.add(new State(s.ID));
		}

		res.ID = this.ID;
		return res;					
	}

	/*	public LTS buildInactiveLTS() {
		// TODO Auto-generated method stub
		LTS res = new LTS();

		ArrayList<transition> NATrans = getOutTransof(NA);

		state src = new state(NA.ID);
		res.NA = src;
		res.s0 = src;
		res.states.add(src);

		Iterator<transition> itert = NATrans.iterator();
		while(itert.hasNext())
		{
			transition t = itert.next();
			state trg;
			try {
				trg = res.getStatebyID(t.trg.ID);
				if(trg==null)
				{
					trg = new state(t.trg.ID);
					res.states.add(trg);
					transition t1= new transition(t.lab, src, trg, t.owner);
					res.trans.add(t1); 
				}
			} catch (NoStateExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		

		}
		res.ID = this.ID;
		return res;					
	}
	 */

	// inits: the states from which we can start computation when a new component is added
	// removs: the states from which we can remove a component
	public void addInitRemovalStates(String inits, String removs) throws NoStateExistException {

		String[] str = inits.split("@");
		for(int idx=0;idx <str.length; idx++)
			this.initStates.add(getStatebyID(str[idx]));

		str = removs.split("@");
		for(int idx=0;idx <str.length; idx++)
			this.removalState.add(getStatebyID(str[idx]));
	}

	public static LTS parseLTS(String filename, String ltsName, String s0, String NA) throws IOException, NoStateExistException
	{
		LTS lts = new LTS();
		lts.ID = ltsName;
		try {
			BufferedReader br = new BufferedReader(new FileReader("/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/inputs/" + filename + ".txt"));
			//			BufferedReader br = new BufferedReader(new FileReader("/home/narges/workspace/CS/src/pack/"+filename+".txt"));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			String everything = sb.toString().trim().replaceAll(" ", "");

			// remove "digraph {"
			everything = everything.substring(8,everything.length()-1);

			//split to the set of lines
			String[] substr = everything.split(";");

			for(int i=0; i<substr.length; i++)
			{
				String edge = substr[i];
				String[] str = edge.split("->");

				//adding the source node to the list, if it is not in state set
				State srcstate = lts.getStatebyID(str[0]);
				if(srcstate==null)
				{
					srcstate = new State(str[0]);
					lts.states.add(srcstate);
					if(str[0].compareTo(s0)==0)
						lts.s0 = srcstate;
					if(str[0].compareTo(NA)==0)
						lts.NA = srcstate;
				}
				String[] str1 = str[1].split("label=");

				//adding the target node to the list, if it is not in state set
				String trg = str1[0].substring(0, str1[0].length()-1);
				State trgstate = lts.getStatebyID(trg); 
				if(trgstate==null)
				{
					trgstate = new State(trg);
					lts.states.add(trgstate);
					if(trg.compareTo(s0)==0)
						lts.s0 = trgstate;
					if(trg.compareTo(NA)==0)
						lts.NA = trgstate;
				}

				String lab = str1[1].substring(1,str1[1].length()-2);
				str = lab.split("\\.");
				String recId = str[0];

				str1 = str[1].split("\\(");
				String msgName = str1[0];

				char type = str1[1].charAt(str1[1].length()-1);
				String arg = str1[1].substring(0, type!='?' && type!='!'? str1[1].length()-1:str1[1].length()-2);
				ACTIVE type1 = (type=='?'?ACTIVE.INPUT:(type=='!'?ACTIVE.OUTPUT:ACTIVE.INTERNAL));
				Event e0 = new Event(type1, recId,msgName, arg);

				Transition t1 = new Transition(e0,srcstate,trgstate,ltsName);
				lts.trans.add(t1);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lts; 
	}

	public static State getInitialStates(ArrayList<LTS> LTSs, Graph g) {

		String ID = "";
		for(int idx=0; idx<LTSs.size(); idx++)
		{
			LTS lts = LTSs.get(idx);
			int ltsID = LTS.getOwnerID(lts.ID, LTSs);

			if(g.findNode(ltsID+1)==null)
				ID += lts.NA.ID +"@";
			else
				ID += lts.s0.ID +"@";
		}
		return new State(ID.substring(0, ID.length()-1));
	}

}
