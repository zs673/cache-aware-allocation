package uk.ac.york.mocha.simulator.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* A. Burns and R. I. Davis. Improved priority assignment for global Fixed priority pre-emptive 
 * scheduling in multiprocessor real-time systems. Real-Time Systems, 47(1):1{40, 2010.
*/

public class UUnifastDiscard {

	private double uUtil;
	private int uNum;
	private ArrayList<Double> uUs;
	private boolean shallDiscard;
	private int discardNum;
	private Random r;
	private int cores;

	private boolean takeAllUtil;

	public UUnifastDiscard(double util, int num, int discard, int cores, boolean takeAllUtil, Random ran) {
		this.uUtil = util;
		this.uNum = num;
		this.uUs = new ArrayList<Double>();
		this.shallDiscard = false;
		this.discardNum = discard;
		this.cores = cores;
		this.r = ran;
		this.takeAllUtil = takeAllUtil;
	}

	public void setUtil(double x) {
		this.uUtil = x;
	}

	public void setNum(int x) {
		this.uNum = x;
	}

	public double getUtil() {
		return this.uUtil;
	}

	public int getNum() {
		return this.uNum;
	}

	public ArrayList<Double> getUtils() {
		if (uUifastDiscard()) {
			if (!takeAllUtil) {
				if (uUs.size() != uNum + 1) {
					System.err.println("UUnifastDiscard.getUtils(): the number of utilisations generated is wrong!");
					System.exit(-1);
				}

				uUs.remove(uUs.size() - 1);
			}
			return uUs;
		} else
			return null;
	}

	private boolean uUnifast() {
		uUs.clear();
		double sumU = this.uUtil;
		double nextSum = 0;
		double temp = 0;
		this.shallDiscard = false;
		for (int i = takeAllUtil ? 1 : 0; i < this.uNum; i++) {

			nextSum = sumU * Math.pow(r.nextDouble(), (1.0 / (this.uNum - i)));
			temp = sumU - nextSum;
			if (temp > cores) {
				this.shallDiscard = true;
				break;
			}
			this.uUs.add(temp);
			sumU = nextSum;
		}
		if (!shallDiscard) {
			if (sumU <= cores)
				uUs.add(sumU);
			else
				shallDiscard = true;
		}
		return this.shallDiscard;

	}

	private boolean uUifastDiscard() {
		boolean isComplete = false;
		for (int i = 0; i < this.discardNum; i++) {
			if (!this.uUnifast()) {
				isComplete = true;
				break;
			} else {
				this.uUs.clear();
			}
		}
		return isComplete;
	}
	
	public static void main(String args[]) {
		int nos = 6000;
		UUnifastDiscard uu1 = new UUnifastDiscard(3.2, 1, 1000, 16, false, new Random(1000));

		List<Double> utils1 = new ArrayList<>();
		while(utils1.size() < nos) {
			double u = uu1.getUtils().get(0);
			// List<Double> us = new ArrayList<>();
			// us.add(u);
		    if (u * 144 <= 250.56)
				utils1.add(u);
		}

		UUnifastDiscard uu2 = new UUnifastDiscard(3.2, 1, 1000, 8, false, new Random(1000));

		List<Double> utils2 = new ArrayList<>();
		while(utils2.size() < nos) {
			double u = uu2.getUtils().get(0);
			// List<Double> us = new ArrayList<>();
			// us.add(u);
		    if (u * 144 <= 250.56)
				utils2.add(u);
		}

		boolean set = true;
		for (int i = 0; i < nos; i++){
			if (!utils1.get(i).equals(utils2.get(i))){
				System.out.println("Wrong Generation");
				set = false;
			}
		}
		if (set){
			System.out.println("Congradulations");
		}
	}

}