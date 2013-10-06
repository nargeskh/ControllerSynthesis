package graphLTS;

import static java.lang.System.out;
import graphTransformation.Graph;
import graphTransformation.GraphTransform;
import graphTransformation.Match;
import graphTransformation.Morphism;
import graphTransformation.Rule;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//import org.jgrapht.DirectedGraph;





































import basics.ACTIVE;

public class gLTS {

	public gLTS(String iD, gState inits, HashMap<String,gState> states, HashMap<String,gState> fstates, HashMap<String,gTransition> trans) {
		super();
		this.states = states;
		this.trans = trans;
		this.ID = iD;
		this.s0 = inits;
		this.finals = fstates;
	}

	public gLTS() {
	}

	public HashMap<String,gState> states = new HashMap<String, gState>();
	public HashMap<String,gState> finals = new HashMap<String,gState>();
	public HashMap<String,gTransition> trans = new HashMap<String,gTransition>();
	public gState s0 = new gState();

	public String ID;

	/*	public HashMap<String,gState> getStates() {
		return states;
	}

	public void setStates(HashMap<String,gState> states) {
		this.states = states;
	}

	public HashMap<String, gTransition> getTrans() {
		return trans;
	}

	public void setTrans(HashMap<String, gTransition> trans) {
		this.trans = trans;
	}

	public boolean hasTrans(Transition t)
	{
		return trans.containsValue(t);
	}

	public boolean hasState(State s)
	{
		return states.containsValue(s);
	}
	 */

	// given an initial structure "model" and the behavior of components, 
	// create a graph transition system modeling the system behavior

	private LTS createLTS(Graph model,
			ArrayList<LTS> LTSs, boolean commOn, 
			HashMap<String,gState> initgState
			)
	{
		ArrayList<LTS> original = new ArrayList<LTS>();
		for(int idx=0; idx<LTSs.size(); idx++)
		{
			LTS lts = LTSs.get(idx);

			// if the model does not contain lts, we replace it with an empty LTS 
			int ltsID = LTS.getOwnerID(lts.ID, LTSs);
			if(model.findNode(ltsID+1)==null)
			{
				LTS tmp = lts.buildInactiveLTS();
				original.add(idx, tmp);
				out.println( lts.ID + " is inactive");
			}
			else
				original.add(idx, lts.copyLTS());
		}
		LTS lts = new LTS();

		ArrayList<State> initStates = new ArrayList<State>();

		for(String sid: initgState.keySet())
			initStates.add(initgState.get(sid).s);

		lts = lts.product(original, model, commOn, initStates);
		return lts;
	}

	public gState getGStatebyID(HashMap<String,gState> states2, String string) {
		return states2.get(string);
	}

	private gLTS creategLTS(Graph model,
			ArrayList<LTS> LTSs, String id, 
			HashMap<String,gState> initState, boolean comOn
			)
	{
		LTS lts = createLTS(model,LTSs,comOn,initState);

		gLTS gts = new gLTS();
		gts.ID = id;

		Iterator<State> iter = lts.states.iterator();
		while(iter.hasNext())
		{
			State s = iter.next();
			State x = new State(s.ID + id);
			gState gs = new gState(x,model);
			if(s==lts.s0)
				gts.s0 = gs;
			gts.states.put(gs.s.ID , gs); 
		}

		Iterator<Transition> itert = lts.trans.iterator();
		while(itert.hasNext())
		{
			Transition t = itert.next();
			gTransition gt= new gTransition(); 
			gt.src = getGStatebyID(gts.states, t.src.ID + id);
			gt.trg = getGStatebyID(gts.states, t.trg.ID + id);
			gt.lab = t.lab;			
			// MODIFIED:			gts.trans.put(gt.src.s.ID +gt.trg.s.ID , gt); 
			gts.trans.put(gt.getLabel() , gt); 
		}		

		//	out.println("\nThe  number of states of new created gLTS " + gts.ID + " is " + Integer.toString(gts.states.size()));
		return gts;
	}

	public gLTS applyReconfiguration(ArrayList<LTS> AllComponentsLTSs,gLTS plan, boolean comOn)
	{
		//		Map<String, gLTS> res = new HashMap();
		//	gLTS res = new gLTS();
		Map <String, gLTS> gLTSMap = new HashMap<String, gLTS>();

		gLTS gltsvar = new gLTS();
		int counter = 0;


		HashMap<String,gState> initList = new HashMap<String,gState>();
		// set the initial state of the plant
		State is0 = LTS.getInitialStates(AllComponentsLTSs, plan.s0.g);
		gState initgs = new gState(is0, plan.s0.g);
		initList.put(initgs.s.ID,initgs);

		boolean isFinalPlanState = plan.getOutTransof(plan.s0).size()==0;
		gltsvar = gltsvar.creategLTS(plan.s0.g, AllComponentsLTSs, plan.s0.s.ID + "@0", initList,comOn);
		initgs = gltsvar.getStatebyID(is0.ID + plan.s0.s.ID + "@0");

		String newPlanStateID = plan.s0.s.ID + "@" + Integer.toString(counter++);
		gLTSMap.put(newPlanStateID, gltsvar);


		Stack<String> stack = new Stack<String>();
		stack.push(newPlanStateID);

		while(!stack.isEmpty())
		{

			String top = stack.pop();
			gLTS srcgLTS = gLTSMap.get(top);

			out.println("\nCurrent reconfiguration plan state: " + top + 
					"\nNo States/Transitions in " + top + ": " + 
					Integer.toString(srcgLTS.states.size()) + "/" 
					+ Integer.toString(srcgLTS.trans.size()) );//+ srcgLTS.draw());

			gState planCurs = plan.getStatebyID(top.split("@")[0]);

			Iterator<gTransition> iter = plan.getOutTransof(planCurs).iterator();
			while (iter.hasNext()) {
				gTransition plantrans = iter.next();
				out.println("Current Target: " + plantrans.trg.s.ID);

				//update the structure
				Graph g = planCurs.g;//, oldg = planCurs.g;
				g = updateStructure(g,plantrans.lab.name, plantrans.lab.args, AllComponentsLTSs); 
				plantrans.trg.g = g;

				// push the cuurent plan state in stack
				newPlanStateID = plantrans.trg.s.ID + "@" + Integer.toString(counter++);
				stack.push(newPlanStateID);
				isFinalPlanState = plan.getOutTransof(plantrans.trg).size()==0;

				initList = srcgLTS.getInitStatesforReconfig(plantrans, AllComponentsLTSs, planCurs.s.ID);
				gLTS trggLTS = creategLTS(g, AllComponentsLTSs, newPlanStateID , initList, comOn);
				/*out.println("\nNumber of states/transitions: " + Integer.toString(trggLTS.states.size()) + "/" +
								Integer.toString(trggLTS.trans.size()));
				 */
				gLTSMap.put(newPlanStateID, trggLTS);
				HashMap<String,gState> trgFinalStates = new HashMap<String, gState>();

				if(plantrans.lab.name.equals("add"))
					trgFinalStates = srcgLTS.addComponent(plantrans, AllComponentsLTSs,trggLTS,isFinalPlanState);
				else
					if(plantrans.lab.name.equals("del"))
						trgFinalStates = srcgLTS.delComponent(plantrans, AllComponentsLTSs, trggLTS);
					else
						trgFinalStates = srcgLTS.conn_disconn_component(plantrans,trggLTS,
								top, newPlanStateID);
				if(isFinalPlanState)
					trggLTS.finals = trgFinalStates;
			} // check for all transitions
			//	out.println("The state " + planCurs.s.ID + " is added to the visited list!" );
		}//stack while
		out.println("\nNumber of gLTSs " + Integer.toString(gLTSMap.size()));
		return unionOfGLTs(initgs, gLTSMap);
	}

	private HashMap<String,gState> getInitStatesforReconfig(gTransition plantrans, ArrayList<LTS> LTSs, String postfix) {

		int ltsID = LTS.getOwnerID(plantrans.lab.args, LTSs);
		HashMap<String,gState> res = new HashMap<String,gState>();

		for(String ID : states.keySet())
		{
			String[] sid = ID.split("@");
			// replace the states of deleting comp with its NA
			if(plantrans.lab.name.equals("del"))
				sid[ltsID] = LTSs.get(ltsID).NA.ID;
			else
				if(plantrans.lab.name.equals("add"))
					sid[ltsID] = LTSs.get(ltsID).initStates.get(0).ID;

			// add the new state to the list
			String id = "";
			for(int i=0; i< LTSs.size(); i++)
				id += sid[i] + "@";

			State s = new State(id.substring(0,id.length()-1).replace(postfix, ""));
			res.put(s.ID, new gState(s, states.get(ID).g));
		}
		return res;
	}

	private gLTS unionOfGLTs(gState inits, Map<String, gLTS> gLTSMap) {

		gLTS glts = new gLTS();
		glts.s0 = inits;

		Collection<gLTS> gLTSlist=	gLTSMap.values();
		for(gLTS cur:gLTSlist)
		{
			glts.states.putAll(cur.states);
			glts.trans.putAll(cur.trans);
			glts.finals.putAll(cur.finals);
		}
		out.println("Total No. of States: " + Integer.toString(glts.states.size()));
		out.println("Total No. of Transitions: " + Integer.toString(glts.trans.size()) + "\n");		
		return glts;
	}

	private Graph updateStructure(
			Graph graph, String name, String args, ArrayList<LTS> lTSs) {

		String L = graph.toString();
		String R = "";
		String I = "";

		if(name.equals("add"))
		{
			String ltsID = Integer.toString(LTS.getOwnerID(args, lTSs)+1);
			I = graph.generateIMapping();
			R = ltsID + ":" + args + ";" + L;			
		}
		else
			if(name.equals("del"))
			{
				int ltsID = LTS.getOwnerID(args, lTSs) + 1;
				I = graph.graphIMappingDelNode(ltsID);
				R = L.replace(ltsID + ":" + args + ";", "");
				R = R.replaceAll("\\d+->"+ ltsID + ":\\w+;", "");
				R = R.replaceAll(ltsID + "->\\d+"+ ":\\w+;", "");
			}		
			else
				if(name.equals("conn"))
				{
					String[] c = args.trim().split(",");

					String srcID = Integer.toString(LTS.getOwnerID(c[0], lTSs)+1);
					String trgID = Integer.toString(LTS.getOwnerID(c[1], lTSs)+1);
					I = graph.generateIMapping();
					R = L + ";" + srcID + "->" + trgID + ":" + c[0] + c[1] ;			
				}
				else
					if(name.equals("dis"))
					{
						String[] c = args.trim().split(",");

						String srcID = Integer.toString(LTS.getOwnerID(c[0], lTSs)+1);
						String trgID = Integer.toString(LTS.getOwnerID(c[1], lTSs)+1);
						String edge = srcID + "->" + trgID + ":" + c[0] + c[1];
						I = graph.generateIMapping().replace(edge + "<->" + edge, "");
						R = L.replace(edge,"");			
						R = R.replace(";;",";");		
					}
					else
					{
						out.println("The action " + name + args + " is not supported");
						return graph;
					}
		GraphTransform xform = new GraphTransform();

		Rule rule = new Rule("rule" + name + args, L, R, I);
		Match match = new Match(rule.left, graph);
		List<Morphism> matches = match.findMatch();
		graph = xform.apply(graph, rule, matches.get(0));
		//	out.println("The updated graph is:\n" + graph.toDot());
		return graph;
	}

	private HashMap<String, gState> conn_disconn_component(gTransition plantrans, gLTS trggLTS, 
			String oldPanStateID, String newPlanStateId) {

		HashMap<String, gState> connectingStates = new HashMap<String, gState>();

		Iterator<gState> itera = states.values().iterator();
		while (itera.hasNext()) {
			gState s = itera.next();
			//set the target of connecting transition t to the one with the same ID but from the new model  
			String newtrgID = 
					s.s.ID.replaceFirst(oldPanStateID, newPlanStateId);

			gState newtrg = trggLTS.getStatebyID(newtrgID);
			if (newtrg != null)
			{
				gTransition newTrans = new gTransition(plantrans.lab, s, newtrg);
				// MODIFIEd: trans.put( s.s.ID+ newtrg.s.ID , new gTransition(plantrans.lab, s, newtrg));
				trans.put( newTrans.getLabel() , newTrans );
				connectingStates.put(newtrgID, newtrg); 
			}
			else
				out.println( newtrgID + " does not exist!!!" );							
		}
		return connectingStates;

	}

	public ArrayList<gTransition> getOutTransof(gState s) {
		ArrayList<gTransition> res = new ArrayList<gTransition>();

		Iterator<gTransition> itera = trans.values().iterator();
		while (itera.hasNext()) {
			gTransition t1 = itera.next();
			if(t1.src.equals(s))
				res.add(t1);
		}
		return res;	
	}


	private HashMap<String,gState> addComponent( 
			gTransition plantrans, 
			ArrayList<LTS> LTSs, 
			gLTS trggLTS, boolean isFinalPlanState)
			{

		// find all NA states of the old configuration and connect them to the potInitStates in the new configuration
		int ltsID = LTS.getOwnerID(plantrans.lab.args, LTSs);
		HashMap<String,gState> NAStates = this.getInactiveStates(ltsID, plantrans.src.s.ID,LTSs);
		HashMap<String,gState> trgStates = trggLTS.getAddingStates(ltsID, plantrans.trg.s.ID,LTSs);

		trans.putAll(trggLTS.findConnectingNodes(NAStates, trgStates, LTSs.size(),ltsID,plantrans));

		return trgStates;
			}

	private HashMap<String, gState> delComponent( 
			gTransition plantrans, 
			ArrayList<LTS> LTSs, gLTS trggLTS)
			{

		// find all NA states of the old configuration and connect them to the potInitStates in the new configuration
		int ltsID = LTS.getOwnerID(plantrans.lab.args, LTSs);
		HashMap<String,gState> srcStates = this.getRemovingStates(ltsID, plantrans.src.s.ID,LTSs);
		HashMap<String,gState> trgStates = trggLTS.getInactiveStates(ltsID, plantrans.trg.s.ID,LTSs);

		trans.putAll(trggLTS.findConnectingNodes(srcStates, trgStates, LTSs.size(),ltsID,plantrans));
		return trgStates;

			}


	private Map<String, gTransition> findConnectingNodes(HashMap<String,gState> srcStates, HashMap<String,gState> trgStates, 
			int ltssize, int ltsID, gTransition plantrans)
			{
		HashMap<String,gTransition> res = new HashMap<String,gTransition>();

		Iterator<gState> iter = srcStates.values().iterator();
		while(iter.hasNext())
		{
			gState src = iter.next();
			Iterator<gState> iterb = trgStates.values().iterator();
			while(iterb.hasNext())
			{
				gState gs = iterb.next();
				//set the target of connecting transition t to the one with the same ID but from the new model

				//check if the current two states only differs in the states of ltsID

				String[] str1 = src.s.ID.replace(plantrans.src.s.ID, "").split("@");
				String[] str2 = gs.s.ID.replace(plantrans.trg.s.ID, "").split("@");
				boolean areconnected = true;
				for(int idx=0; idx < ltssize; idx++)
					areconnected = areconnected && (idx!=ltsID?str1[idx].compareTo(str2[idx])==0:true);

				if(areconnected)
					res.put(src.s.ID + gs.s.ID , new gTransition(plantrans.lab, src, gs));
			}
		}
		return res;

			}

	private HashMap<String,gState> getAddingStates(int ltsIndex,  
			String reconfStateId, ArrayList<LTS> LTSs) {
		//get the initstates of the LTS of lab's owner
		ArrayList<State> sts = LTSs.get(ltsIndex).initStates;

		return	getMappingStates(sts,LTSs.size(),ltsIndex,reconfStateId);
	}

	private HashMap<String,gState> getRemovingStates(int ltsIndex, 
			String reconfStateId,
			ArrayList<LTS> LTSs) {
		ArrayList<State> sts = LTSs.get(ltsIndex).removalState;
		return	getMappingStates(sts,LTSs.size(),ltsIndex,reconfStateId);
	}

	private HashMap<String,gState> getMappingStates(ArrayList<State> sts, int LTSsize, int ltsIndex, String reconfStateId)
	{
		//str is as string which shows the IDs of initstates/removalStates
		String sStr = "";
		Iterator<State> iter = sts.iterator();
		while(iter.hasNext())
		{
			State s = iter.next();
			sStr += s.ID + "@";
		}

		// search among the states of gLTS the states and 
		// find those in which ltsIndex is in an initstate/removalstates
		Iterator<gState> giter = states.values().iterator();
		HashMap<String,gState> res = new HashMap<String,gState>();

		while(giter.hasNext())
		{
			gState s = giter.next();
			String[] str = s.s.ID.split("@");
			if(ltsIndex == LTSsize-1)
				str[ltsIndex]=str[ltsIndex].replaceAll(reconfStateId,"");

			//if the substate of LTS_ltsindex is an adding states, then add this states as the adding state
			if(sStr.contains(str[ltsIndex]))
				res.put(s.s.ID , s);
		}
		return res;
	}

	//get states in which ltsIndex is in inactive a state
	private HashMap<String,gState> getInactiveStates(int ltsIndex,  
			String reconfStateId, ArrayList<LTS> LTSs) {
		HashMap<String,gState> res = new HashMap<String,gState>();
		//find the NA states of the LTS of lab's owner
		Iterator<gState> giter = states.values().iterator();
		while(giter.hasNext())
		{
			gState s = giter.next();
			String[] str = s.s.ID.split("@");
			if(ltsIndex == LTSs.size()-1)
				str[ltsIndex]=str[ltsIndex].replaceAll(reconfStateId,"");

			//if the substate of LTS_ltsindex is an adding states, then add this states as the adding state
			if(str[ltsIndex].compareTo(LTSs.get(ltsIndex).NA.ID)==0)
				res.put(s.s.ID , s);
		}

		return res;
	}

	public gState getStatebyID(String sID) {
		return states.get(sID);
	}

	public String draw()
	{
		String g = 
				"digraph { \n";
		if(states.size()<=1)
			g += s0.s.ID + "[fillcolor = red]";
		else
		{	
			Iterator<gTransition>	iter = trans.values().iterator();
			while(iter.hasNext())
			{
				gTransition t = iter.next();
				g += (t.src.s.ID) + "->";
				g += (t.trg.s.ID);
				g += "[label= \"" + t.lab.print_event() + "\"];\n";
				//a -> b[label="0.2",weight="0.2"];
			}

		}

		out.println( "\n\nNumber of transitions: " + Integer.toString(trans.size()) +
				"\nNumber of states: " + Integer.toString(states.size()));
		return g.replace("@", "") + "\n}";	

	}

	public gLTS removeIOtrans() {
		ArrayList<gTransition> tlist = new ArrayList<gTransition>();
		Iterator<gTransition>	iter = trans.values().iterator();
		while(iter.hasNext())
		{
			gTransition t = iter.next();
			if(t.lab.type!=ACTIVE.INTERNAL)
			{
				tlist.add(t);
				t.lab.recID = "A";
				t.lab.name = "IO";
				t.lab.args = "";
			}
		}

		iter = tlist.iterator();
		while(iter.hasNext())
		{
			gTransition t = iter.next();
			//	if(tlist.contains(t) && t.lab.type!=Active.INTERNAL)
			trans.remove(t);
		}
		return this;
	}

	public gState getGStatebyID(String iD2, Graph model) {

		Iterator<gState> iter = states.values().iterator();

		while (iter.hasNext()) {
			gState s = iter.next();
			if (s.s.ID.compareTo(iD2) == 0 &&
					s.g.iso(model))
				return s;
		}
		return null;
	}

	public void transToIntStates() {

		HashMap<String, String> mapping = new HashMap<String, String>();
		HashMap<String, gState> newStates = new HashMap<String, gState>();
		HashMap<String, gTransition> newTrans = new HashMap<String, gTransition>();

		Iterator<String> iter = states.keySet().iterator();
		int index = 0;
		while(iter.hasNext())
		{
			String oldid = states.get(iter.next()).s.ID;
			String newid = "a"+Integer.toString(index++);

			mapping.put(oldid, newid);

			gState newState = states.get(oldid).copy();
			newState.s.ID = newid;
			newStates.put(newid , newState);
		}		

		iter = trans.keySet().iterator();
		while(iter.hasNext())
		{
			String oldid = iter.next();
			gTransition oldt = trans.get(oldid);

			gState src = newStates.get(mapping.get(oldt.src.s.ID));
			gState trg = newStates.get(mapping.get(oldt.trg.s.ID));

			gTransition newt = new gTransition(oldt.lab, src, trg);
			// MODIFIED newTrans.put(src.s.ID + trg.s.ID + oldt.lab.recID  + oldt.lab.name , newt);
			newTrans.put(newt.getLabel() , newt);
		}	

		s0 = newStates.get(mapping.get(s0.s.ID));

		states = newStates;
		trans = newTrans;
	}

	public void removeStateSplitter() {

		HashMap<String, gState> newStates = new HashMap<String, gState>();
		HashMap<String, gState> newFinalStates = new HashMap<String, gState>();
		HashMap<String, gTransition> newTrans = new HashMap<String, gTransition>();

		Iterator<String> iter = states.keySet().iterator();
		while(iter.hasNext())
		{
			String oldid = iter.next();
			String newid = oldid.replace("@", "");

			gState newState = states.get(oldid).copy();
			newState.s.ID = newid;
			newStates.put(newid , newState);
		}		

		iter = trans.keySet().iterator();
		while(iter.hasNext())
		{
			String oldid = iter.next();
			gTransition oldt = trans.get(oldid);

			gState src = newStates.get(oldt.src.s.ID.replace("@", ""));
			gState trg = newStates.get(oldt.trg.s.ID.replace("@", ""));

			gTransition newt = new gTransition(oldt.lab, src, trg);
			//MODIFIED: newTrans.put(src.s.ID + trg.s.ID + oldt.lab.recID  + oldt.lab.name , newt);
			newTrans.put(newt.getLabel() , newt);
		}	

		iter = finals.keySet().iterator();
		while(iter.hasNext())
		{
			String oldid = iter.next();
			String newid = oldid.replace("@", "");

			gState newState = newStates.get(newid);
			newFinalStates.put(newid , newState);
		}		

		s0 = newStates.get(s0.s.ID.replace("@", ""));

		states = newStates;
		trans = newTrans;
		finals = newFinalStates;
	}


	public ArrayList<gTransition> getInTransof(gState s) {
		ArrayList<gTransition> res = new ArrayList<gTransition>();

		Iterator<String> itera = trans.keySet().iterator();
		while (itera.hasNext()) {
			gTransition t1 = trans.get(itera.next());
			if(t1.trg.s.ID.equals(s.s.ID))
				res.add(t1);
		}
		return res;	
	}


	public static gLTS parsegLTS(String filename, String ltsName, String s0, Graph model) throws IOException
	{
		gLTS glts = new gLTS();
		glts.ID = ltsName;
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
				gState srcstate = glts.getStatebyID(str[0]);
				if(srcstate==null)
				{
					srcstate = new gState(new State(str[0]), model);
					glts.states.put(srcstate.s.ID,srcstate);
					if(str[0].compareTo(s0)==0)
						glts.s0 = srcstate;
				}
				String[] str1 = str[1].split("label=");

				//adding the target node to the list, if it is not in state set
				String trg = str1[0].substring(0, str1[0].length()-1);
				gState trgstate = glts.getStatebyID(trg); 
				if(trgstate==null)
				{
					trgstate = new gState(new State(trg), model);
					glts.states.put(trgstate.s.ID,trgstate);
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

				gTransition t1 = new gTransition(e0,srcstate,trgstate);
				// MODIFIED: glts.trans.put(srcstate.s.ID + trgstate.s.ID, t1);
				glts.trans.put(t1.getLabel(), t1);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return glts; 
	}

	/*public String getInTransof(String iD2) {
		String str = "";

		Iterator<gTransition> iter = trans.values().iterator();
		while(iter.hasNext())
		{
			gTransition t = iter.next();
			if(t.trg.s.ID.equals(iD2))
				str += t.src.s.ID + "      ";

		}
		return str;
	}*/

	public gLTS copy() {

		gLTS res = new gLTS();

		Iterator<gState> iter = states.values().iterator();
		while(iter.hasNext())
		{
			gState gs = iter.next(), gscopy = gs.copy();
			res.states.put(gscopy.s.ID, gscopy);
			if(gs == s0)
				res.s0 = gs;
		}
		/*
		for(gTransition t : trans)
		{
			gState src = res.getStatebyID(t.src.s.ID);
			gState trg = res.getStatebyID(t.trg.s.ID);
			res.trans.add(new gTransition(t.lab, src, trg));
		}
		 */
		return res;

	}

	public gLTS getGLTSofConfig(String configID) {

		gLTS res = new gLTS();
		Iterator<String> iter = states.keySet().iterator();
		while(iter.hasNext())
		{
			String sid = iter.next();
			gState gs = states.get(sid);
			if(sid.split("@")[0].contains(configID))
			{	 
				gState	gscopy = gs.copy();
				res.states.put(gscopy.s.ID, gscopy);
				if(gs == s0)
					res.s0 = gs;
			}
		}

		for(String tid : trans.keySet())
		{
			gTransition t = trans.get(tid);

			gState src = res.states.get(t.src.s.ID);
			gState trg = res.states.get(t.trg.s.ID);

			if(src!=null && trg!=null)
				res.trans.put(tid , new gTransition(t.lab, src, trg));
		}
		return res;
	}

	public gLTS findConnectedStatesofTwoConfigs(String configID1, String configID2) {

		gLTS res = new gLTS();
		for(String tid : trans.keySet())
		{
			gTransition t = trans.get(tid);

			gState src = states.get(t.src.s.ID);
			gState trg = states.get(t.trg.s.ID);

			if(src.s.ID.contains(configID1) && trg.s.ID.contains(configID2))
			{
				res.states.put(src.s.ID , src);
				res.states.put(trg.s.ID , trg);
				res.trans.put(tid , new gTransition(t.lab, src, trg));
			}
		}
		return res;

	}

	public gState getStateBeginWithID(String iD2) {
		Iterator<gState> iter = states.values().iterator();

		while (iter.hasNext()) {
			gState s = iter.next();
			if (s.s.ID.startsWith(iD2))
				return s;
		}
		return null;
	}

	public ArrayList<String> getOutEventsof(gState state) {
		ArrayList<String> res = new ArrayList<String>();

		Iterator<gTransition> itera = trans.values().iterator();
		while (itera.hasNext()) {
			gTransition t1 = itera.next();
			if(t1.src.equals(state))
				res.add(t1.lab.print_event());
		}
		return res;	
	}

	public ArrayList<String> getUncontrollableEventsof(gState state,
			List<String> uncontrollableEventlist) {
		ArrayList<String> res = new ArrayList<String>();

		Iterator<gTransition> itera = trans.values().iterator();
		while (itera.hasNext()) {
			gTransition t1 = itera.next();
			if(t1.src.equals(state) &&
					uncontrollableEventlist.contains(t1.lab.print_event()))
				res.add(t1.lab.print_event());
		}
		return res;	
	}

	public gLTS removeState(ArrayList<String> badStates) {

		if(badStates.isEmpty())
			return this;
		HashMap<String, gTransition> resTrans = new HashMap<String, gTransition>();

		for(gTransition t: this.trans.values())
			if(!badStates.contains(t.src.s.ID) &&
					!badStates.contains(t.trg.s.ID))
				//				String t_label = t.src.s.ID + t.trg.s.ID;
				//				resTrans.remove(t_label);
				resTrans.put(t.getLabel(), t);
			else
				out.println("The transition from " + t.src.s.ID +  " to " + t.trg.s.ID + " with label " + t.lab.print_event() + " was removed" );

		for(String s: badStates)
		{
			this.states.remove(s);

			if(this.finals.containsKey(s))
				finals.remove(s);

			if(s0.equals(s))
				s0 = null;

			out.println("The state  " + s  + " was removed" );
		}

		return new gLTS(ID, s0, states, finals, resTrans);
	}
}



