package graphAutomata;

import graphLTS.NoStateExistException;
import graphLTS.State;
import graphLTS.gTransition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import basics.TreeBooleanEvaluator;

public class GraphAutomata{

	public ArrayList<State> states = new ArrayList<State>();
	public ArrayList<gExptransition> trans = new ArrayList<gExptransition>();
	public State s0 = new State();
	public String ID;

	public ArrayList<State> finalstates = new ArrayList<State>();

	public GraphAutomata(ArrayList<State> sts, ArrayList<gExptransition>trs, 
			State init, String id, ArrayList<State> fs)
	{
		this.states = sts;
		this.trans = trs;
		this.s0 = init;
		this.ID = id;
		this.finalstates = fs;
	}

	public GraphAutomata() {
		// TODO Auto-generated constructor stub
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
		}
		return null;
	}

	// find all transitions of state sstate in specification
	// that allow the transition ctrans of the plant
	public ArrayList<gExptransition> findSatisfiableTrans(gTransition ctrans,
			State sstate) {

		ArrayList<gExptransition> outTrans = new ArrayList<gExptransition>();
		TreeBooleanEvaluator evaluator = new TreeBooleanEvaluator();
		evaluator.context = ctrans;

		for(gExptransition t:trans)
		{
			List<String> evalseq = TreeBooleanEvaluator.eval(evaluator, t.label + "&& true");
			boolean evalres = evalseq.get(evalseq.size() -1).endsWith("true");

			if(t.src.ID.equalsIgnoreCase(sstate.ID) && evalres)
				outTrans.add(t);
			/*			else
				out.println(t.label + " is unsatisfiable in " + ctrans.lab.print_event() +
						"\n" + t.src.ID + " vs  " + sstate.ID + "\n");*/
		}
		return outTrans;
	}

	public boolean isfinalState(String iD2) {

		for(State s: finalstates)
			if(s.ID.equals(iD2))
				return true;
		return false;
	}

	public static GraphAutomata parseGraphAutomata(String filename, String s0, String finals, String inPath) throws IOException, NoStateExistException
	{
		GraphAutomata automata = new GraphAutomata();
		automata.ID = filename;
		try {
			BufferedReader br = new BufferedReader(new FileReader( inPath + filename + ".txt"));
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
				String edge = substr[i].replace("$", ";");
				String[] str = edge.split("->");

				//adding the source node to the list, if it is not in state set
				State srcstate = automata.getStatebyID(str[0]);
				if(srcstate==null)
				{
					srcstate = new State(str[0]);
					automata.states.add(srcstate);
					if(str[0].compareTo(s0)==0)
						automata.s0 = srcstate;
					if(finals.contains(str[0]))
						automata.finalstates.add(srcstate);

				}

				String[] str1 = str[1].split("label=");

				//adding the target node to the list, if it is not in state set
				String trg = str1[0].substring(0, str1[0].length()-1);
				State trgstate = automata.getStatebyID(trg); 
				if(trgstate==null)
				{
					trgstate = new State(trg);
					automata.states.add(trgstate);
					if(finals.contains(trgstate.ID))
						automata.finalstates.add(trgstate);
				}

				String lab = str1[1].substring(1,str1[1].length()-2);
				lab = lab.replace("=>", "->");
				gExptransition t1 = new gExptransition(lab,srcstate,trgstate);
				automata.trans.add(t1);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return automata; 
	}


}
