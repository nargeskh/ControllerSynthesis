// VERY IMPORTANT NOTE: The Node IDs of graph should be same (increased by one) with the order in the LTSs model defined in buildWODESComponentModel
// The second point: the arguments of actions in spec should be in bracket "[]" rather parantheses "()"
package synthesis;

import static java.lang.System.out;
import graphAutomata.GraphAutomata;
import graphLTS.LTS;
import graphLTS.NoStateExistException;
import graphLTS.State;
import graphLTS.gLTS;
import graphLTS.gState;
import graphLTS.gTransition;
import graphTransformation.Graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainCS {

	public static void putIntoFile(String content, String path, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					path + fileName + ".dot")));
			writer.write(content);
			writer.close();
			// String command = "dot -Tjpg -o " + fileName + ".jpg " + fileName
			// + ".dot";
			// Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public static gLTS buildReconfigurationPlan(String name, Graph model) {
	 * // state 0 is assigned to the NA state State s0 = new State("p0"); State
	 * s1 = new State("p1"); State s2 = new State("p2"); State s3 = new
	 * State("p3"); gState gs0 = new gState(s0, model); gState gs1 = new
	 * gState(s1, model); gState gs2 = new gState(s2, model); gState gs3 = new
	 * gState(s3, model); ArrayList<gState> finals = new ArrayList<gState>();
	 * ArrayList<gState> states = new ArrayList<gState>(); states.add(gs0);
	 * states.add(gs1); states.add(gs2); states.add(gs3); // finals.add(gs3);
	 * 
	 * ArrayList<gTransition> trans = new ArrayList<gTransition>();
	 * 
	 * Event eadd = new Event(ACTIVE.OUTPUT, "env","add", "lgr"); gTransition t2
	 * = new gTransition(eadd,gs0,gs1); trans.add(t2); /* Event edis = new
	 * Event(ACTIVE.OUTPUT, "env","del", "ch"); gTransition t3 = new
	 * gTransition(edis,gs1,gs2); trans.add(t3);
	 * 
	 * Event econ = new Event(ACTIVE.OUTPUT, "env","conn", "receiver,lgr");
	 * gTransition t4 = new gTransition(econ,gs2,gs3); trans.add(t4);
	 */
	/*
	 * return new gLTS(name, gs0, states, finals, trans); }
	 */
	private static ArrayList<LTS> buildComponentModel(String inPath,
			String outPath) throws IOException, NoStateExistException {

		LTS logger = LTS.parseLTS("logger", inPath, "lgr", "q0", "q1");
		logger.addInitRemovalStates("q0", "q0");

		LTS cache = LTS.parseLTS("cache", inPath, "ch", "c0", "c2");
		cache.addInitRemovalStates("c0", "c1@c0");

		LTS receiver = LTS.parseLTS("receiver", inPath, "receiver", "a0", "a0");
		receiver.addInitRemovalStates("a0", "a1");

		LTS dispatcher = LTS.parseLTS("dispatcher", inPath, "disp", "d0", "d4");
		dispatcher.addInitRemovalStates("d0", "d0");

		LTS server1 = LTS.parseLTS("server1", inPath, "srv1", "s0", "s6");
		server1.addInitRemovalStates("s0", "s0");

		LTS server2 = LTS.parseLTS("server2", inPath, "srv2", "v0", "v7");
		server2.addInitRemovalStates("v0", "v0");

		LTS server3 = LTS.parseLTS("server3", inPath, "srv3", "w0", "w4");
		server3.addInitRemovalStates("w0", "w0");

		LTS DBA = LTS.parseLTS("DBA", inPath, "DBA", "k0", "k1");
		DBA.addInitRemovalStates("k0", "k0");

		LTS DBB = LTS.parseLTS("DBB", inPath, "DBB", "b0", "b1");
		DBB.addInitRemovalStates("b0", "b0");

		LTS agg = LTS.parseLTS("agg", inPath, "agg", "g0", "g1");
		agg.addInitRemovalStates("g0", "g0");

		out.flush();
		putIntoFile(logger.draw(), outPath, "logger");
		putIntoFile(receiver.draw(), outPath, "receiver");
		putIntoFile(logger.draw(), outPath, "logger");
		putIntoFile(cache.draw(), outPath, "cache");
		putIntoFile(dispatcher.draw(), outPath, "dispatcher");
		putIntoFile(server1.draw(), outPath, "server1");
		putIntoFile(server2.draw(), outPath, "server2");
		putIntoFile(server3.draw(), outPath, "server3");
		putIntoFile(DBA.draw(), outPath, "DBA");
		putIntoFile(agg.draw(), outPath, "agg");

		ArrayList<LTS> ltslist = new ArrayList<LTS>();
		// the order of adding LTSs to the model should be same as their ID in
		// structural model
		ltslist.add(receiver);
		ltslist.add(cache);
		ltslist.add(logger);
		ltslist.add(dispatcher);
		ltslist.add(server1);
		ltslist.add(server2);
		ltslist.add(server3);
		ltslist.add(DBA);
		ltslist.add(DBB);
		ltslist.add(agg);

		return ltslist;

	}

	private static Graph getHeavyLoadStructure(String path) {
		String nodes = "1:receiver;2:ch;4:disp;5:srv1;6:srv2;8:DBA;7:srv3;9:DBB;10:agg";
		String edges = "1->2:receiverch;" + "2->1:chreceiver;"
				+ "1->4:receiverdisp;" + "4->5:dispsrv1;" + "4->6:dispsrv2;"
				+ "8->5:DBAsrv1;" + "8->6:DBAsrv2;" + "9->6:DBBsrv2;"
				+ "9->5:DBBsrv1;" + "9->7:DBBsrv3;" + "5->10:srv1agg;"
				+ "6->10:srv2agg;" + "6->7:srv2srv3;" + "10->2:aggch;"
				+ "7->6:srv3srv2";

		Graph model = new Graph("model", "(10,15)" + nodes + "|" + edges);
		/*
		 * String nodes = "1:receiver;3:lgr"; String edges = "1->3:receiverlgr";
		 * Graph model = new Graph("model", "(2,1)"+ nodes + "|" + edges);
		 */
		out.println("The initial structure of system is:\n" + model.toDot());
		putIntoFile(model.toDot(), path, "structure");

		return model;
	}

	@SuppressWarnings("unused")
	private static Graph getLightLoadStructure(String path) {
		String nodes = "1:receiver;3:lgr;4:disp;5:srv1;8:DBA;7:srv3;9:DBB;10:agg";
		String edges = "1->3:receiverlgr;" + "1->4:receiverdisp;"
				+ "4->5:dispsrv1;" + "10->3:agglgr;" + "4->7:dispsrv3;"
				+ "8->5:DBAsrv1;" + "9->5:DBBsrv1;" + "9->7:DBBsrv3;"
				+ "5->10:srv1agg;" + "7->10:srv3agg";
		Graph model = new Graph("model", "(8,14)" + nodes + "|" + edges);
		/*
		 * String nodes = "1:receiver;3:lgr"; String edges = "1->3:receiverlgr";
		 * Graph model = new Graph("model", "(2,1)"+ nodes + "|" + edges);
		 */
		out.println("The initial structure of system is:\n" + model.toDot());
		// putIntoFile( model.toDot(), path, "structure");

		return model;
	}

	@SuppressWarnings("unused")
	private static Graph getTestStructure() {
		String nodes = "1:receiver;2:ch";
		String edges = "1->2:receiverch;" + "1->1:receiverreceiver;"
				+ "2->1:chreceiver"
				// + "1->4:receiverdisp"
				;
		Graph model = new Graph("model", "(2,2)" + nodes + "|" + edges);
		out.println("The initial structure of system is:\n" + model.toDot());
		// putIntoFile( model.toDot(), "structure");

		return model;
	}

	private static void test_synthesise_phase2() throws IOException,
	NoStateExistException {

		String outPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/output/";
		String inPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/input/";
		ArrayList<LTS> ltslist = buildComponentModel(inPath, outPath);
		Graph model = getHeavyLoadStructure(outPath);

		// create an empty plan
		gState planS0 = new gState(new State("p0"), model);
		HashMap<String, gState> planStates = new HashMap<String, gState>();
		HashMap<String, gState> planFinalStates = new HashMap<String, gState>();
		planStates.put("p0", planS0);
		planFinalStates.put("p0", planS0);
		HashMap<String, gTransition> trans = new HashMap<String, gTransition>();
		gLTS plan = new gLTS("plan", planS0, planStates, planFinalStates, trans);
		// out.println("The reconfiguration plan:\n" + plan.draw());

		// create the system model
		gLTS gts = new gLTS();
		gts = gts.applyReconfiguration(ltslist, plan, false);
		// gts.transToIntStates();
		gts.removeStateSplitter();
		// putIntoFile( gts.draw(), "model");

		GraphAutomata spec = GraphAutomata.parseGraphAutomata("spec", "s0",
				"s0", outPath);
		// spec.finalstates.add(spec.s0);
		List<String> uncontrolledEvents = new ArrayList<String>();
		uncontrolledEvents.add("env.dis(srv3,srv2)");

		Synthesizer synth = new Synthesizer();
		gLTS adaptor = synth.synthesize(gts, spec, plan, uncontrolledEvents,
				true, outPath);
		// putIntoFile( adaptor.draw(), "adaptor");

	}

	private static void test_synthesise_phase(String inPath, String outPath)
			throws IOException, NoStateExistException {

		GraphAutomata spec = GraphAutomata.parseGraphAutomata("specTest", "s0",
				"s0", inPath);

		Graph model = new Graph("model", "(1,1)1:plant|1->1:plantplant");

		gState planS0 = new gState(new State("p0"), model);
		HashMap<String, gState> planStates = new HashMap<String, gState>();
		HashMap<String, gState> planFinalStates = new HashMap<String, gState>();
		planStates.put("p0", planS0);
		planFinalStates.put("p0", planS0);
		HashMap<String, gTransition> trans = new HashMap<String, gTransition>();
		gLTS plan = new gLTS("plan", planS0, planStates, planFinalStates, trans);

		LTS agg = LTS.parseLTS("plant", inPath, "plant", "g0", "g1");
		agg.addInitRemovalStates("g0", "g0");
		ArrayList<LTS> ltslist = new ArrayList<LTS>();
		ltslist.add(agg);

		gLTS gts = new gLTS();
		gts = gts.applyReconfiguration(ltslist, plan, false);
		out.println("The system model is:\n" + gts.draw(0, 75));
		gts.removeStateSplitter();

		List<String> uncontrolledEvents = new ArrayList<String>();
		uncontrolledEvents.add("env.dis(srv3,srv2)");
		uncontrolledEvents.add("ana.req(x)");
		// uncontrolledEvents.add("env.del(srv2)");
		// uncontrolledEvents.add("env.del(ch)");
		// uncontrolledEvents.add("env.add(lgr)");

		Synthesizer synth = new Synthesizer();
		gLTS adaptor = synth.synthesize(gts, spec, plan, uncontrolledEvents,
				false, outPath);

		putIntoFile(adaptor.draw(0, 75), outPath, "adaptor");
	}


	private static ArrayList<gLTS> buildgLTSModel(String outPath, String inPath,
			String plan, 
			boolean heavyLight) throws IOException,
			NoStateExistException {
		long startTime = System.currentTimeMillis();

		ArrayList<LTS> ltslist = buildComponentModel(inPath, outPath);
		Graph model = new Graph();
		if (heavyLight)
			model = getHeavyLoadStructure(outPath);
		else
			model = getLightLoadStructure(outPath);

		String[] planPars = plan.split("@");
		gLTS plangLTS = gLTS.parsegLTS(planPars[0], inPath, planPars[1], "pa",
				model);
		putIntoFile(plangLTS.draw(0, 75), outPath, "plan");
		gLTS plant = new gLTS();
		plant = plant.applyReconfiguration(ltslist, plangLTS, false);
		plant.removeStateSplitter();

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		out.println("\nModel Construction Time:" + Long.toString(duration)+
				"\nNumber of states/transitions: " +
				Integer.toString(plant.states.size()) + "/" +
				Integer.toString(plant.trans.size())
				+ "\nModel Construction Time:" + Long.toString(duration));


		ArrayList<gLTS> res = new ArrayList<gLTS>();
		res.add(plangLTS);
		res.add(plant);

		return res;
	}

	private static String synthesize(String outPath, String inPath,
			String spec, gLTS plant, gLTS plangLTS, ArrayList<String> uncontrolledEvents,
			boolean drawing, String fileName) throws IOException, NoStateExistException
	{
		String[] specPars = spec.split("@");
		GraphAutomata specBAutomata = GraphAutomata.parseGraphAutomata(
				specPars[0], specPars[1], specPars[2], inPath);

		Synthesizer synth = new Synthesizer();
		long startTime = System.currentTimeMillis();
		gLTS adaptor = synth.synthesize(plant, specBAutomata, plangLTS,
				uncontrolledEvents, drawing, outPath);

	//	putIntoFile(adaptor.draw(0, 75), outPath, fileName);
		long duration = System.currentTimeMillis() - startTime;
		String output = ("Synthesis Time:" + Long.toString(duration) +
				"\nNumber of states/transitions: " +
				Integer.toString(adaptor.states.size()) + "/" +
				Integer.toString(adaptor.trans.size()) + 
				"\nSynthesis Time:" + Long.toString(duration)+ "\n\n");
		return output;
	}

	static void printEachConfigAdaptor(gLTS adaptor, gLTS plan, String path,
			String fileName, String configs) {
		for (gState s : plan.states.values())
			if (configs.contains(s.s.ID)) {
				gLTS configAdaptor = adaptor.getGLTSofConfig(s.s.ID, "eee");
				out.println("The configuration controller in " + s.s.ID + ":"
						+ Integer.toString(configAdaptor.states.size()));
				putIntoFile(configAdaptor.draw(0, 75), path, fileName + s.s.ID);
			}
	}

	private static ArrayList<String> getThirdUncontrallobleSets() {
		ArrayList<String> uncontrolledEvents = new ArrayList<String>();
		uncontrolledEvents.add("DBA.getData(x)");
		uncontrolledEvents.add("DBB.getData(x)");
		uncontrolledEvents.add("srv2.process(x)");
		uncontrolledEvents.add("srv1.process(x)");
		uncontrolledEvents.add("srv3.process(x)");
		return uncontrolledEvents;
	}

	private static final long MEGABYTE = 1024L * 1024L;
	private static final long KBYTE = 1024L ;

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

	public static long bytesToKilobytes(long bytes) {
		return bytes / KBYTE;
	}

	public static String MemoryUsage() {
		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		runtime.gc();
		// Calculate the used memory
		long memory = runtime.totalMemory() - runtime.freeMemory();
		String res = ("Used memory is bytes: " + memory);
		res += ("\nUsed memory is megabytes: "
				+ bytesToKilobytes(memory));
		return res;	  
	}

	public static void main(String[] args) throws IOException,
	NoStateExistException {
		String outPath = "/Users/nkhadmin/Dropbox/Workspaces/synthesis/IO/output/";
		String inPath = "/Users/nkhadmin/Dropbox/Workspaces/synthesis/IO/inputs/";

		String output = "";
	
		ArrayList<gLTS> res = new ArrayList<gLTS>();
		gLTS plangLTS = new gLTS();
		gLTS plant= new gLTS();
		String filename;

		String input = "1@1@3;1@2@1;1@2@2;1@2@3;1@3@2;1@4@3;";
		input += "2@2@1;2@2@2;2@2@3;2@3@3;2@4@2;2@1@2;";
		input += "3@1@2;3@2@1;3@3@2;3@4@2;3@4@3";
		String[] items = input.split(";");
		boolean[] gltsCreated = {false,false,false};

		for(int k=0;k < items.length ; k++)
		{
			String[] ids = items[k].split("@");
			String j1 =  ids[0];	
			String j2 = ids[1];	
			String j3 = ids[2];	

			if(!gltsCreated[Integer.parseInt(j1) -1])
			{
				boolean reconf = !(Integer.parseInt(j1)==3);

				res  = buildgLTSModel(outPath, inPath, "Pl"+ j1+"@pa", reconf);
				gltsCreated[Integer.parseInt(j1)-1]= true;
			}
			plangLTS = res.get(0);
			plant = res.get(1);

			ArrayList<String> uncontrolledEvents1 = new ArrayList<String>();
			if (Integer.parseInt(j3) == 2)
				uncontrolledEvents1.add("ana.req(x)");
			else if (Integer.parseInt(j3) == 3)
				uncontrolledEvents1 = getThirdUncontrallobleSets();
			filename = "Pl" + j1 + 
					"p" + (j2)+ 
					"e"+(j3);

			output += "\n\n The plan is Pl" + j1 + "\n The property is p"
					+ (j2)
					+ "\n The uncontrollanle events are e"
					+ String.valueOf(Integer.parseInt(j3)-1) + "\n";
			if(Integer.parseInt(j2)!=4)
				output += synthesize(outPath, inPath, "p" + (j2) + "@s0@s0", plant, plangLTS, uncontrolledEvents1, false, filename);
			else
				output += synthesize(outPath, inPath, "p" + (j2) + "@s00@s0", plant, plangLTS, uncontrolledEvents1, false, filename);
			output += MemoryUsage();
			out.println("\n\n**********************************************" + output);

		}
	}
}