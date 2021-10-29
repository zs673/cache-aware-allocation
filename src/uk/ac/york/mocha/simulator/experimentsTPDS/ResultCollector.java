package uk.ac.york.mocha.simulator.experimentsTPDS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.simulator.Utils;

public class ResultCollector {

	public static void writeSchedToSystem(ResultCap cap, int variable, int mode, String expName, String folder) {
		String our_file = "";
		String he_file = "";
		String seq_file = "";

		String our_out = "";
		String he_out = "";
		String seq_out = "";

		String file = "";
		String out = "";

		switch (mode) {
		case 0: // intra-task interference
			our_file = expName + "_" + variable + "_" + "intra" + "_" + "our" + ".txt";
			he_file = expName + "_" + variable + "_" + "intra" + "_" + "he" + ".txt";
			seq_file = expName + "_" + variable + "_" + "intra" + "_" + "seq" + ".txt";

			List<List<long[]>> intra = cap.intra_delay;
			for (List<long[]> i : intra) {
				long[] our = i.get(0);
				for (int k = 0; k < our.length; k++) {
					if (k < our.length - 1)
						our_out += our[k] + ",";
					else
						our_out += our[k];
				}
//				our_out += "\n";

				long[] he = i.get(1);
				for (int k = 0; k < he.length; k++) {
					if (k < he.length - 1)
						he_out += he[k] + ",";
					else
						he_out += he[k];
				}
//				he_out += "\n";

				long[] seq = i.get(2);
				for (int k = 0; k < seq.length; k++) {
					if (k < seq.length - 1)
						seq_out += seq[k] + ",";
					else
						seq_out += seq[k];
				}
//				seq_out += "\n";
			}

			Utils.writeResult(folder, our_file, our_out);
			Utils.writeResult(folder, he_file, he_out);
			Utils.writeResult(folder, seq_file, seq_out);

			break;
		case 1: // inter-task interference
			our_file = expName + "_" + variable + "_" + "inter" + "_" + "our" + ".txt";
			he_file = expName + "_" + variable + "_" + "inter" + "_" + "he" + ".txt";
			seq_file = expName + "_" + variable + "_" + "inter" + "_" + "seq" + ".txt";

			List<List<long[]>> inter = cap.inter_delay;
			for (List<long[]> i : inter) {
				long[] our = i.get(0);
				for (int k = 0; k < our.length; k++) {
					if (k < our.length - 1)
						our_out += our[k] + ",";
					else
						our_out += our[k];
				}
//				our_out += "\n";

				long[] he = i.get(1);
				for (int k = 0; k < he.length; k++) {
					if (k < he.length - 1)
						he_out += he[k] + ",";
					else
						he_out += he[k];
				}
//				he_out += "\n";

				long[] seq = i.get(2);
				for (int k = 0; k < seq.length; k++) {
					if (k < seq.length - 1)
						seq_out += seq[k] + ",";
					else
						seq_out += seq[k];
				}
//				seq_out += "\n";
			}

			Utils.writeResult(folder, our_file, our_out);
			Utils.writeResult(folder, he_file, he_out);
			Utils.writeResult(folder, seq_file, seq_out);

			break;
		case 2: // response time
			our_file = expName + "_" + variable + "_" + "response" + "_" + "our" + ".txt";
			he_file = expName + "_" + variable + "_" + "response" + "_" + "he" + ".txt";
			seq_file = expName + "_" + variable + "_" + "response" + "_" + "seq" + ".txt";

			List<List<long[]>> response = cap.response_time;
			for (List<long[]> i : response) {
				long[] our = i.get(0);
				for (int k = 0; k < our.length; k++) {
					if (k < our.length - 1)
						our_out += our[k] + ",";
					else
						our_out += our[k];
				}
//				our_out += "\n";

				long[] he = i.get(1);
				for (int k = 0; k < he.length; k++) {
					if (k < he.length - 1)
						he_out += he[k] + ",";
					else
						he_out += he[k];
				}
//				he_out += "\n";

				long[] seq = i.get(2);
				for (int k = 0; k < seq.length; k++) {
					if (k < seq.length - 1)
						seq_out += seq[k] + ",";
					else
						seq_out += seq[k];
				}
//				seq_out += "\n";
			}

			Utils.writeResult(folder, our_file, our_out);
			Utils.writeResult(folder, he_file, he_out);
			Utils.writeResult(folder, seq_file, seq_out);

			break;
		case 3: // sched info
			file = expName + "_" + variable + "_" + "sched" + ".txt";

			out += cap.NoSched_our + ",";
			out += cap.NoSched_he + ",";
			out += cap.NoSched_seq;

			Utils.writeResult(folder, file, out);

			break;
		}
	}

	/**
	 * The following is not in use.
	 */

	public static String path = "result_multi";
	public static String path_d = "result_multi_daivd";
	public static String path_out = "result_multi_out";

	public static String[] preFixs = { "util", "nop", "nopF", "paral" };
	public static String[] types = { "inter", "intra", "response", "sched" };
	public static String[] sufFixs = { "he", "our", "seq" };

	public static void main(String args[]) {
		List<String> allFiles = getAllFileNames(path);

		for (String file : allFiles) {
			System.out.println(file);
		}

		readOneExp(preFixs[0], 1, 20, path, allFiles);

	}

	public static void readOneExp(String preFix, int startingVar, int endingVar, String path, List<String> allFiles) {
		List<String> files_he = new ArrayList<>();
		List<String> files_our = new ArrayList<>();
		List<String> files_seq = new ArrayList<>();

		for (String name : allFiles) {
			if (name.contains(preFix) && name.contains(sufFixs[0]))
				files_he.add(name);
			if (name.contains(preFix) && name.contains(sufFixs[1]))
				files_our.add(name);
			if (name.contains(preFix) && name.contains(sufFixs[2]))
				files_seq.add(name);
		}

		for (String type : types) {
			readOneExpOneType(files_he, preFix, type, "he", startingVar, endingVar, path);
		}

	}

	public static void readOneExpOneType(List<String> filesOneExp, String exp, String type, String method,
			int startingVar, int endingVar, String path) {
		List<String> files = new ArrayList<>();

		for (String file : filesOneExp) {
			if (file.contains(type)) {
				files.add(file);
			}
		}

		List<Integer> indexes = praseToInt(files);

		List<List<List<Long>>> allRes = new ArrayList<>();
//		String out = "";

		for (int i = startingVar; i <= endingVar; i++) {
			int index = indexes.indexOf(i);
			String filename = files.get(index);

			System.out.println("-----------------------------------------");
			System.out.println(i);
			Pair<List<List<Long>>, String> readRes = readFile(path, filename);

			List<List<Long>> res = readRes.getFirst();
			String readOut = readRes.getSecond();

			System.out.println(readOut);
			System.out.println("-----------------------------------------");

			allRes.add(res);

//			out+=readOut +"\n";

			Utils.writeResult(path_out, exp + "_" + type + "_" + method + "_" + i + ".txt", readOut);
		}

	}

	private static Pair<List<List<Long>>, String> readFile(String path, String file) {
		List<List<Long>> res = new ArrayList<>();

		String out = "";

		try {
			BufferedReader br = new BufferedReader(new FileReader(path + "/" + file));

			String line = br.readLine();

			while (line != null) {

				String[] re = line.split(" ");

				List<Long> r = new ArrayList<>();
				for (String s : re) {
					if (s.length() > 0) {
						r.add(Long.parseLong(s));
						out += s + ",";
					}
				}
				out += "\n";

				if (r.size() > 0)
					res.add(r);

				line = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Pair<>(res, out);
	}

	private static List<Integer> praseToInt(List<String> files) {
		List<Integer> nums = new ArrayList<>();

		for (String s : files) {
			String[] s_split = s.split("_");
			nums.add(Integer.parseInt(s_split[1]));
		}

		return nums;
	}

	public static List<String> getAllFileNames(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		List<String> files = new ArrayList<>();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
//				System.out.println("File " + listOfFiles[i].getName());

				String fileName = listOfFiles[i].getName();

				boolean isResult = false;
				for (String pre : preFixs) {
					if (fileName.contains(pre)) {
						isResult = true;
						break;
					}
				}

				if (isResult)
					files.add(listOfFiles[i].getName());
			}
		}

		return files;
	}

}
