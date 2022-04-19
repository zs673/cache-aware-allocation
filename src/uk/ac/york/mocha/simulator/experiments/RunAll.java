package uk.ac.york.mocha.simulator.experiments;

import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.DagType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.ETDistruction;

public class RunAll {

	public static void main(String args[]) {
		SystemParameters.dagType = DagType.Random;

		SystemParameters.utilPerTask = 2.0;
		SystemParameters.useWCET = true;

		EP1_2.run();
		EP3_4.run();

		SystemParameters.utilPerTask = 2.0;
		SystemParameters.useWCET = false;
		SystemParameters.etType = ETDistruction.uniform;

		EP1_2.run();
		EP3_4.run();

		SystemParameters.utilPerTask = 2.0;
		SystemParameters.useWCET = false;
		SystemParameters.etType = ETDistruction.normal;

		EP1_2.run();
		EP3_4.run();

//		EP5_1.run();
//		EP5_2.run();
	}
}
