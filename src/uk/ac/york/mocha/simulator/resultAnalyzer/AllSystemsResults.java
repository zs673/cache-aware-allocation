package uk.ac.york.mocha.simulator.resultAnalyzer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.util.Pair;

import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class AllSystemsResults {
	
	public enum ResultType {
		makespan, util, finish, makespan_abs, SLR, speedup
	}

	int taskNum;
	int systemNum;

	int[] instanceNo;

	List<String> data = new ArrayList<>();
	List<String> compareData = new ArrayList<>();

	List<String> dataAna = new ArrayList<>();
	List<String> compareDataAna = new ArrayList<>();

	String cachePerformance = "";

	String folder;

	DecimalFormat df = new DecimalFormat("#.###");

	String recencyName;

	String rate = "";
	String effect = "";

	public AllSystemsResults(List<OneSystemResults> resPerSystem, int[] instanceNo, int systemNum, int taskNum,
			SystemParameters.ExpName name, boolean append, double rate, double effect) {

		DecimalFormat df_temp = new DecimalFormat("#.#");
		this.rate = df_temp.format(rate) + "";
		this.effect = df_temp.format(effect) + "";

		this.taskNum = taskNum;
		this.systemNum = systemNum;
		this.instanceNo = instanceNo;
		this.recencyName = "";

		if (this.rate.equals(""))
			folder = "result/" + name + "/";
		else
			folder = "result/" + name + "_" + this.rate + "_" + this.effect + "/";

		File theDir = new File(folder);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		writeInstanceNum(append);
		writeResults(resPerSystem, append);
		writeCachePerf(resPerSystem, append);
		writeTaskParam(resPerSystem, append);

	}
	//systemNum -> cores
	public AllSystemsResults(List<OneSystemResults> resPerSystem, int[] instanceNo, int systemNum, int taskNum,
			SystemParameters.ExpName name, boolean append) {
		this(resPerSystem, instanceNo, systemNum, taskNum, name, "", append);
	}

	public AllSystemsResults(List<OneSystemResults> resPerSystem, int[] instanceNo, int systemNum, int taskNum,
			SystemParameters.ExpName name) {
		this(resPerSystem, instanceNo, systemNum, taskNum, name, "", false);
	}

	public AllSystemsResults(List<OneSystemResults> resPerSystem, int[] instanceNo, int systemNum, int taskNum,
			SystemParameters.ExpName name, String recencyName) {
		this(resPerSystem, instanceNo, systemNum, taskNum, name, recencyName, false);
	}

	public AllSystemsResults(List<OneSystemResults> resPerSystem, int[] instanceNo, int systemNum, int taskNum,
			SystemParameters.ExpName name, String recencyName, boolean append) {
		this.taskNum = taskNum;
		this.systemNum = systemNum;
		this.instanceNo = instanceNo;
		this.recencyName = recencyName;

		if (rate.equals(""))
			folder = "result/" + name + "/";
		else
			folder = "result/" + name + "_" + rate + "_" + effect + "/";
		File theDir = new File(folder);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		writeInstanceNum(append);
		writeResults(resPerSystem, append);
		writeCachePerf(resPerSystem, append);
		writeTaskParam(resPerSystem, append);
	}

	public void writeTaskParam(List<OneSystemResults> resPerSystem, boolean append) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < resPerSystem.size(); i++) {
			List<DirectedAcyclicGraph> dags = resPerSystem.get(i).allMethods.get(0);

			String dagsInfo = "";
			for (int j = 0; j < 1; j++) {
				String dagInfo = dags.get(j).getSchedParameters().getWCET() + ","
						+ dags.get(j).getSchedParameters().getPeriod() + ","
						+ df.format(dags.get(j).getSchedParameters().getUtil()) + "\n";
				dagsInfo += dagInfo;
				// if (j < dags.size() - 1)
				// dagsInfo += dagInfo + ",";
				// else
				// dagsInfo += dagInfo + "\n";
				// System.out.println(dags.get(j).getSchedParameters().getWCET()
				// );
			}
			builder.append(dagsInfo);
		}

		String fileName = "taskparam" + "_" + taskNum + "_" + SystemParameters.utilPerTask + recencyName + ".txt";
		Utils.writeResult(folder + fileName, builder.toString(), append);
	}

	public void writeCachePerf(List<OneSystemResults> resPerSystem, boolean append) {

		StringBuilder builder = new StringBuilder();
		double[][] avg = new double[resPerSystem.get(0).cachePerf.size()][5];
		for (int i = 0; i < resPerSystem.get(0).cachePerf.size(); i++) {
			// long[] avg = new long[resPerSystem.get(0).cachePerf.get(0).length];
			//double[] avg = new double[5];
			final int methodIndex = i;

			for (int j = 0; j < resPerSystem.size(); j++) {

				double[] cacheOneMethod = resPerSystem.get(j).cachePerf.get(methodIndex);

				for (int k = 0; k < cacheOneMethod.length; k++) {
					builder.append(cacheOneMethod[k] + ",");
					avg[i][k] += cacheOneMethod[k];
				}
				builder.append("\n");
			}
			for (int m = 0; m < 5; m++){
				avg[i][m] = (double)avg[i][m] / resPerSystem.size();
			}
			builder.append("\n");
			builder.append("\n");
		}
		
		for (int i = 0; i < resPerSystem.get(0).cachePerf.size(); i++) {
			for (int m = 0; m < 5; m++){
				builder.append(df.format(avg[i][m]) + ",");
			}
			builder.append("\n");
		}

		String cacheFileName = "cache" + "_" + taskNum + "_" + SystemParameters.utilPerTask + recencyName + ".txt";
		Utils.writeResult(folder + cacheFileName, builder.toString(), append);
	}

	public void writeResults(List<OneSystemResults> resPerSystem, boolean append) {
		for (int i = 0; i < ResultType.values().length; i++) {
			Pair<String, String> dataPair = dataPerSysToAllSys(resPerSystem, i, false);//源数据和分析的数据
			data.add(dataPair.getFirst());
			dataAna.add(dataPair.getSecond());

			Pair<String, String> dataComparePair = dataPerSysToAllSys(resPerSystem, i, true);
			compareData.add(dataComparePair.getFirst());
			compareDataAna.add(dataComparePair.getSecond());
		}

		for (int i = 0; i < ResultType.values().length; i++) {//A是analyse的结果
			String dataFileName = ResultType.values()[i].name() + "_" + taskNum + "_" + SystemParameters.utilPerTask
					+ recencyName + ".txt";
			Utils.writeResult(folder + dataFileName, data.get(i).toString(), append);

			String compareFileName = ResultType.values()[i].name() + "_compare" + "_" + taskNum + "_"
					+ SystemParameters.utilPerTask
					+ recencyName + ".txt";
			Utils.writeResult(folder + compareFileName, compareData.get(i).toString(), append);

			String dataAnaFileName = "A_" + ResultType.values()[i].name() + "_" + taskNum + "_"
					+ SystemParameters.utilPerTask + recencyName
					+ ".txt";
			Utils.writeResult(folder + dataAnaFileName, dataAna.get(i).toString(), append);

			String compareDataAnaFileName = "A_" + ResultType.values()[i].name() + "_compare" + "_" + taskNum + "_"
					+ SystemParameters.utilPerTask + recencyName + ".txt";
			Utils.writeResult(folder + compareDataAnaFileName, compareDataAna.get(i).toString(), append);
		}
	}

	public Pair<String, String> dataPerSysToAllSys(List<OneSystemResults> resPerSystem, int metricIndex,
			boolean compare) {

		List<List<String>> temp = new ArrayList<>();//第一维度表示nos，第二维度表示方法，string表示多个dag的表现
		for (int i = 0; i < resPerSystem.size(); i++) {

			String[] oneMetric = null;

			if (!compare)
				oneMetric = resPerSystem.get(i).resultsToString.get(metricIndex).split("\n");
			else
				oneMetric = resPerSystem.get(i).resultsCompareToString.get(metricIndex).split("\n");

			List<String> d = new ArrayList<>();
			for (int j = 0; j < oneMetric.length; j++)
				d.add(oneMetric[j]);

			temp.add(d);
		}

		StringBuilder resByMetric = new StringBuilder();
		StringBuilder resAnalyse = new StringBuilder();

		for (int i = 0; i < temp.get(0).size(); i++) {//把每个方法的多次nos数据集中起来
			final int index = i;

			temp.stream().forEach(c1 -> {
				resByMetric.append(c1.get(index) + "\n");
			});

			resByMetric.append("\n");
		}
		//resByMetric string method * nos(实验次数) * instanceNum（dagNum）
		resAnalyse.append("\n\nData analysis for each instance \n");
		resAnalyse.append("AVG,MED,MAX,MIN\n");

		List<List<List<Double>>> analysedDataEachMethod = new ArrayList<>();

		for (int k = 0; k < temp.get(0).size(); k++) {//取单个method
			List<List<Double>> summaryAll = new ArrayList<>();//单个method nos * 4个指标（among instance）
			final int index = k;

			temp.stream().forEach(s -> {

				String c = s.get(index);
				String[] cs = c.split(",");

				List<Double> v = new ArrayList<>();
				for (int i = 0; i < cs.length; i++) {
					try {
						double d = Double.parseDouble(cs[i]);
						v.add(d);
					} catch (NullPointerException e) {
					} catch (NumberFormatException e) {
					}
				}

				double avg = v.stream().mapToDouble(c1 -> c1).sum() / (double) v.size();
				double max = v.stream().mapToDouble(c1 -> c1).max().getAsDouble();
				double min = v.stream().mapToDouble(c1 -> c1).min().getAsDouble();

				Median median = new Median();
				double[] v_d = new double[v.size()];
				for (int i = 0; i < v.size(); i++) {
					v_d[i] = v.get(i);
				}
				double med = median.evaluate(v_d);

				List<Double> summary = new ArrayList<>();
				// summary.add(avg);//单次运行所有instance/dag的平均值，如果instanceNum = 1 and taskNum = 1，就只是一个值的avg
				// summary.add(med);
				// summary.add(max);
				// summary.add(min);
				for (int i = 0; i < v.size(); i++){
					summary.add(v.get(i));
					if (i < v.size() - 1){
						resAnalyse.append(df.format(v.get(i)) + ",");
					}else{
						resAnalyse.append(df.format(v.get(i)) + ",\n");
					}
					
				}
				summaryAll.add(summary);


				// resAnalyse.append(df.format(avg) + ",");
				// resAnalyse.append(df.format(med) + ",");
				// resAnalyse.append(df.format(max) + ",");
				// resAnalyse.append(df.format(min) + ",\n");

			});

			resAnalyse.append("\n\n");//不同method之间用\n\n分割
			analysedDataEachMethod.add(summaryAll); //多个method nos * 4个指标（among instance）维度和resAnalyse一样
		}

		resAnalyse.append("\n\nFurther Data analysis of all test cases \n");
		resAnalyse.append("INSTANCEID avg med max min \n");

		for (int k = 0; k < analysedDataEachMethod.size(); k++) {//对每个method来说
			List<List<Double>> summaryAll = analysedDataEachMethod.get(k); //nos * 4
			List<List<Double>> summartAllHtoV = new ArrayList<>(); //4 * nos
			//上面两个数组维度互换
			for (int j = 0; j < summaryAll.get(0).size(); j++) {
				List<Double> summary = new ArrayList<>();
				for (int i = 0; i < summaryAll.size(); i++) {
					summary.add(summaryAll.get(i).get(j));
				}
				summartAllHtoV.add(summary);
			}

			summartAllHtoV.forEach(v -> {

				Median median = new Median();
				double[] v_d = new double[v.size()];
				for (int i = 0; i < v.size(); i++) {
					v_d[i] = v.get(i);
				}

				double avg = v.stream().mapToDouble(c1 -> c1).sum() / (double) v.size();
				double med = median.evaluate(v_d);
				double max = v.stream().mapToDouble(c1 -> c1).max().getAsDouble();
				double min = v.stream().mapToDouble(c1 -> c1).min().getAsDouble();

				int count = summartAllHtoV.indexOf(v);//如果不cache-aware，每次instance的结果都一样，所以index不准确
				count += 1;
				// switch (count) {
				// 	case 0:
				// 		resAnalyse.append("AVGs,");
				// 		break;
				// 	case 1:
				// 		resAnalyse.append("MEDs,");
				// 		break;
				// 	case 2:
				// 		resAnalyse.append("MAXs,");
				// 		break;
				// 	case 3:
				// 		resAnalyse.append("MINs,");
				// 		break;
				// 	default:
				// 		break;
				// }
				resAnalyse.append("INSTANCE" + count + " ");
				resAnalyse.append(df.format(avg) + ", ");
				resAnalyse.append(df.format(med) + ", ");
				resAnalyse.append(df.format(max) + ", ");
				resAnalyse.append(df.format(min) + ", \n");

			});
			resAnalyse.append("\n\n"); //最大 最小 中位数 平均等四个指标的15次实验的最大最小中位平均
		}

		return new Pair<String, String>(resByMetric.toString(), resAnalyse.toString());
	}

	public void writeInstanceNum(boolean append) {
		String instanceNumString = "";
		for (int i = 0; i < instanceNo.length; i++) {
			if (i != instanceNo.length - 1)
				instanceNumString += instanceNo[i] + ",";
			else
				instanceNumString += instanceNo[i] + "\n";
		}

		Utils.writeResult(folder + "instanceNum_" + taskNum + "_" + SystemParameters.utilPerTask + recencyName + ".txt",
				instanceNumString,
				append);
	}

}
