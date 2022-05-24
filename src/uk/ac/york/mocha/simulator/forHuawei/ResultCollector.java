package uk.ac.york.mocha.simulator.forHuawei;

import uk.ac.york.mocha.simulator.simulator.Utils;

public class ResultCollector {

	public static void writeSchedToSystem(ResultCap cap, int variable, String expName, String folder) {
	
		String file = "";
		String out = "";

		
		file = expName + "_" + variable + "_" + "sched" + ".txt";

		out += cap.NoSched_our + ",";
		out += cap.NoSched_he + ",";
		out += cap.NoSched_seq + "\n";

		Utils.writeResult(folder, file, out);
	}

}
