package uk.ac.york.mocha.simulator.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DavidUtilisationGenerator {

	public static void main(String args[]) {
		int num = 100;
		int sum = 1;

		List<Double> utils = getDavidUtilVector(num, sum);

		for (Double d : utils) {
			System.out.println(d);
		}
	}

	public static List<Double> getDavidUtilVector(int num, double sum) {

		List<Double> utils = new ArrayList<>();
		try {
			/*
			 * The final parameter is override priority passed to Python indicating the
			 * analysis will use the priority passed from Java. 0 - use RTSS priority with
			 * CPC model. 1 - use priority passed from java space.
			 */
			Process process = Runtime.getRuntime().exec("python3 runner.py " + num + " " + sum);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// Read the output from the command
//			System.out.println("Here is the standard output of the command:\n");
			String out = null;
			String resRaw = "";
			while ((out = stdInput.readLine()) != null) {
//				System.out.println(out);
				resRaw += out + "";
			}

			// Read any errors from the attempted command
			// System.out.println("Here is the standard error of the command (if any):\n");
			String error = null;
			while ((error = stdError.readLine()) != null) {
				System.out.println(error);
			}

			String resPre = resRaw.replaceAll("\\[", "").replaceAll("\\]", "").trim();
			String[] resString = resPre.split(",");

			for (String s : resString) {
				Double d = Double.parseDouble(s);
				utils.add(d);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return utils;
	}
}
