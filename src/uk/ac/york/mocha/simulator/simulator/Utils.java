package uk.ac.york.mocha.simulator.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import uk.ac.york.mocha.simulator.dag.DirectedAcyclicGraph;

public class Utils {

	public static DirectedAcyclicGraph getDagByIndex(List<DirectedAcyclicGraph> dags, int id, int instanceID) {
		for (DirectedAcyclicGraph dag : dags)
			if (dag.id == id && dag.instanceNo == instanceID)
				return dag;

		return null;
	}

	public static void writeResults(String content, String filename) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File("result/" + filename), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writer.println(content);
		writer.close();
	}
}
