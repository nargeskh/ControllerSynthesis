// VERY IMPORTANT NOTE: The Node IDs of graph should be same (increased by one) with the order in the LTSs model defined in buildWODESComponentModel
// The second point: the arguments of actions in spec should be in bracket "[]" rather parantheses "()"
package synthesis;

import static java.lang.System.out;
import graphAutomata.GraphAutomata;
import graphLTS.LTS;
import graphLTS.NoStateExistException;
import graphLTS.gLTS;
import graphLTS.gState;
import graphTransformation.Graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WODESMain {

	public static void putIntoFile(String content, String path , String fileName){
		try {			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + fileName + ".dot")));
			writer.write(content);
			writer.close();
			//			String command = "dot -Tjpg -o " + fileName + ".jpg " + fileName + ".dot";
			//			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static  ArrayList<LTS> buildWODESComponentModel(String inPath,String outPath) throws IOException, NoStateExistException {

		LTS logger = LTS.parseLTS("logger", inPath, "lgr", "b0","b1");
		logger.addInitRemovalStates("b0" , "b0");

		LTS cache = LTS.parseLTS("cache",inPath,"ch", "c0","c2");
		cache.addInitRemovalStates("c0" , "c1@c0");

		LTS receiver = LTS.parseLTS("receiver",inPath,"receiver","a3","a0");
		receiver.addInitRemovalStates("a0" , "a1");

		LTS server1 = LTS.parseLTS("server1",inPath,"srv1","d0","d1");
		server1.addInitRemovalStates("d0" , "d0");

		out.flush();
		putIntoFile(logger.draw(), outPath, "logger");
		putIntoFile(receiver.draw(),outPath, "receiver");
		putIntoFile(cache.draw(), outPath, "cache");
		putIntoFile(server1.draw(), outPath, "server1");

		ArrayList<LTS> ltslist = new ArrayList<LTS>();
		//the order of adding LTSs to the model should be same as their ID in structural model
		ltslist.add(receiver);
		ltslist.add(cache);
		ltslist.add(server1);
		ltslist.add(logger);

		return ltslist;

	}


	private static Graph getWODESPaperStructure()
	{
		String nodes = "1:receiver;2:ch;3:srv1";
		String edges = 
				"1->2:receiverch;"
						+ "1->1:receiverreceiver;"
						+ "1->3:receiversrv1;";
		Graph model = new Graph("model", "(3,3)"+ nodes + "|" + edges);
		out.println("The initial structure of system is:\n" + model.toDot());
		//		putIntoFile( model.toDot(), "structure");

		return model;
	}


	private static void WODESSynthesize(String inPath,String outPath) throws IOException, NoStateExistException {

		ArrayList<LTS>  ltslist = buildWODESComponentModel(inPath, outPath);
		Graph model = getWODESPaperStructure();
		gLTS plan = gLTS.parsegLTS("plan", inPath, "plan", "pa", model);
		out.println("The reconfiguration plan:\n" + plan.draw(100,100));

		gLTS gts = new gLTS();
		gts = gts.applyReconfiguration(ltslist, plan,false);
		gts.removeStateSplitter();
		putIntoFile( gts.draw(100,100), outPath, "model");
		printEachConfigAdaptor(gts, plan, outPath,"model");

		//		GraphAutomata spec = GraphAutomata.parseGraphAutomata("spec", "s0", "s0",inPath);

		//this the example with no deadlock 
		GraphAutomata spec = GraphAutomata.parseGraphAutomata("NoDeadlockSpec", "s0", "s0",inPath);

		List<String> uncontrolledEvents = new ArrayList<String>();
		//uncontrolledEvents.add("X.a()");
		//uncontrolledEvents.add("X.e()");

		Synthesizer synth = new Synthesizer();
		gLTS adaptor = synth.synthesize(gts, spec, plan, uncontrolledEvents,false,outPath);

		//adaptor = synth.removeCycles(adaptor, outPath);
		putIntoFile( adaptor.draw(100,100), outPath, "adaptor");
		printEachConfigAdaptor(adaptor, plan, outPath,"adaptor");
	}


	private static void printEachConfigAdaptor(gLTS adaptor, gLTS plan, String path, String fileName) {
		for(gState s:plan.states.values())
		{
			gLTS configAdaptor = adaptor.getGLTSofConfig(s.s.ID, "");
			out.println("The configuration controller in " + s.s.ID + 
					":" + Integer.toString(configAdaptor.states.size()));
			putIntoFile( configAdaptor.draw(100,100), path , "adaptor"+s.s.ID);
		}

	}

	public static void main(String[] args) throws IOException, NoStateExistException {
		String outPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/IO/outputWODES/";
		String inPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/IO/inputWODES/";
		WODESSynthesize(inPath,outPath);
	}


}