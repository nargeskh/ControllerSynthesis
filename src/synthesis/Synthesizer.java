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
import java.util.List;
import java.util.Stack;

public class Synthesizer {

	public gLTS synthesize(gLTS plant, GraphAutomata spec, gLTS plan,
			List<String> uncontrolledEvents, boolean drawing, String outPath) {
		out.println("\n************************\nNo. of States/Transitions/Finals of plant:"
				+ Integer.toString(plant.states.size()) + "/"
				+ Integer.toString(plant.trans.size()) + "/"
				+ Integer.toString(plant.finals.size()));

		String draw = "";
		gLTS controller = new gLTS();
		try {
			controller = product(plant, spec);
			/*		out.println("\n************************\nNo. of States/Transitions/Finals of Product:"
					+ Integer.toString(controller.states.size()) + "/"
					+ Integer.toString(controller.trans.size())+ "/"
					+ Integer.toString(plant.finals.size()));

			ExampleMain.putIntoFile(controller.connectingNodesDraw(),
					outPath, "connectings.gv");

			if (drawing)
			{
				draw = controller.filter_gml_draw(0,75, "NOP","e1");
				out.println(drawing ? "\nThe Product of SPEC and PLANT is :"
						+ draw: "");
				ExampleMain.putIntoFile(draw,
						outPath, "product.gml");
			}
*/
			controller = removeBadStates(plant, controller, uncontrolledEvents, false);
			/*out.println("\n************************\nNo. of States/Transitions after bad state removal:"
					+ Integer.toString(controller.states.size()) + "/"
					+ Integer.toString(controller.trans.size()));
			out.println("\nController after removing bad states:"
					+ (drawing ? controller.draw(0, 75) : ""));
*/
			controller = this.removeCounreachableStates(controller);
			out.println("\n************************\nNo States/Transitions/Finals of final adaptor:"
					+ Integer.toString(controller.states.size()) + "/"
					+ Integer.toString(controller.trans.size())+ "/"
					+ Integer.toString(controller.trans.size()));
			//out.println("\nController after removing un-coaccesible states:"
			//		+ (drawing ? controller.draw(0, 75) : ""));

		} catch (NoStateExistException e) {
			e.printStackTrace();
		}
		return controller;

	}

	private gLTS product(gLTS plant, GraphAutomata spec)
			throws NoStateExistException {
		gState s0 = new gState(new State(plant.s0.s.ID + "@" + spec.s0.ID),
				plant.s0.g);
		gLTS adaptor = new gLTS("adaptor", s0, new HashMap<String, gState>(),
				new HashMap<String, gState>(),
				new HashMap<String, gTransition>());

		adaptor.states.put(s0.s.ID, s0);
		// slist.add(s0);

		// put all destination of current state in s1 in stack and repeat the
		// algorithm for them
		Stack<gState> stack = new Stack<gState>();
		stack.push(s0);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			if (!visited.contains(curstate.s.ID)) {
				String[] sIDs = curstate.s.ID.split("@");

				// get the current state of plant and the spec
				gState pstate = plant.getStatebyID(sIDs[0]);
				State sstate = spec.getStatebyID(sIDs[1]);
				//String curBuchiStateID = sIDs[2];

				// get the outgoing transition of current state of plant
				ArrayList<gTransition> pouttrans = plant.getOutTransof(pstate.s.ID);

				Iterator<gTransition> iter = pouttrans.iterator();
				while (iter.hasNext()) {
					gTransition ptrans = iter.next();

					// find all the satisfying transitions from spec
					ArrayList<gExptransition> satTransList = spec
							.findSatisfiableTrans(ptrans, sstate);
					/*	out.println("The number of outgoing and satisfied ones are :" + 
							Integer.toString(pouttrans.size()) +
							"     " + Integer.toString(satTransList.size()));
					 */	

					Iterator<gExptransition> itera = satTransList.iterator();
					while (itera.hasNext()) {
						// out.println("C" + Integer.toString((counter++)));
						gExptransition strans = itera.next();
						String trgBuchiStateID;

						//trgBuchiStateID = curBuchiStateID;

						/*if (spec.isfinalState(sstate.ID)) 
							if(plant.isfinalState(pstate.s.ID))
								trgBuchiStateID = "2";
							else
								trgBuchiStateID = "1";
						if ( plant.isfinalState(pstate.s.ID)) 
							trgBuchiStateID = "2";
						if (curBuchiStateID.contains("2"))
							trgBuchiStateID = "0";
*/
						String sid = ptrans.trg.s.ID + "@" + strans.trg.ID;
						gState trgstate = new gState(new State(sid),
								ptrans.trg.g);

						// check if the new state has already added to the model
						// or not
						gState ngs = adaptor.getGStatebyID(sid, trgstate.g);
						if (ngs != null)
							trgstate = ngs;
						else
							adaptor.states.put(trgstate.s.ID, trgstate);

						if (!visited.contains(trgstate))
							stack.push(trgstate);
						gTransition gt = new gTransition(ptrans.lab, curstate,
								trgstate);
						adaptor.trans.put(gt.getLabel(), gt);

						// if both plant and system model are in final states,
						// then this state is final
						if (spec.isfinalState(strans.trg.ID)
							&& plant.finals.get(ptrans.trg.s.ID) != null)
							adaptor.finals.put(trgstate.s.ID, trgstate);
					}
				}
				visited.add(curstate.s.ID);
			}
		}
		out.println("Number of visited states are:" + 
				Integer.toString(visited.size()));
		return adaptor;
	}

	// we first find the initial reachable state, initreachable
	// then for each final state. we look for the states that are reachable from one of 
	// the previously to-final-reachable states 
	gLTS removeCounreachableStates(gLTS controller) {

		// find the reachable states from the initial state
		gLTS initReachable = findReachablesfromaState(controller.s0, controller);
		gLTS result = new gLTS();
		out.println("\nNo. of init reachable states are "
				+ Integer.toString(initReachable.states.size()) + 
				"\nNumber of final states is: " + Integer.toString(initReachable.finals.size()) );// +

		//gLTS finalreachable = result;
		HashMap<String, gState> orgiFinals = initReachable.finals;

		for (gState fs : orgiFinals.values()) {
		//	out.println("Current final: " + fs.s.ID);
			if (!result.states.containsKey(fs.s.ID)) {
				result = findReachablesToState(fs, initReachable,result);
				//finalreachable.states.putAll(glts.states);
				//finalreachable.trans.putAll(glts.trans);
			//	out.println("No. of final reachable states are "
			//			+ Integer.toString(result.states.size()));// +
				// controller.draw());
			}
		}

		/*HashMap<String, gState> slist = new HashMap<String, gState>();

		for (gState s : result.states.values())
			if (finalreachable.states.get(s.s.ID) != null)
				slist.put(s.s.ID, s);

		//		controller.finals = slist;	
		//		out.println( controller.draw(0,0));

		result.states = slist;*/

		result = removeDanglingTransitions(result);
		return result;
	}

	private gLTS removeDanglingTransitions(gLTS controller) {		
		HashMap<String, gTransition> tlist = new HashMap<String, gTransition>();
		for (String tid : controller.trans.keySet()) {
			gTransition t = controller.trans.get(tid);
			if (controller.states.get(t.src.s.ID) != null
					&& controller.states.get(t.trg.s.ID) != null)
				tlist.put(t.src.s.ID + t.trg.s.ID, t);
		}

		return new gLTS("res", controller.s0, controller.states, controller.finals, tlist);
	}

	// compare plant and controller, if there is a state from which there is an
	// uncontrollable state
	// that we have removed in the controller, then remove all its transitions
	// and that state
	private gLTS removeBadStates(gLTS plant, gLTS controller,
			List<String> uncontrollableEventlist, boolean commentOn) {

		if(uncontrollableEventlist.size()==0)
			return controller;

		out.println("The initial state of the controller " + controller.s0.s.ID
				+ "\n " + "The initial state of plant " + plant.s0.s.ID);

		ArrayList<String> badStates = new ArrayList<String>();

		for (gState plantState : plant.states.values()) {
		/*	if(commentOn)
				out.println("The current state of the plant " + plantState.s.ID
			 );*/
			gState controllerState = controller
					.getStateBeginWithID(plantState.s.ID);
			
			if (controllerState != null) {
				ArrayList<String> controllerOutEvents = controller
						.getOutEventsof(controllerState);

				ArrayList<String> plantOutEvents = plant
						.getUncontrollableEventsof(plantState,
								uncontrollableEventlist);

				boolean goodState = true;
				for (String event : plantOutEvents)
				{
					goodState = goodState
							&& (controllerOutEvents.contains(event));
						if(!goodState && commentOn)
							out.println("The uncontrollable event " + event + " was disabled");
				}

				if (!goodState)
					badStates.add(controllerState.s.ID);
			}
		}

		controller = controller.removeStates(badStates, commentOn);

		if(commentOn)
		{
			out.println("The bad states are:\n ");
			for (String s : badStates)
				out.println(s + "  ");
		}

		return controller;
	}

	private gLTS findReachablesfromaState(gState state, gLTS controller) {

		HashMap<String, gState> states = new HashMap<String, gState>();
		HashMap<String, gState> finalstates = new HashMap<String, gState>();
		HashMap<String, gTransition> translist = new HashMap<String, gTransition>();
		Stack<gState> stack = new Stack<gState>();

		states.put(state.s.ID, state);
		stack.push(state); 

		HashMap<String, Boolean> visited = new HashMap<String, Boolean>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			//out.println(curstate.s.ID + "\n");

			if (visited.get(curstate.s.ID) == null) {
				ArrayList<gTransition> pouttrans = controller
						.getOutTransof(curstate.s.ID);

				Iterator<gTransition> iter = pouttrans.iterator();
				while (iter.hasNext()) {
					gTransition ptrans = iter.next();
					states.put(ptrans.trg.s.ID, ptrans.trg);
					translist.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
					if(controller.finals.containsKey(ptrans.trg.s.ID))
						finalstates.put(ptrans.trg.s.ID, ptrans.trg);
					//	out.println(ptrans.src.s.ID + " -> " + ptrans.trg.s.ID + "\n");
					stack.push(ptrans.trg);
				}
				visited.put(curstate.s.ID, true);
			}
		}

		return new gLTS(controller.ID, state, states, finalstates,translist);
	}

	private gLTS findReachablesToState(gState state, gLTS init, gLTS result) {

		HashMap<String, gState> slist = new HashMap<String, gState>();
		//	HashMap<String, gTransition> translist = new HashMap<String, gTransition>();
		Stack<gState> stack = new Stack<gState>();

		ArrayList<String> visited = new ArrayList<String>();

		gLTS res = result;

		if(result.states.size()>0)
			res = new gLTS("res", result.s0, result.states, result.finals, result.trans);

		slist.put(state.s.ID, state);
		stack.push(state);

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			if (!res.states.containsKey(curstate.s.ID) &&
					!visited.contains(curstate.s.ID)) {
				ArrayList<gTransition> pouttrans = init.getInTransof(curstate);

				Iterator<gTransition> iter = pouttrans.iterator();
				while (iter.hasNext()) {
					gTransition ptrans = iter.next();
					slist.put(ptrans.src.s.ID, ptrans.src);
					//				translist.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
					res.trans.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
					stack.push(ptrans.src);
					//out.println( ptrans.src.s.ID +  " is added to the stack");
				}
				visited.add(curstate.s.ID);

				if(init.finals.containsKey(curstate.s.ID))
					res.finals.put(curstate.s.ID, curstate);

			}
		}
		res.states.putAll(slist);

		//		return new gLTS(controller.ID, controller.s0, slist, controller.finals,
		//				translist);
		return res;
	}

	private gLTS getForwardablegLTS(gLTS controller,
			CharSequence finalPlanStates) {

		HashMap<String, gState> slist = new HashMap<String, gState>();
		HashMap<String, gState> finalStates = new HashMap<String, gState>();
		HashMap<String, gTransition> translist = new HashMap<String, gTransition>();
		Stack<gState> stack = new Stack<gState>();

		gState s0 = controller.s0;
		slist.put(s0.s.ID, s0);
		stack.push(s0);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();
			boolean isFinalPlanState = curstate.s.ID.contains(finalPlanStates);

			if (!visited.contains(curstate.s.ID) && !isFinalPlanState) {
				ArrayList<gTransition> allOutTrans = controller
						.getOutTransof(curstate.s.ID);
				// ArrayList<gTransition> reconfigTrans =
				// findReconfigAction(allOutTrans);
				Iterator<gTransition> iter = allOutTrans.iterator();
				// (reconfigTrans.size() > 0? reconfigTrans.iterator():
				// allOutTrans.iterator());

				while (iter.hasNext()) {
					gTransition ptrans = iter.next();

					if (!visited.contains(ptrans.trg.s.ID)) {
						slist.put(ptrans.trg.s.ID, ptrans.trg);
						translist
						.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
						stack.push(ptrans.trg);
					}
				}
			}
			if (controller.finals.get(curstate.s.ID) != null)
				finalStates.put(curstate.s.ID, curstate);
			visited.add(curstate.s.ID);
		}
		return new gLTS(controller.ID, controller.s0, slist, finalStates,
				translist);
	}

	private gLTS getPrioritizedLTS(gLTS controller, String finalPlanStates) {

		HashMap<String, gState> slist = new HashMap<String, gState>();
		HashMap<String, gState> finalStates = new HashMap<String, gState>();
		HashMap<String, gTransition> translist = new HashMap<String, gTransition>();
		Stack<gState> stack = new Stack<gState>();

		gState s0 = controller.s0;
		slist.put(s0.s.ID, s0);
		stack.push(s0);

		ArrayList<String> visited = new ArrayList<String>();

		while (!stack.isEmpty()) {
			gState curstate = stack.pop();

			boolean isFinalPlanState = curstate.s.ID.contains(finalPlanStates);

			if (!visited.contains(curstate.s.ID) && !isFinalPlanState) {
				ArrayList<gTransition> allOutTrans = controller
						.getOutTransof(curstate.s.ID);
				ArrayList<gTransition> reconfigTrans = findReconfigAction(allOutTrans);
				Iterator<gTransition> iter = (reconfigTrans.size() > 0 ? reconfigTrans
						.iterator() : allOutTrans.iterator());

				while (iter.hasNext()) {
					gTransition ptrans = iter.next();

					slist.put(ptrans.trg.s.ID, ptrans.trg);
					translist.put(ptrans.src.s.ID + ptrans.trg.s.ID, ptrans);
					stack.push(ptrans.trg);
				}
			}
			visited.add(curstate.s.ID);

			if (controller.finals.get(curstate.s.ID) != null)
				finalStates.put(curstate.s.ID, curstate);
		}
		return new gLTS(controller.ID, controller.s0, slist, finalStates,
				translist);
	}

	private ArrayList<gTransition> findReconfigAction(
			ArrayList<gTransition> pOuttTans) {
		ArrayList<gTransition> res = new ArrayList<gTransition>();

		for (gTransition t : pOuttTans)
			if (t.lab.isReconfigAction())
				res.add(t);
		return res;
	}

	public gLTS removeCycles(gLTS controller, String outPath) {
		// the second parameter shows the final state of the reconfiguration
		// controller = getPrioritizedLTS(controller, "pp");
		controller = getForwardablegLTS(controller, "pp");

		out.println("\nNo States/Transitions of Prioritized Controller:"
				+ Integer.toString(controller.states.size()) + "/"
				+ Integer.toString(controller.trans.size()));// +
		// controller.draw());

		controller = removeCounreachableStates(controller);
		out.println("No States/Transitions of After Removed Counaccessible States:"
				+ Integer.toString(controller.states.size())
				+ "/"
				+ Integer.toString(controller.trans.size()));// +
		// controller.draw());

		controller = removeDanglingTransitions(controller);
		MainCS.putIntoFile(controller.draw(0, 75), outPath,
				"SecondFinalAdaptor");
		out.println("No States/Transitions of Final Adaptor:"
				+ Integer.toString(controller.states.size()) + "/"
				+ Integer.toString(controller.trans.size()));// +
		// controller.draw());

		return controller;
	}
}
