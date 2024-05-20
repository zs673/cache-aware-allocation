package uk.ac.york.mocha.simulator.simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.python.indexer.Util;

import uk.ac.york.mocha.simulator.entity.DAGtoPython;
import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.entity.Node;
import uk.ac.york.mocha.simulator.entity.RecencyProfileReal;
import uk.ac.york.mocha.simulator.generator.CacheHierarchy;
import uk.ac.york.mocha.simulator.generator.HitCore;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.parameters.SystemParameters.RecencyType;

public class Utils {

	public static List<long[]> getETHistroy(Node n, List<Node> hist) {
		List<long[]> etHist = new ArrayList<>();
		List<Node> sameNodes = new ArrayList<>();

		for (Node ln : hist) {
			if (ln.getDagID() == n.getDagID() && ln.getId() == n.getId())
				sameNodes.add(ln);
		}

		sameNodes.sort((c1, c2) -> -Integer.compare(c1.getDagInstNo(), c2.getDagInstNo()));

		for (int i = 0; i < SystemParameters.etHist_length; i++) {
			if (sameNodes.size() == i)
				break;

			long[] res = new long[2];
			res[0] = sameNodes.get(i).expectedET;
			res[1] = sameNodes.get(i).expectedCache;
			etHist.add(res);
		}

		return etHist;
	}

	public static List<List<Node>> getAllocHistoryByLevel2Cache(List<List<Node>> allocHis) {

		List<List<Node>> level2 = new ArrayList<>();
		for (int i = 0; i < allocHis.size() / SystemParameters.Level2CoreNum; i++) {
			level2.add(new ArrayList<>());
		}

		for (int i = 0; i < allocHis.size(); i++) {
			int cluster = i / SystemParameters.Level2CoreNum;

			level2.get(cluster).addAll(allocHis.get(i));
		}

		return level2;
	}

	public static void assignPriorityOur(List<DirectedAcyclicGraph> dags) {
		for (DirectedAcyclicGraph dag : dags) {
			Pair<Long, List<int[]>> res = null;

			dag.hard = true;

			res = DAGtoPython.pharseDAGForPython(dag, 8);
			List<int[]> prio = res.getSecond();

			for (Node n : dag.getFlatNodes()) {
				int id = n.getId();

				for (int i = 0; i < prio.size(); i++) {
					if (prio.get(i)[0] - 1 == id) {
						n.priority = prio.get(i)[1];
						break;
					}
				}
			}
		}
	}

	/*
	 * Order nodes by 1) its DAG priority and 2) DAG instance number.
	 */
	public static int compareDAG(List<DirectedAcyclicGraph> dags, int id1, int inst1, int id2, int inst2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, id1, inst1);
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, id2, inst2);

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			return Long.compare(inst1, inst2);
		}
	}

	public static int compareNodeByPriorityAndSensitivity(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {

			int ins = Long.compare(c1.getDagInstNo(), c2.getDagInstNo());

			if (ins != 0)
				return ins;
			else
				return compareNodeBySensitivity(c1, c2);
		}
	}

	private static int compareNodeBySensitivity(Node c1, Node c2) {

		if (c1.sensitivity > c2.sensitivity)
			return -1;
		else if (c1.sensitivity < c2.sensitivity)
			return 1;
		else {
			return -Long.compare(c1.getWCET(), c2.getWCET());
		}

	}

	/*
	 * Order nodes by 1) its DAG priority and 2) its WCET.
	 */
	public static int compareNode(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {

			int ins = Long.compare(c1.getDagInstNo(), c2.getDagInstNo());

			if (ins != 0)
				return ins;
			else
				return -Long.compare(c1.getWCET(), c2.getWCET());

			// int c = -Long.compare(c1.getWCET(), c2.getWCET());
			//
			// if (c != 0)
			// return c;
			// else {
			// return Integer.compare(c1.getDagInstNo(), c2.getDagInstNo());
			// }

		}

	}

	public static int compareNodeForYHX(List<DirectedAcyclicGraph> dags, Node c1, Node c2, Map<Node, HitCore> hitCore) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {

			int ins = Long.compare(c1.getDagInstNo(), c2.getDagInstNo());

			if (ins != 0)
				return ins;
			else
				if (hitCore.get(c1).priority != hitCore.get(c2).priority){
					return -Double.compare(hitCore.get(c1).priority, hitCore.get(c2).priority);
				}
				return -Long.compare(c1.getWCET(), c2.getWCET());
		}
	}

	// public static int compareNodeForYHX1(List<DirectedAcyclicGraph> dags, Node c1, Node c2, Map<Node, HitCore> hitCore) {

	// 	DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
	// 	DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

	// 	if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
	// 		return -1;
	// 	} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
	// 		return 1;
	// 	} else {

	// 		int ins = Long.compare(c1.getDagInstNo(), c2.getDagInstNo());

	// 		if (ins != 0)
	// 			return ins;
	// 		else
	// 			if (hitCore.get(c1).priority1 != hitCore.get(c2).priority1){
	// 				return -Integer.compare(hitCore.get(c1).priority1, hitCore.get(c2).priority1);
	// 			}
	// 			return -Long.compare(c1.getWCET(), c2.getWCET());
	// 	}
	// }


	public static int compareNodeByID(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		return Integer.compare(c1.getId(), c2.getId());
	}

	public static int compareNodeWithPriority(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			int c = -1;

			if (dag1.id != dag2.id) {
				System.out.println(
						"Utils.compareNodeWithHard(): the IDs of DAG-1 and DAG-2 are not equal, but they have the same priority!");
				System.exit(-1);
			}

			c = Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());

			if (c != 0)
				return c;
			else {
				return -Long.compare(c1.priority, c2.priority);
			}

		}

	}

	public static int compareNodeWithHard(List<DirectedAcyclicGraph> dags, Node c1, Node c2) {

		DirectedAcyclicGraph dag1 = Utils.getDagByIndex(dags, c1.getDagID(), c1.getDagInstNo());
		DirectedAcyclicGraph dag2 = Utils.getDagByIndex(dags, c2.getDagID(), c2.getDagInstNo());

		if (dag1.getSchedParameters().getPriority() > dag2.getSchedParameters().getPriority()) {
			return -1;
		} else if (dag1.getSchedParameters().getPriority() < dag2.getSchedParameters().getPriority()) {
			return 1;
		} else {
			int c = -1;

			if (dag1.hard) {
				if (dag1.id != dag2.id) {
					System.out.println(
							"Utils.compareNodeWithHard(): the IDs of DAG-1 and DAG-2 are not equal, but there should be only one DAG in the system!");
					System.exit(-1);
				}

				c = Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());

				if (c != 0)
					return c;
				else {
					return -Long.compare(c1.priority, c2.priority);
				}

			} else {
				c = -Long.compare(c1.getWCET(), c2.getWCET());

				// if (c != 0)
				return c;
				// else {
				// return Integer.compare(c1.getDagInstNo(), c1.getDagInstNo());
				// }
			}

		}

	}

	/*
	 * Compute the hyperperiod of input DAGs. NOTE: The simulation covers a complete
	 * hyperperiod.
	 */
	public static long getHyperPeriod(List<Long> periods) {

		if (periods == null)
			return 0;

		List<Long> period_copy = new ArrayList<>(periods);
		long lcm = 1;
		int divisor = 2;

		while (true) {
			int counter = 0;
			boolean divisible = false;

			for (int i = 0; i < period_copy.size(); i++) {

				if (period_copy.get(i) == 1) {
					counter++;
				}

				if (period_copy.get(i) % divisor == 0) {
					divisible = true;
					period_copy.set(i, period_copy.get(i) / divisor);
				}
			}

			if (divisible) {
				lcm = lcm * divisor;
			} else {
				divisor++;
			}

			if (counter == period_copy.size()) {
				return lcm;
			}
		}
	}

	public static DirectedAcyclicGraph getDagByIndex(List<DirectedAcyclicGraph> dags, int id, int instanceID) {
		for (DirectedAcyclicGraph dag : dags)
			if (dag.id == id && dag.instanceNo == instanceID)
				return dag;

		return null;
	}

	public static List<DirectedAcyclicGraph> deepCopy(List<DirectedAcyclicGraph> dags) {

		List<DirectedAcyclicGraph> dp = new ArrayList<>();

		for (DirectedAcyclicGraph d : dags)
			dp.add(d.deepCopy());

		return dp;
	}

	public static void writeResult(String path, String file, String result) {
		writeResult(path, file, result, false);
	}

	public static void writeResult(String path, String file, String result, boolean append) {

		File theDir = new File(path);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(path + "/" + file), append));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writer.println(result);
		writer.close();
	}

	public static void writeResult(String filename, String result, boolean append) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(filename), append));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writer.println(result);
		writer.close();
	}

	public static void writeResult(String filename, String result) {
		writeResult(filename, result, false);
	}

	public static void writeUtils(String path, List<Double> utils, boolean append) {
		try{
			File file = new File(path);
			if(!file.isFile()){
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < utils.size(); i++) {
				bw.write(utils.get(i) + ",");
			}
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
//		List<RecencyProfileReal> crps = readJson("crp/profile.tacle.crp.json");

	}

	public static List<RecencyProfileReal> readJson(String file, List<String> taskNames, CacheHierarchy cache) {
		JSONParser parser = new JSONParser();

		List<RecencyProfileReal> crps = new ArrayList<>();

		try {
			Object obj = parser.parse(new FileReader(file));
			JSONObject jsonObj = (JSONObject) obj;

			@SuppressWarnings("unchecked")
			Set<String> names = jsonObj.keySet();

			List<String> nameList = new ArrayList<String>();
			if (taskNames == null) {
				for (String x : names)
					nameList.add(x);
			} else {
				nameList = taskNames;
			}

			int id = 0;
			for (String key : nameList) {
				JSONObject job = (JSONObject) jsonObj.get(key);

				double WCET = ((Number) job.get("median_timing")).doubleValue();

//				double highWaterMark = ((Number) job.get("hwm")).doubleValue();
				double medianET = ((Number) job.get("median_timing")).doubleValue();

				JSONArray breaks_j = (JSONArray) job.get("breaks");
				JSONArray slopes_j = (JSONArray) job.get("slopes");
				JSONArray intercepts_j = (JSONArray) job.get("intercepts");

				double[] breaks = new double[breaks_j.size()];
				double[] slopes = new double[slopes_j.size()];
				double[] intercepts = new double[intercepts_j.size()];

				for (int i = 0; i < breaks.length; i++) {
					breaks[i] = ((Number) breaks_j.get(i)).doubleValue();
					slopes[i] = ((Number) slopes_j.get(i)).doubleValue();
					intercepts[i] = ((Number) intercepts_j.get(i)).doubleValue();
				}

				id++;

				RecencyProfileReal crp = new RecencyProfileReal(cache, RecencyType.REAL, key, id, -1, WCET, medianET,
						breaks, slopes, intercepts);
				crps.add(crp);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return crps;
	}

}

    // private Map<Node, HitCore> getHitCores(List<Node> readyNodes, List<Integer> availableCores, long[] availableTimeAllProcs, 
    //         List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, 
    //         List<List<Node>> allocHistory, long currentTime, boolean lcif){
        
	// 	int level2ClusterNum = history_level2.size();
	// 	int level2ClusterSize = history_level1.size() / level2ClusterNum;
    //     LinkedHashMap<Node, HitCore> map = new LinkedHashMap<>();
    //     for (int i = 0; i < readyNodes.size(); i++){
    //         Set<Integer> level1HitCore = new HashSet<>();
    //         Set<Integer> level2HitCore = new HashSet<>();
    //         Set<Integer> level3HitCore = new HashSet<>();
    //         Node n = readyNodes.get(i);

    //         for (int j = 0; j < history_level1.size(); j++) {
    //             if (availableCores.contains(j)){
    //                 int hitCacheLevel = n.crp.computeET(-1, history_level1, history_level2, history_level3, n, j, true, 0, 0, lcif).getSecond();
    //                 switch (hitCacheLevel) {
    //                     case 1:
    //                         level1HitCore.add(j);
	// 						int clusterCoreIdx = (j / level2ClusterSize) * level2ClusterSize;
    //                         level2HitCore.addAll(IntStream.rangeClosed(clusterCoreIdx, clusterCoreIdx + level2ClusterSize - 1).boxed().collect(Collectors.toSet()));
	// 						Set<Integer> filteredLevel2 = level2HitCore.stream().filter(element -> availableCores.contains(element) 
    //                                                                                     && !level1HitCore.contains(element)).collect(Collectors.toSet());
	// 						level2HitCore.clear();
	// 						level2HitCore.addAll(filteredLevel2);

	// 						level3HitCore.addAll(IntStream.rangeClosed(0, history_level1.size() - 1).boxed().collect(Collectors.toSet()));
	// 						Set<Integer> filteredLevel3 = level3HitCore.stream().filter(element -> availableCores.contains(element) && !level2HitCore.contains(element) 
    //                                                                                         && !level1HitCore.contains(element)).collect(Collectors.toSet());
	// 						level3HitCore.clear();
	// 						level3HitCore.addAll(filteredLevel3);
    //                         break;
    //                     case 2:
	// 						level2HitCore.add(j);
	// 						level3HitCore.addAll(IntStream.rangeClosed(0, history_level1.size() - 1).boxed().collect(Collectors.toSet()));
	// 						Set<Integer> _filteredLevel3 = level3HitCore.stream().filter(element -> availableCores.contains(element) && !level2HitCore.contains(element) 
    //                                                                                         && !level1HitCore.contains(element)).collect(Collectors.toSet());
	// 						level3HitCore.clear();
	// 						level3HitCore.addAll(_filteredLevel3);
	// 						break;
	// 					case 3:
	// 						level3HitCore.add(j);
	// 						break;
    //                     default:
    //                         break;
    //                 }
    //             }
	// 		}

	// 		map.put(n, new HitCore(level1HitCore, level2HitCore, level3HitCore));

	// 	}
	// 	return map;
	// }

	    // private Long getSUSacForFutureNodes(Node n, Integer core, List<Node> future, 
    //             List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
        
    //     if (future.size() <= 0){
    //         return (long)0;
    //     }
    //     List<Integer> coreList = new ArrayList<>();
    //     coreList.add(core);
    //     Map<Integer, Long> SUT = getSUT(n, coreList, history_level1, history_level2, history_level3);
    //     Long et_n = (long)n.getWCET() - SUT.get(core);
    //     // Long sum = (long) 0;
    //     Long max = Long.MIN_VALUE;
    //     Node maxNode = null;
    //     Map<Node, List<Pair<Integer, Long>>> speedUpTable = getSUTForAllNodes(future, coreList, history_level1, history_level2, history_level3);
    //     for (Entry<Node, List<Pair<Integer, Long>>> entry : speedUpTable.entrySet()) {
    //         Node node = entry.getKey();
    //         List<Pair<Integer, Long>> sutList = entry.getValue();
    //         if (sutList.get(0).getSecond() > max){
    //             max = sutList.get(0).getSecond();
    //             maxNode = node;
    //         }
    //     }

    //     Node futureNode = maxNode;
    //     long affectedTime1 = futureNode.crp.computeET(-1, history_level1, history_level2,
    //                     history_level3, futureNode, core, true, et_n, 0,false).getFirst().getFirst();
    //     long affectedTime2 = futureNode.crp.computeET(-1, history_level1, history_level2, history_level3, futureNode,
    //                     core, true, 0,0, false).getFirst().getFirst(); 
    //     long affectedTime = affectedTime1 - affectedTime2;
        
    //     //affectedTime = affectedTime < 0 ? 0 : affectedTime;
    //     if (affectedTime < 0) {
    //         System.err.println("CacheAwareAlloc.setPartition(): the affected time is less than 0!");
    //         System.exit(-1);
    //     }
    //     return affectedTime;
    // }

	        
        // future.clear();
        // List<Node> nodesInProc = allocHistory.get(core);
        // long Nodenum = 0;
        // for (int j = nodesInProc.size() - 1; j >= 0; j--) {
        //     Nodenum += nodesInProc.get(j).expectedET;
        //     if (Nodenum >= SystemParameters.v4) { //无法从cache受益的在计算impact时不考虑
        //         break;
        //     }
        //     future.add(nodesInProc.get(j));
        // }

		// private List<Entry<Integer, Long>> getRecencyTable(Node n, List<Integer> procs, List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, long additionalTime){
		// 	Map<Integer, Long> rct = new LinkedHashMap<>();
	
		// 	for (int i = 0; i < procs.size(); i++){
		// 		long recencyDis = n.crp.computeRecency(-1, history_level1, history_level2, history_level3, n, procs.get(i), true, additionalTime);
		// 		rct.put(procs.get(i), recencyDis);
		// 	}
		// 	List<Entry<Integer, Long>> list = new ArrayList<Entry<Integer, Long>>(rct.entrySet());
		// 	Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>() {
		// 		public int compare(Entry<Integer, Long> o1, Entry<Integer, Long> o2) {
		// 			return o1.getValue().compareTo(o2.getValue());
		// 		}
		// 	});
		// 	//rct.sort((c1, c2) -> Long.compare(c1.value, c2.value));
		// 	//return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		// 	return list;
		// }
	
		// //找到会被影响的结点
		// private Map<Node, List<Node>> getAffectedNodes(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<List<Node>> allocHistory, long currentTime){
		// 	Map<Node, List<Node>> res = new LinkedHashMap<>();
	
		// 	for (int i = 0; i < readyNodes.size(); i++){
		// 		Node n = readyNodes.get(i);
		// 		List<Node> tmp = new ArrayList<>();
		// 		//tmp.addAll(readyNodes); tmp.remove(n);
		// 		long startTime = currentTime; long endTime = startTime + n.getWCET();
	
		// 		for (int j = 0; j < allocHistory.size(); j++){
		// 			for (int k = 0; k < allocHistory.get(j).size(); k++){
		// 				Node tmpNode = allocHistory.get(j).get(k);
		// 				//exclude nodes whose next instance is Node n
		// 				if (n.getId() == tmpNode.getId() && n.getDagID() == tmpNode.getDagID() && tmpNode.getDagInstNo() + 1 == n.getDagInstNo()){
		// 					continue;
		// 				}
		// 				long T = Utils.getDagByIndex(dags, tmpNode.getDagID(), tmpNode.getDagInstNo()).sched_param.getPeriod();
		// 				long nextArrival = tmpNode.start + T;
		// 				if (nextArrival < endTime && nextArrival >= currentTime){
		// 					//exclude the nodes already in readynodes
		// 					if (!readyNodes.contains(Utils.getDagByIndex(dags, tmpNode.getDagID(), tmpNode.getDagInstNo() + 1).getNodeById(tmpNode.getId()))){
		// 						tmp.add(tmpNode);
		// 					}
		// 				}
		// 			}
		// 		}
		// 		res.put(n, tmp);
		// 	}
		// 	return res;
		// }

		
    // private List<Node> getFutureNodes(List<Integer> cores, long[] coreTime, Node[] currentExe){
    //     List<Node> futureNodes = new ArrayList<>();
    //     //LinkedHashMap<Integer, Long> id_to_waiting = new LinkedHashMap<>();
	// 	// determine the core set based on medTime -- futureProc

	// 	List<Node> nodesTobedone = new ArrayList<>();
	// 	for (int i = 0; i < cores.size(); i++) {
	// 		if (currentExe[i] != null && currentExe[i].finishAt <= coreTime[i]) {
	// 			nodesTobedone.add(currentExe[i]);
	// 		}

	// 	}
	// 	// determine the node to be free -- futureNodes
	// 	for (Node tmp : nodesTobedone) {
	// 		for (Node child : tmp.getChildren()) {
	// 			if (futureNodes.contains(child) || child.start != -1) {
	// 				// already added
	// 				continue;
	// 			}
	// 			//long worst_time = tmp.finishAt;
	// 			boolean isReady = true;
	// 			for (Node parent : child.getParent()) {
	// 				// haven't been finished before and would not be finished this turn
	// 				if (!parent.finish && !nodesTobedone.contains(parent)) {
	// 					isReady = false;
	// 					break;
	// 				}
	// 				// if (nodesTobedone.contains(parent)) {
	// 				// 	worst_time = Math.max(worst_time, parent.finishAt);
	// 				// }
	// 			}
	// 			if (isReady) {
	// 				futureNodes.add(child);
	// 				//id_to_waiting.put(child.getId(), worst_time);
	// 			}
	// 		}
	// 	}
    //     return futureNodes;
    // }