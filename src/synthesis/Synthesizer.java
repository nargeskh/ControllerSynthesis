package synthesis;

import static java.lang.System.out;
import graphAutomata.GraphAutomata;
import graphAutomata.gExptransition;
import graphLTS.NoStateExistException;
import graphLTS.State;
import graphLTS.gLTS;
import graphLTS.gState;
import graphLTS.gTransition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class Synthesizer {

	public gLTS synthesize(gLTS plant, 
			GraphAutomata spec,
			gLTS plan) 
	{
		gLTS controller = new gLTS();
		try {
			controller = product(plant , spec);
			out.println("\nThe Product of SPEC and PLANT is :");// + controller.draw());
			out.println("No. of States/Transitions of Product:" + Integer.toString(controller.states.size()) + "/" 
					+Integer.toString(controller.trans.size()) );// + controller.draw());

			controller = removeBadStates(plant, controller);
			out.println("\nController after removing bad states:");// + controller.draw());

			controller  = this.removeCounreachableStates(controller);
			out.println("\nController after removing un-coaccesible states:");// + controller.draw());
			out.println("No States/Transitions of final adaptor:" + Integer.toString(controller.states.size()) + "/" 
					+Integer.toString(controller.trans.size()) );// + controller.draw());

		} 
		catch (NoStateExistException e) {
			e.printStackTrace();
		}
		return controller;

	}

	private gLTS product(gLTS plant, 
			GraphAutomata spec) throws NoStateExistException
			{
		gState s0 = new gState(new State(plant.s0.s.ID + "@" + spec.s0.ID), 
				plant.s0.g);
		gLTS adaptor = new gLTS("adaptor",s0,new HashMap<String,gState>(),
				new HashMap<String,gState>(),new HashMap<String,gTransition>());

		adaptor.states.put(s0.s.ID,s0);
		//		slist.add(s0);

		// put all destination of current state in s1 in stack and repeat the
		// algorithm for them
		Stack<gState> stack = new Stack<gState>();
		stack.push(s0);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			if(!visited.contains(curstate.s.ID))
			{
				String[] sIDs = curstate.s.ID.split("@");

				// get the current state of plant and the spec
				gState pstate = plant.getStatebyID(sIDs[0]);
				State  sstate  = spec.getStatebyID(sIDs[1]);

				// get the outgoing transition of current state of plant
				ArrayList<gTransition> pouttrans = plant.getOutTransof(pstate);

				Iterator<gTransition> iter = pouttrans.iterator();
				while (iter.hasNext()) {
					gTransition ptrans = iter.next();

					// find all the satisfying transitions from spec
					ArrayList<gExptransition> satTransList = spec.findSatisfiableTrans(ptrans , sstate);
					//			out.println("B" + Integer.toString((counter++)));

					Iterator<gExptransition> itera = satTransList.iterator();
					while (itera.hasNext()) {
						//	out.println("C" + Integer.toString((counter++)));
						gExptransition strans = itera.next();
						String sid = ptrans.trg.s.ID + "@" + strans.trg.ID;
						gState trgstate = new gState(new State(sid), ptrans.trg.g);

						// check if the new state has already added to the model or not
						gState ngs = adaptor.getGStatebyID(sid , trgstate.g);
						if(ngs!=null)
							trgstate = ngs;
						else
							adaptor.states.put(trgstate.s.ID, trgstate);

						if(!visited.contains(trgstate))
							stack.push(trgstate);
						gTransition gt = new gTransition(ptrans.lab, curstate, trgstate);
						adaptor.trans.put(gt.src.s.ID + gt.trg.s.ID , gt);

						//if both plant and system model are in final states, then this state is final
						if(spec.isfinalState(strans.trg.ID) &&
								plant.finals.get(ptrans.trg.s.ID)!=null)
							adaptor.finals.put(trgstate.s.ID,trgstate);
					}
				}
				visited.add(curstate.s.ID);
			}
		}
		return adaptor;
			}

	private gLTS removeCounreachableStates(gLTS controller) {

		// find the reachable states from the initial state
		gLTS initreachable = findReachablesfromaState(controller.s0, controller);
		out.println("\nNo. of init reachable states are " + Integer.toString(initreachable.states.size()));// + controller.draw());

		// find the reachable states to the final state
		gLTS finalreachable = new gLTS();

		out.println("\nNo. of final states are " + Integer.toString(controller.finals.size()));// + controller.draw());

		for(gState fs: controller.finals.values())
		{
			out.println("\nCurrent final: " + fs.s.ID);// + controller.draw());
			if(!finalreachable.states.containsKey(fs.s.ID))
			{
				gLTS glts = findReachablesToState(fs, controller);
				finalreachable.states.putAll(glts.states);
				finalreachable.trans.putAll(glts.trans);
				out.println("No. of final reachable states are " + Integer.toString(finalreachable.states.size()));// + controller.draw());
			}
		}

		HashMap<String,gState> slist = new HashMap<String,gState>();

		for(gState s: controller.states.values())
			if(initreachable.states.get(s.s.ID)!=null &&
			finalreachable.states.get(s.s.ID)!=null)
				slist.put(s.s.ID , s);

		controller.states = slist;
		controller = removeDanglingTransitions(controller);
		return controller;
	}

	private gLTS removeDanglingTransitions(gLTS controller)
	{
		HashMap<String,gTransition> tlist = new HashMap<String,gTransition>();
		for(String tid: controller.trans.keySet())
		{
			gTransition t = controller.trans.get(tid);
			if(controller.states.get(t.src.s.ID)==null ||
					controller.states.get(t.trg.s.ID)==null)
				tlist.put(t.src.s.ID + t.trg.s.ID , t);
		}
		for(String t: tlist.keySet())
			controller.trans.remove(t);

		return controller;
	}

	private gLTS removeBadStates(gLTS plant,
			gLTS controller) {

		return controller;
	}


	private gLTS findReachablesfromaState(gState state, gLTS controller) 
	{

		HashMap<String,gState> slist = new HashMap<String,gState>();
		HashMap<String,gTransition> translist = new HashMap<String,gTransition>();
		Stack<gState> stack = new Stack<gState>();

		slist.put(state.s.ID, state);
		stack.push(state);

		HashMap<String,Boolean> visited = new HashMap<String,Boolean>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			if(visited.get(curstate.s.ID)==null)		
			{
				ArrayList<gTransition> pouttrans = controller.getOutTransof(curstate);

				Iterator<gTransition> iter = pouttrans.iterator();
				while (iter.hasNext()) {
					gTransition ptrans = iter.next();
					slist.put(ptrans.trg.s.ID , ptrans.trg);
					translist.put(ptrans.src.s.ID + ptrans.trg.s.ID , ptrans);
					stack.push(ptrans.trg);
				}
				visited.put(curstate.s.ID , true);
			}
		}

		return new gLTS(controller.ID, controller.s0,slist,controller.finals,translist);
	}


	private gLTS findReachablesToState(gState state, gLTS controller) 
	{

		HashMap<String,gState> slist = new HashMap<String,gState>();
		HashMap<String,gTransition> translist = new HashMap<String,gTransition>();
		Stack<gState> stack = new Stack<gState>();

		slist.put(state.s.ID, state);
		stack.push(state);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			if(!visited.contains(curstate.s.ID))		
			{
				ArrayList<gTransition> pouttrans = controller.getInTransof(curstate);

				Iterator<gTransition> iter = pouttrans.iterator();
				while (iter.hasNext()) {
					gTransition ptrans = iter.next();
					slist.put(ptrans.src.s.ID , ptrans.src);
					translist.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
					stack.push(ptrans.src);
				}
				visited.add(curstate.s.ID);
			}
		}

		return new gLTS(controller.ID, controller.s0,slist,controller.finals,translist);
	}

	private gLTS getForwardablegLTS(gLTS controller, CharSequence finalPlanStates) {

		HashMap<String,gState> slist = new HashMap<String,gState>();
		HashMap<String,gState> finalStates = new HashMap<String,gState>();
		HashMap<String,gTransition> translist = new HashMap<String,gTransition>();
		Stack<gState> stack = new Stack<gState>();

		gState s0 = controller.s0;
		slist.put(s0.s.ID, s0);
		stack.push(s0);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			boolean isFinalPlanState =  curstate.s.ID.contains(finalPlanStates);

			if(!visited.contains(curstate.s.ID) && !isFinalPlanState)		
			{
				ArrayList<gTransition> allOutTrans = controller.getOutTransof(curstate);
				//	ArrayList<gTransition> reconfigTrans = findReconfigAction(allOutTrans);
				Iterator<gTransition> iter = allOutTrans.iterator();
				//						(reconfigTrans.size() > 0? reconfigTrans.iterator(): allOutTrans.iterator());

				while (iter.hasNext()) {
					gTransition ptrans = iter.next();

					if(!visited.contains(ptrans.trg.s.ID))		
					{
						slist.put(ptrans.trg.s.ID , ptrans.trg);
						translist.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
						stack.push(ptrans.trg);
					}
				}
			}
			if(controller.finals.get(curstate.s.ID)!=null)
				finalStates.put(curstate.s.ID , curstate);
			visited.add(curstate.s.ID);
		}
		return new gLTS(controller.ID, controller.s0,slist,finalStates,translist);
	}

	private gLTS getPrioritizedLTS(gLTS controller,
			String finalPlanStates) {

		HashMap<String,gState> slist = new HashMap<String,gState>();
		HashMap<String,gState> finalStates = new HashMap<String,gState>();
		HashMap<String,gTransition> translist = new HashMap<String,gTransition>();
		Stack<gState> stack = new Stack<gState>();

		gState s0 = controller.s0;
		slist.put(s0.s.ID, s0);
		stack.push(s0);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();

			boolean isFinalPlanState =  curstate.s.ID.contains(finalPlanStates);

			if(!visited.contains(curstate.s.ID) && !isFinalPlanState)		
			{
				ArrayList<gTransition> allOutTrans = controller.getOutTransof(curstate);
				ArrayList<gTransition> reconfigTrans = findReconfigAction(allOutTrans);
				Iterator<gTransition> iter = 
						(reconfigTrans.size() > 0? reconfigTrans.iterator(): allOutTrans.iterator());

				while (iter.hasNext()) {
					gTransition ptrans = iter.next();

					slist.put(ptrans.trg.s.ID , ptrans.trg);
					translist.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
					stack.push(ptrans.trg);
				}
			}
			visited.add(curstate.s.ID);

			if(controller.finals.get(curstate.s.ID)!=null)
				finalStates.put(curstate.s.ID , curstate);
		}
		return new gLTS(controller.ID, controller.s0,slist,finalStates,translist);
	}

	private ArrayList<gTransition> findReconfigAction(ArrayList<gTransition> pOuttTans) {
		ArrayList<gTransition> res = new ArrayList<gTransition>();

		for(gTransition t: pOuttTans)
			if(t.lab.isReconfigAction())
				res.add(t);
		return res;
	}

	public gLTS removeCycles(gLTS controller, String outPath) {
		// the second parameter shows the final state of the reconfiguration
		//controller = getPrioritizedLTS(controller, "pp");
		controller = getForwardablegLTS(controller, "pp");

		out.println("\nNo States/Transitions of Prioritized Controller:" + Integer.toString(controller.states.size()) + "/" 
				+Integer.toString(controller.trans.size()));// + controller.draw());

		controller = removeCounreachableStates(controller);
		out.println("No States/Transitions of After Removed Counaccessible States:" + Integer.toString(controller.states.size()) + "/" 
				+Integer.toString(controller.trans.size()) );// + controller.draw());

		controller = removeDanglingTransitions(controller);
		MainCS.putIntoFile(controller.draw(), outPath, "SecondFinalAdaptor");
		out.println("No States/Transitions of Final Adaptor:" + Integer.toString(controller.states.size()) + "/" 
				+Integer.toString(controller.trans.size()));//+ controller.draw());

		return controller;
	}
}

