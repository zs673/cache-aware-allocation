package uk.ac.york.mocha.simulator.dag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Recency {

	public static final double[] costFactorMIN = { 0.3, 0.4, 0.5 };
	public static final double[] costFactorMAX = { 0.5, 0.6, 0.7 };

	public static final int cacheLevel = 3;
	public static final int[] recencyDepth = {4,10,20};
	
	private static final DecimalFormat df = new DecimalFormat("#.##");
	
	private Random rng;

	/**********************************************************************
	 * A cache-aware recency table that describes the real WCET of a node *
	 **********************************************************************/
	public List<List<Double>> recencyTable;

	public Recency(int seed) {
		rng = new Random(seed);

		recencyTable = new ArrayList<>(); // new double[cacheLevel][recencyDepth1];

		for (int i = 0; i < cacheLevel; i++) {
			List<Double> oneLevel = new ArrayList<>();
			for (int j = 0; j < recencyDepth[i]; j++) {

				if (j == 0) {
					double factor = rng.nextDouble() * (costFactorMAX[i] - costFactorMIN[i]) + costFactorMIN[i];
					double formatFactor = Double.parseDouble(df.format(factor));
					oneLevel.add(formatFactor);
				} else {
					double factor = -1;
					switch (i) {
					case 0:
						factor = oneLevel.get(j-1) + 0.1;
						break;
					case 1:
						factor = oneLevel.get(j-1) + 0.05;
						break;
					case 2:
						factor = oneLevel.get(j-1) + 0.01;
						break;

					default:
						break;
					}
					
					factor = factor > 1.0 ? 1.0 : factor;
					double formatFactor = Double.parseDouble(df.format(factor));
					oneLevel.add(formatFactor);
				}
			}
			recencyTable.add(oneLevel);
		}

		recencyTable.get(cacheLevel -1 ).set(recencyDepth[2]-1, 1.0);
	}

	public static void main(String args[]) {

		Recency table = new Recency(1000);

		for (List<Double> d: table.recencyTable)
			System.out.println(Arrays.toString(d.toArray()));

	}
}
