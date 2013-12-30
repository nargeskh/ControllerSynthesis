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

public class ExampleMain {

	public static void putIntoFile(String content, String path, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					path + fileName + ".gv")));
			writer.write(content);
			writer.close();
			// String command = "dot -Tjpg -o " + fileName + ".jpg " + fileName
			// + ".dot";
			// Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<LTS> buildComponentModel(String inPath,
			String outPath) throws IOException, NoStateExistException {

		LTS compA = LTS.parseLTS("compA", inPath, "compA", "a0", "a1");
		compA.addInitRemovalStates("a0", "a0");

		LTS compC = LTS.parseLTS("compC", inPath, "compC", "c0", "c3");
		compC.addInitRemovalStates("c0", "c0@c2@c1");

		LTS compD = LTS.parseLTS("compD", inPath, "compD", "d0", "d1");
		compD.addInitRemovalStates("d0", "d0");

		LTS compB = LTS.parseLTS("compB", inPath, "compB", "b2", "b2");
		compB.addInitRemovalStates("b0", "b0@b1");

		LTS compE = LTS.parseLTS("compE", inPath, "compE", "e0", "e0");
		compE.addInitRemovalStates("e0", "e0");

		out.flush();
		putIntoFile(compA.draw(), outPath, "compA");
		putIntoFile(compB.draw(), outPath, "compB");
		putIntoFile(compC.draw(), outPath, "compC");
		putIntoFile(compD.draw(), outPath, "compD");

		ArrayList<LTS> ltslist = new ArrayList<LTS>();
		// the order of adding LTSs to the model should be same as their ID in
		// structural model
		ltslist.add(compA);
		ltslist.add(compC);
		ltslist.add(compD);
		ltslist.add(compE);
		ltslist.add(compB);

		return ltslist;

	}

	private static Graph getStructure() {
		String nodes = "1:compA;2:compC;3:compD;4:compE";
		String edges = "1->4:compAcompE;" + "1->2:compAcompC;"
				+ "2->1:compCcompA;" + "2->3:compCcompD;" + "3->2:compDcompC;"
				+ "3->1:compDcompA;" + "1->3:compAcompD";
		Graph model = new Graph("model", "(4,7)" + nodes + "|" + edges);
		out.println("The initial structure of system is:\n" + model.toDot());
		// putIntoFile( model.toDot(), "structure");

		return model;
	}

	private static void Synthesize(String inPath, String outPath)
			throws IOException, NoStateExistException {

		ArrayList<LTS> ltslist = buildComponentModel(inPath, outPath);
		Graph model = getStructure();
		gLTS plan = gLTS.parsegLTS("plan", inPath, "plan", "pa", model);
		out.println("The reconfiguration plan:\n" + plan.draw(0, 75));

		gLTS gts = new gLTS();
		gts = gts.applyReconfiguration(ltslist, plan, false);
		gts.removeStateSplitter();
		// putIntoFile(gts.draw(0,75), outPath, "model");

		putIntoFile(gts.printEachConfigAdaptor(plan, "e1", 100), outPath,
				"model_config");

		// GraphAutomata spec = GraphAutomata.parseGraphAutomata("spec", "s0",
		// "s0",inPath);

		// this the example with no deadlock
		GraphAutomata spec = GraphAutomata.parseGraphAutomata("spec", "s0",
				"s0", inPath);

		List<String> uncontrolledEvents = new ArrayList<String>();
		//uncontrolledEvents.add("X.a()");
		uncontrolledEvents.add("X.e()");

		Synthesizer synth = new Synthesizer();
		gLTS adaptor = synth.synthesize(gts, spec, plan, uncontrolledEvents,
				false, outPath);

		adaptor = synth.removeCycles(adaptor, outPath);
		putIntoFile(adaptor.printEachConfigAdaptor(plan, "e1", 100), outPath,
				"adaptor_config");
		// putIntoFile(adaptor.draw(0,75), outPath, "adaptor");
		// printEachConfigAdaptor(adaptor, plan, outPath, "adaptor");
	}

	private static void printEachConfigAdaptor(gLTS adaptor, gLTS plan,
			String path, String fileName) {
		String output = "";
		for (gState s : plan.states.values()) {
			gLTS configAdaptor = adaptor.getGLTSofConfig(s.s.ID, "");
			out.println("The configuration controller in " + s.s.ID + ":"
					+ Integer.toString(configAdaptor.states.size()));
			// putIntoFile(configAdaptor.draw(0,75), path, fileName + s.s.ID);
			output += configAdaptor.draw(0, 75);
		}
		putIntoFile(output, path, fileName + "o");

	}

	public static void main(String[] args) throws IOException,
			NoStateExistException {
		String outPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/IO/outputPaperExample/";
		String inPath = "/Volumes/Miscellaneous/Eclipse Workspace/ControllerSyn/IO/inputPaperExample/";
		Synthesize(inPath, outPath);
	}

}