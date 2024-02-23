package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;

import it.unimi.dsi.fastutil.Hash;
import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.entity.Node;
import uk.ac.york.mocha.simulator.entity.Node.NodeType;
import uk.ac.york.mocha.simulator.generator.HitCore;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class OnlineYHX_syn extends AllocationMethods {
    private Random rng = new Random(1000);

	@Override
	public void allocate(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<List<Node>> localRunqueue,
			List<Integer> cores, long[] availableTimeAllProcs, List<List<Node>> history_level1,
			List<List<Node>> history_level2, List<Node> history_level3, List<List<Node>> allocHistory, long currentTime,
			boolean lcif, List<Node> etHist, List<Double> speeds, Node[] currentExe) {

		List<Integer> availableCores = new ArrayList<>();
		for (int i = 0; i < cores.size(); i++) {
			if (localRunqueue.get(i).size() == 0 && availableTimeAllProcs[i] <= currentTime)
				availableCores.add(i);
		}

		readyNodes.stream().forEach(c -> c.partition = -1);

        //yhx
        if (readyNodes.get(0).getType() == NodeType.SOURCE){
			System.out.println("A new instance starts");
		}
		/*
		 * Sort ready nodes list by FPS+WF, take first procNum nodes to allocate.  Order nodes by 1) its DAG priority and 2) its WCET.
		 */
		readyNodes.sort((c1, c2) -> Utils.compareNode(dags, c1, c2));
        /*
         * only take the nodes which will be allocated in this allocation run into account
         */
		List<Node> preEligible = new ArrayList<>();
		for (int i = 0; i < availableCores.size(); i++) {
			if (readyNodes.size() == i)
				break;
			preEligible.add(readyNodes.get(i)); //找readyNode和空闲核的最小值
		}

        List<Node> affect = new ArrayList<>(preEligible);
        List<Node> futureNodes = getFutureNodes(cores, availableTimeAllProcs, currentExe);
        affect.addAll(futureNodes);
		

        //Map<Node, List<Node>> affectedList = getAffectedNodes(dags, readyNodes, allocHistory, currentTime);
        Map<Node, HitCore> hitCore = getHitCores(affect, availableCores, availableTimeAllProcs, history_level1, history_level2, history_level3, allocHistory, currentTime, lcif);
        Map<Node, List<Pair<Integer, Long>>> sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                                                            history_level1, history_level2, history_level3, allocHistory, currentTime, lcif);

		List<Integer> availableP = new ArrayList<>(availableCores);

        Map<Node, List<Pair<Integer, Long>>> speedUpTable = new HashMap<>();
		for (Node n : preEligible) {
			List<Pair<Integer, Long>> ETdrop = new ArrayList<>();
			for (int i = 0; i < availableP.size(); i++) {
				int proc = i;
				if (availableP.contains(proc)) {
					/*
					 * Speed up by ABSOLUTE value
					 */
					long WCET = n.getWCET();
					long realET = n.crp
							.computeET(-1, history_level1, history_level2, history_level3, n, proc, true, 0, 0, false)
							.getFirst().getFirst();
					long speedup = WCET - realET;

					ETdrop.add(new Pair<Integer, Long>(proc, speedup));
				}
			}
			speedUpTable.put(n, ETdrop);
		}

		//已分配
		List<Integer> allocProcs = new ArrayList<>();
		List<Node> allocNodes = new ArrayList<>();
		//空闲core的history_level1
		List<List<Node>> historyCut = new ArrayList<>();
		for (int i = 0; i < history_level1.size(); i++) {
			if (availableP.contains(i))
				historyCut.add(history_level1.get(i));
		}
		//空闲core的分配历史 索引和availableP对应（存的空闲core id）
		List<List<Node>> allocHistoryCut = new ArrayList<>();
		for (int i = 0; i < allocHistory.size(); i++) {
			if (availableP.contains(i))
				allocHistoryCut.add(allocHistory.get(i));
		}
        
        Integer availableNodes = preEligible.size();
        for (int k = 0; k < availableP.size(); k++) {
			if (k >= availableNodes)
				break;//没那么多node分到多的核上
			
			// Node n = preEligible.get(k);
			// if (allocNodes.contains(n)){
			// 	continue;
			// }
			Pair<Node, Integer> p = setPartition2(speedUpTable, sacrifice, allocNodes, allocProcs, allocHistoryCut, allocHistory,
            preEligible, availableP, availableTimeAllProcs, currentTime, lcif, history_level1, history_level2,
            history_level3);
            Node n = p.getFirst(); Integer core = p.getSecond();

			n.partition = core;

			allocNodes.add(n);
			allocProcs.add(core);

			localRunqueue.get(n.partition).add(n);//加到localRunqueue
            //更新代价
            affect.remove(n);
            preEligible.remove(n);
            sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                                    history_level1, history_level2, history_level3, allocHistory, currentTime, lcif);
		}
		//从readyNode移走已分配的结点
		for (int i = 0; i < readyNodes.size(); i++) {
			if (readyNodes.get(i).partition > -1) {
				readyNodes.remove(i);
				i--;
			}
		}

	}

    // SAC + LCIF
	private Pair<Node, Integer> setPartition2(Map<Node, List<Pair<Integer, Long>>> SUT, Map<Node, List<Pair<Integer, Long>>> sacrifice, List<Node> allocNodes,
                                                List<Integer> allocProcs, List<List<Node>> allocHistory, List<List<Node>> fullAllocHistory,
                                                List<Node> preEligible, List<Integer> procs, long[] availableTimeAllProcs, long time, boolean lcif,
                                                List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3) {

        // 是否要排除已分配的
        Map<Node, Long> minValue = new HashMap<>();
        Map<Node, Long> avg = new HashMap<>();
        for (Entry<Node, List<Pair<Integer, Long>>> entry : sacrifice.entrySet()) {
            Long min = Long.MAX_VALUE; Long sum = (long)0; Integer cnt = 0;
            Node n = entry.getKey();
            if (!allocNodes.contains(n)){
                List<Pair<Integer, Long>> sacList = entry.getValue();
                for (int i = 0; i < sacList.size(); i++){
                    if (!allocProcs.contains(sacList.get(i).getFirst())){
                        sum += sacList.get(i).getSecond();
                        cnt++;
                        if (min > sacList.get(i).getSecond()){
                            min = sacList.get(i).getSecond();
                        }
                    }
                }
                sum = (Long)(sum / cnt);
                minValue.put(n, min);
                avg.put(n, sum);
            }
        }

        Node nToAlloc = null; Integer core = -1;
        Long maxSUTValue = Long.MIN_VALUE;
        for (Entry<Node, List<Pair<Integer, Long>>> entry : SUT.entrySet()) {
            Node n = entry.getKey();
            if (!allocNodes.contains(n)){
                List<Pair<Integer, Long>> sutList = entry.getValue();
                List<Pair<Integer, Long>> sacList = entry.getValue();
                for (int i = 0; i < sutList.size(); i++){
                    if (!allocProcs.contains(sutList.get(i).getFirst())){
                        if (sutList.get(i).getSecond() > maxSUTValue){
                            // if (sacList.get(i).getSecond() > sum){
                            //     continue;
                            // }
                            if (sacList.get(i).getSecond() > minValue.get(n)){
                                continue;
                            }
                            maxSUTValue = sutList.get(i).getSecond();
                            nToAlloc = n;
                            core = sutList.get(i).getFirst();
                        }
                    }
                }

            }
        }

        //改为之前的想法 分配给recency miss余地最小的 或者不要这一步
        // if (lcif) {

        // }

        if (nToAlloc == null || core == -1) {
            System.err.println("SimpleCacheAware.getIndexOfMaximum(): Cannot find the max value!");

            System.exit(-1);
        }

        return new Pair<Node, Integer>(nToAlloc, core);
    }

    private List<Node> getFutureNodes(List<Integer> cores, long[] coreTime, Node[] currentExe){
        List<Node> futureNodes = new ArrayList<>();
        //HashMap<Integer, Long> id_to_waiting = new HashMap<>();
		// determine the core set based on medTime -- futureProc

		List<Node> nodesTobedone = new ArrayList<>();
		for (int i = 0; i < cores.size(); i++) {
			if (currentExe[i] != null && currentExe[i].finishAt <= coreTime[i]) {
				nodesTobedone.add(currentExe[i]);
			}

		}
		// determine the node to be free -- futureNodes
		for (Node tmp : nodesTobedone) {
			for (Node child : tmp.getChildren()) {
				if (futureNodes.contains(child) || child.start != -1) {
					// already added
					continue;
				}
				//long worst_time = tmp.finishAt;
				boolean isReady = true;
				for (Node parent : child.getParent()) {
					// haven't been finished before and would not be finished this turn
					if (!parent.finish && !nodesTobedone.contains(parent)) {
						isReady = false;
						break;
					}
					// if (nodesTobedone.contains(parent)) {
					// 	worst_time = Math.max(worst_time, parent.finishAt);
					// }
				}
				if (isReady) {
					futureNodes.add(child);
					//id_to_waiting.put(child.getId(), worst_time);
				}
			}
		}
        return futureNodes;
    }

    private Map<Node, HitCore> getHitCores(List<Node> readyNodes, List<Integer> availableCores, long[] availableTimeAllProcs, 
            List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, 
            List<List<Node>> allocHistory, long currentTime, boolean lcif){
        
		int level2ClusterNum = history_level2.size();
		int level2ClusterSize = history_level1.size() / level2ClusterNum;
        HashMap<Node, HitCore> map = new HashMap<>();
        for (int i = 0; i < readyNodes.size(); i++){
            Set<Integer> level1HitCore = new HashSet<>();
            Set<Integer> level2HitCore = new HashSet<>();
            Set<Integer> level3HitCore = new HashSet<>();
            Node n = readyNodes.get(i);

            for (int j = 0; j < history_level1.size(); j++) {
                if (availableCores.contains(j)){
                    int hitCacheLevel = n.crp.computeET(-1, history_level1, history_level2, history_level3, n, j, true, 0, 0, lcif).getSecond();
                    switch (hitCacheLevel) {
                        case 1:
                            level1HitCore.add(j);
							int clusterCoreIdx = (j / level2ClusterSize) * level2ClusterSize;
                            level2HitCore.addAll(IntStream.rangeClosed(clusterCoreIdx, clusterCoreIdx + level2ClusterSize - 1).boxed().collect(Collectors.toSet()));
							Set<Integer> filteredLevel2 = level2HitCore.stream().filter(element -> availableCores.contains(element) 
                                                                                        && !level1HitCore.contains(element)).collect(Collectors.toSet());
							level2HitCore.clear();
							level2HitCore.addAll(filteredLevel2);

							level3HitCore.addAll(IntStream.rangeClosed(0, history_level1.size() - 1).boxed().collect(Collectors.toSet()));
							Set<Integer> filteredLevel3 = level3HitCore.stream().filter(element -> availableCores.contains(element) && !level2HitCore.contains(element) 
                                                                                            && !level1HitCore.contains(element)).collect(Collectors.toSet());
							level3HitCore.clear();
							level3HitCore.addAll(filteredLevel3);
                            break;
                        case 2:
							level2HitCore.add(j);
							level3HitCore.addAll(IntStream.rangeClosed(0, history_level1.size() - 1).boxed().collect(Collectors.toSet()));
							Set<Integer> _filteredLevel3 = level3HitCore.stream().filter(element -> availableCores.contains(element) && !level2HitCore.contains(element) 
                                                                                            && !level1HitCore.contains(element)).collect(Collectors.toSet());
							level3HitCore.clear();
							level3HitCore.addAll(_filteredLevel3);
							break;
						case 3:
							level3HitCore.add(j);
							break;
                        default:
                            break;
                    }
                }
			}

			map.put(n, new HitCore(level1HitCore, level2HitCore, level3HitCore));

		}
		return map;
	}
        

	private Long getRecencyFree(Node n, int core, List<List<Node>> allocHistory, List<Integer> procs, 
                                List<List<Node>> history_level1,  List<List<Node>> history_level2, List<Node> history_level3){

		long et_n = n.crp.computeET(-1, history_level1, history_level2, history_level3, n, core, true, 0, 0, false).getFirst().getFirst();
		int idx = procs.indexOf(core);
		if (idx == -1){
			System.out.println("*****");
		}
		List<Node> nodesInProc = allocHistory.get(idx);

		long nodeNum = 0; //Get the nodes that can hit level two cache in each free core.
		List<Node> affectedNodes = new ArrayList<>();
		for (int j = nodesInProc.size() - 1; j >= 0; j--) {
			nodeNum += nodesInProc.get(j).expectedET;

			if (nodeNum >= SystemParameters.v4) { //无法从cache受益的在计算impact时不考虑
				break;
			}

			affectedNodes.add(nodesInProc.get(j));
		}
		//优先选不让(更少的）别的结点miss L2的核 todo：有可能所有的候选核都会有之前的结点miss L2了，在都miss的情况下是选recency小的还是miss少的（都hit L1感觉speedup都不差，选miss少的吧）
		//也可以换成别的方案：距离L2 miss的阈值最远的，试试哪个好
		// int cnt = 0;
		// for (Node affected : affectedNodes){
		// 	Long recencyL2 = n.crp.computeRecency(-1, history_level1, history_level2, history_level3, affected, core, true, et_n);
		// 	if (recencyL2 > SystemParameters.v3){
		// 		cnt++;
		// 	}
		// }

		Long sum = (long) 0;
		for (Node affected : affectedNodes){
			Long recencyL2 = n.crp.computeRecency(-1, history_level1, history_level2, history_level3, affected, core, true, 0);
			sum += SystemParameters.v3 - recencyL2;
		}
		return sum;
	}

	private List<Entry<Integer, Long>> getRecencyTable(Node n, List<Integer> procs, List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, long additionalTime){
		Map<Integer, Long> rct = new HashMap<>();

		for (int i = 0; i < procs.size(); i++){
			long recencyDis = n.crp.computeRecency(-1, history_level1, history_level2, history_level3, n, procs.get(i), true, additionalTime);
			rct.put(procs.get(i), recencyDis);
		}
		List<Entry<Integer, Long>> list = new ArrayList<Entry<Integer, Long>>(rct.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>() {
			public int compare(Entry<Integer, Long> o1, Entry<Integer, Long> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		//rct.sort((c1, c2) -> Long.compare(c1.value, c2.value));
		//return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return list;
	}

    //找到会被影响的结点
    private Map<Node, List<Node>> getAffectedNodes(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<List<Node>> allocHistory, long currentTime){
        Map<Node, List<Node>> res = new HashMap<>();

        for (int i = 0; i < readyNodes.size(); i++){
            Node n = readyNodes.get(i);
            List<Node> tmp = new ArrayList<>();
            //tmp.addAll(readyNodes); tmp.remove(n);
            long startTime = currentTime; long endTime = startTime + n.getWCET();

            for (int j = 0; j < allocHistory.size(); j++){
                for (int k = 0; k < allocHistory.get(j).size(); k++){
                    Node tmpNode = allocHistory.get(j).get(k);
					//exclude nodes whose next instance is Node n
					if (n.getId() == tmpNode.getId() && n.getDagID() == tmpNode.getDagID() && tmpNode.getDagInstNo() + 1 == n.getDagInstNo()){
						continue;
					}
					long T = Utils.getDagByIndex(dags, tmpNode.getDagID(), tmpNode.getDagInstNo()).sched_param.getPeriod();
					long nextArrival = tmpNode.start + T;
                    if (nextArrival < endTime && nextArrival >= currentTime){
						//exclude the nodes already in readynodes
						if (!readyNodes.contains(Utils.getDagByIndex(dags, tmpNode.getDagID(), tmpNode.getDagInstNo() + 1).getNodeById(tmpNode.getId()))){
							tmp.add(tmpNode);
						}
                    }
                }
            }
            res.put(n, tmp);
        }
        return res;
    }


    private Pair<Integer, Long> findMaxValueKeyInMap(Map<Integer, Long> map, Integer core) {
            Integer maxKey = null;
            Long maxValue = Long.MIN_VALUE;
    
            for (Entry<Integer, Long> entry : map.entrySet()) {
                //跳过被占的核
                if (entry.getKey() == core){
                    continue;
                }
                if (entry.getValue() > maxValue) {
                    maxValue = entry.getValue();
                    maxKey = entry.getKey();
                }
            }
            
            maxValue = maxValue == Long.MIN_VALUE ? 0 : maxValue;
            return new Pair<Integer, Long>(maxKey, maxValue);
    }

    private Map<Integer, Long> getSUT(Node n, List<Integer> coreList, List<List<Node>> history_level1, 
                    List<List<Node>> history_level2, List<Node> history_level3){

        //计算结点n在命中核上的SUT
        Map<Integer, Long> speedUpTable = new HashMap<>();
        for (int i = 0; i < coreList.size(); i++) {
            int proc = coreList.get(i);
            /*
                * Speed up by ABSOLUTE value
                */
            long WCET = n.getWCET();
            long realET = n.crp
                    .computeET(-1, history_level1, history_level2, history_level3, n, proc, true, 0, 0,false)
                    .getFirst().getFirst();//computeET返回三个值，第一个是model估计的ET，第二个是err，第三个是命中第几层缓存
            long speedup = WCET - realET;

            speedUpTable.put(proc, speedup);
        }

        return speedUpTable;
    }

    //加速差
    private Long getSUSac(Node n, Integer core, List<Node> affect, Map<Node, HitCore> hitCore, 
            List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
        
        Long sum = (long) 0;
        for (int i = 0; i < affect.size(); i++){
            if (n == affect.get(i)){
                continue;
            }
            HitCore tmp = hitCore.get(affect.get(i));
            List<Integer> coreList = tmp.getCoreList();
            if (!coreList.contains(core)){
                continue;
            }
            Map<Integer, Long> SUT = getSUT(affect.get(i), coreList, history_level1, history_level2, history_level3);
            Long rawSU = SUT.get(core);
            Pair<Integer, Long> pair = findMaxValueKeyInMap(SUT, core);

            //rawSU < maxSU -> no sacrifice
            sum += (rawSU > pair.getSecond() ? rawSU - pair.getSecond() : 0);
            //if core is not available, predict the Node n will be allocated to the core with MSF
        }
        // for (int i = 0; i < affected.size(); i++){
        //     HitCore tmp = hitCore2.get(readyNodes.get(i));
        //     List<Integer> level2List = new ArrayList<>(tmp.level2);
        //     for (int j = 0; j < level2List.size(); j++){
        //         if (level2List.get(j) == core){
        //             continue;
        //         }
        //         Long recencyL2 = n.crp.computeRecency(-1, history_level1, history_level2, history_level3, affected.get(i), level2List.get(j), true, 0);
        //         sum += SystemParameters.v3 - recencyL2;
        //     }
        // }
        return sum;

    }

    //L1 miss: 只要有争用就算一个 or hit的L1刚好只有这一个才算
    //L2 miss：只要有争用就算一个 or hit的L2刚好只有这一个才算 or L2 recency miss 余地总和（和其他核比较，越小越不好）
    //最大recency的变化
    //加速差
    private Map<Node, List<Pair<Integer, Long>>> getSacrifice(List<Node> readyNodes, List<Node> affect, Map<Node, HitCore> hitCore, List<Integer> availableCores, 
            long[] availableTimeAllProcs, List<List<Node>> history_level1, List<List<Node>> history_level2, 
            List<Node> history_level3, List<List<Node>> allocHistory, long currentTime, boolean lcif){
        
        Map<Node, List<Pair<Integer, Long>>> sacrifice = new HashMap<>();
        for (int i = 0; i < readyNodes.size(); i++){
            //sacrifice.add(new ArrayList<>());
            Node n = readyNodes.get(i);
            //HitCore nHitCore = hitCore.get(n);

            // List<Node> affected = new ArrayList<>(affectedList.get(n));
            // Map<Node, HitCore> affectedHitCore = getHitCores(affected, availableCores, availableTimeAllProcs, history_level1, 
            //                                             history_level2, history_level3, allocHistory, currentTime, lcif);
            List<Pair<Integer, Long>> sacList = new ArrayList<>();
            //List<Integer> coreList = nHitCore.getCoreList();
            List<Integer> coreList = new ArrayList<>(availableCores);
            for (int j = 0; j < coreList.size(); j++){
                Integer core = coreList.get(j);
                Long metric = getSUSac(n, core, affect, hitCore, history_level1, history_level2, history_level3);
                sacList.add(new Pair<Integer, Long>(core, metric));
            }
            if(sacList.size() > 0){
			    sacrifice.put(n, sacList);
            }
        }
        
        return sacrifice;
    }
}

