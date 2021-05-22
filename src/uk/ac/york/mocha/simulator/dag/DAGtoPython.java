package uk.ac.york.mocha.simulator.dag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.york.mocha.simulator.generator.SystemGenerator;

public class DAGtoPython {

	public static void main(String args[]) {
		SystemGenerator gen = new SystemGenerator(8, 1, true, true, null, 1000, true, true);
		List<DirectedAcyclicGraph> dags = gen.generatedDAGInstancesInOneHP(1, -1, null, true);

		DirectedAcyclicGraph dag = dags.get(0);

		for (int i = 0; i < dag.getFlatNodes().size(); i++) {
			dag.getFlatNodes().get(i).priority = dag.getFlatNodes().size() - i;
		}

		for (int i = 0; i < 10000; i++) {
			pharseDAGForPython(dag, 2);
		}
	}

	public static long pharseDAGForPython(DirectedAcyclicGraph dag, int coreNum) {

		List<Node> nodes = dag.getFlatNodes();

		nodes.sort((c1, c2) -> Integer.compare(c1.getId(), c2.getId()));

		List<NodeByTemplate> nodesByTemplate = nodes.stream().map(c -> nodeTemplate(c)).collect(Collectors.toList());

		String G = "{";
		String C = "{";
		String P = "{";

		for (int i = 0; i < nodesByTemplate.size(); i++) {
			String childrenS = nodesByTemplate.get(i).childrenS;
			String wcetS = nodesByTemplate.get(i).wcetS;
			String priorityS = nodesByTemplate.get(i).priorityS;

			G += childrenS;
			C += wcetS;
			P += priorityS;

			if (i != nodesByTemplate.size() - 1) {
				G += ",";
				C += ",";
				P += ",";
			}
		}

		G += "}";
		C += "}";
		P += "}";

		String core = coreNum + "";

		System.out.println(G);
		System.out.println(C);
		System.out.println(P);
		System.out.println(core);

		long makespan = -1;

		try {
			Process process = Runtime.getRuntime().exec("python3 rta.py " + G + " " + C + " " + P + " " + core);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// Read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			String s = null;
			String makespanS = null;
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
				makespanS = s + "";
			}

			makespan = Long.parseLong(makespanS);

			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			String error = null;
			while ((error = stdError.readLine()) != null) {
				System.out.println(error);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (makespan <= 0) {
			System.err.println("DAGtoPython.pharseDAGForPython(): makespan <= 0.");
			System.exit(-1);
		}

		return makespan;
	}

	private static NodeByTemplate nodeTemplate(Node n) {

		int id = n.getId() + 1;

		List<Integer> children = n.getChildren().stream().map(c -> c.getId()).collect(Collectors.toList());

		String childrenS = id + ":" + "[";

		for (int i = 0; i < children.size(); i++) {
			childrenS += children.get(i) + 1;

			if (i != children.size() - 1)
				childrenS += ",";
		}
		childrenS += "]";

		String wcetS = id + ":" + n.getWCET();

		String priorityS = id + ":" + n.priority;

		NodeByTemplate nodeTemp = new NodeByTemplate(childrenS, wcetS, priorityS);

		return nodeTemp;
	}
}

class NodeByTemplate {
	String childrenS;
	String wcetS;
	String priorityS;

	public NodeByTemplate(String childrenS, String wcetS, String priorityS) {
		this.childrenS = childrenS;
		this.wcetS = wcetS;
		this.priorityS = priorityS;
	}
}
