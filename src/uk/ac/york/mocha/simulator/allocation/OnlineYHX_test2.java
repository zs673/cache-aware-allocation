package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.event.InternalFrameEvent;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.math3.util.Pair;
//import org.python.antlr.base.boolop;
//
//import it.unimi.dsi.fastutil.Hash;
import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.entity.Node;
import uk.ac.york.mocha.simulator.entity.Node.NodeType;
import uk.ac.york.mocha.simulator.generator.HitCoreNew;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.simulator.Utils;

public class OnlineYHX_test2 extends AllocationMethodsYHX {
    static int delayCnt3 = 0;
    static int futureSac3 = 0;
    static int missCnt = 0;
    static int sacCnt = 0;

	@Override
	public boolean allocate(List<DirectedAcyclicGraph> dags, List<Node> readyNodes, List<List<Node>> localRunqueue,
			List<Integer> cores, long[] availableTimeAllProcs, List<List<Node>> history_level1,
			List<List<Node>> history_level2, List<Node> history_level3, List<List<Node>> allocHistory, long currentTime,
			boolean lcif, List<Node> etHist, List<Double> speeds, Node[] currentExe) {
        
        boolean hasAlloc = false;
		List<Integer> availableCores = new ArrayList<>();
		for (int i = 0; i < cores.size(); i++) {
			if (localRunqueue.get(i).size() == 0 && availableTimeAllProcs[i] <= currentTime)
				availableCores.add(i);
		}

		readyNodes.stream().forEach(c -> c.partition = -1);

        //yhx
        // if (readyNodes.get(0).getType() == NodeType.SOURCE){
		// 	System.out.println("compare: A new instance starts" + readyNodes.get(0).getDagInstNo());
		// }
        Map<Node, HitCoreNew> hitCore = getHitCores(readyNodes, availableCores, availableTimeAllProcs, history_level1, history_level2, history_level3, allocHistory, currentTime, lcif);

		//readyNodes.sort((c1, c2) -> Utils.compareNodeForYHX(dags, c1, c2, hitCore));
        readyNodes.sort((c1, c2) -> Utils.compareNode(dags, c1, c2));

        //debug
        for (Node n : readyNodes) {
			if (n.getDagID() == 1 && n.getDagInstNo() == 1) {
				break;
			}
		}

        /*
         * only take the nodes which will be allocated in this allocation run into account
         */
		List<Node> preEligible = new ArrayList<>();
		for (int i = 0; i < availableCores.size(); i++) {
			if (readyNodes.size() == i)
				break;
			preEligible.add(readyNodes.get(i)); //找readyNode和空闲核的最小值
		}
        List<Integer> availableP = new ArrayList<>(availableCores);

        List<Node> affect = new ArrayList<>(preEligible);
		List<Integer> allocProcs = new ArrayList<>();
		List<Node> allocNodes = new ArrayList<>();
        Map<Node, List<Pair<Integer, Long>>> speedUpTable = getSUTForAllNodes(preEligible, availableCores, history_level1, history_level2, history_level3);
        // Map<Node, List<Pair<Integer, Long>>> sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
        // allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, false); 
        Map<Node, List<Pair<Integer, Long>>> sacrificeF = new LinkedHashMap<>();  
        Map<Node, List<Pair<Integer, Long>>> sacrifice = getSacrifice_new(preEligible, availableP, allocProcs, speedUpTable);

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
			
			Pair<Node, Integer> p = setPartition2(dags, speedUpTable, sacrifice, sacrificeF, allocNodes, allocProcs, allocHistoryCut, allocHistory,
            preEligible, availableP, cores, availableTimeAllProcs, currentTime, lcif, history_level1, history_level2,
            history_level3);
            Node n = p.getFirst(); Integer core = p.getSecond();
            if (core != -1){
                hasAlloc = true;
                n.partition = core;
                // if (n.getDagInstNo() == 1){
                //     System.out.println("compare:" + n + "->" + core);
                // }
                allocNodes.add(n);
                allocProcs.add(core);

                localRunqueue.get(n.partition).add(n);//加到localRunqueue
                //更新代价
                affect.remove(n);
                preEligible.remove(n);
                // sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                //                         allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, false);
                 sacrifice = getSacrifice_new(preEligible, availableP, allocProcs, speedUpTable);
            }else{
                allocNodes.add(n);
                //更新代价
                affect.remove(n);
                // affectF.add(n);
                preEligible.remove(n);
                // sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                //                         allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, false);
                 sacrifice = getSacrifice_new(preEligible, availableP, allocProcs, speedUpTable);
                // sacrificeF = getSacrifice(preEligible, affectF, hitCore, availableCores, availableTimeAllProcs, 
                //                         allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, true);
            }
		}
			
		//从readyNode移走已分配的结点
		for (int i = 0; i < readyNodes.size(); i++) {
			if (readyNodes.get(i).partition > -1) {
				readyNodes.remove(i);
				i--;
			}
		}
        // System.out.println("compare delay cnt: " + delayCnt3);
        //System.out.println("compare sac cnt: " + sacCnt);
        return hasAlloc;
	}

    // SAC + LCIF
	private Pair<Node, Integer> setPartition2(List<DirectedAcyclicGraph> dags, Map<Node, List<Pair<Integer, Long>>> SUT, Map<Node, List<Pair<Integer, Long>>> sacrifice, Map<Node, List<Pair<Integer, Long>>> sacrificeF, List<Node> allocNodes,
                                                List<Integer> allocProcs, List<List<Node>> allocHistory, List<List<Node>> fullAllocHistory,
                                                List<Node> preEligible, List<Integer> procs, List<Integer> cores, long[] availableTimeAllProcs, long time, boolean lcif,
                                                List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3) {
        
        Node nToAlloc = null; Integer core = -1;
        Long maxValue = Long.MIN_VALUE;
        Long minValueSac = (long)0; Long minValueSU = (long)0; 
        for (Entry<Node, List<Pair<Integer, Long>>> entry : SUT.entrySet()) {
            Node n = entry.getKey();
            if (!allocNodes.contains(n)){
                List<Pair<Integer, Long>> sutList = entry.getValue();
                List<Pair<Integer, Long>> sacList = sacrifice.get(n);
                // List<Pair<Integer, Long>> sacListF = sacrificeF.get(n);
                for (int i = 0; i < sutList.size(); i++){
                    if (!allocProcs.contains(sutList.get(i).getFirst())){
                        if (sacList.get(i).getSecond() > maxValue){
                            
                            maxValue = sacList.get(i).getSecond();
                            nToAlloc = n;
                            core = sutList.get(i).getFirst();
                            // minValueF = sacListF.get(i).getSecond();
                            minValueSU = sutList.get(i).getSecond();
                            minValueSac = sacList.get(i).getSecond();
                        }
                    }
                }

            }
        }
        
        List<Integer> candidateC = new ArrayList<>();
        for (int i = 0; i < procs.size(); i++){
            Integer proc = procs.get(i);
            long sut = 0; long sac = 0; 
            if (!allocProcs.contains(proc)){
                sac = sacrifice.get(nToAlloc).get(i).getSecond();
                sut = SUT.get(nToAlloc).get(i).getSecond();
                // sacF = sacrificeF.get(nToAlloc).get(i).getSecond();
                if (sac == maxValue){
                    candidateC.add(proc);
                }
            
            }
        }

        long max = Long.MIN_VALUE;
        if (candidateC.size() > 1){
            for (int i = 0; i < candidateC.size(); i++){
                Integer c = candidateC.get(i);
                Integer index = procs.indexOf(c);
                long sut = 0; long sac = 0; 
                sac = sacrifice.get(nToAlloc).get(index).getSecond();
                sut = SUT.get(nToAlloc).get(index).getSecond();
                // sacF = sacrificeF.get(nToAlloc).get(index).getSecond();
                long recencyFree = getRecencyFree(c, allocHistory, procs, history_level1, history_level2, history_level3);
                if (recencyFree > max){
                    max = recencyFree;
                    core = c;
                    minValueSac = sac; minValueSU = sut; 
                }
            }
        }

        if (nToAlloc == null || core == -1) {
            System.err.println("SimpleCacheAware.getIndexOfMaximum(): Cannot find the max value!");
            System.exit(-1);
        }

        Integer cache = nToAlloc.crp.computeET(-1, history_level1, history_level2, history_level3, nToAlloc,
                            core, true, 0,0, false).getSecond(); 
        // boolean delay1 = (cache < 3) && (maxValue <= 0);
        // boolean delay2 = (cache >= 3) && (minValueF + minValueSac >= 0);
        boolean delay1 = maxValue <= 0;
        delay1 = true;
        boolean delay3 = (time + nToAlloc.getWCET()) <= (nToAlloc.release + Utils.getDagByIndex(dags, nToAlloc.getDagID(), nToAlloc.getDagInstNo()).sched_param.getPeriod());
        // if (!delay3){
        //     missCnt++;
        //     System.out.println("miss: " + missCnt);
        // }
        boolean delay4 = false;
        Integer futureCore = -1;
        for (Integer c : cores){
            //if (nToAlloc.notFitCore.contains(c) || (procs.contains(c) && allocProcs.contains(c))){
            if (nToAlloc.notFitCore.contains(c) || procs.contains(c)){
                continue;
            }
            long predictET = nToAlloc.crp.computeET(-1, history_level1, history_level2,
                            history_level3, nToAlloc, c, true, 0, 0,false).getFirst().getFirst();
            long predictSpeedup = nToAlloc.getWCET() - predictET;
            if(minValueSU + availableTimeAllProcs[c] - time < predictSpeedup){
                delay4 = true;
                futureCore = c;
                break;
            }
        }
        // boolean delay = (delay1 || delay2) && delay3 && delay4;
        boolean delay = delay1 && delay3 && delay4;
        delay = false;
        if (!delay){
            return new Pair<Node, Integer>(nToAlloc, core);
        }else{
            nToAlloc.delayCnt++;
            List<Integer> tmp = new ArrayList<>(procs);
            tmp.removeAll(allocProcs);
            nToAlloc.notFitCore.addAll(tmp);
            delayCnt3++;
            //System.out.println("delay: " + delayCnt3);
            return new Pair<Node, Integer>(nToAlloc, -1);
        }
    }

    private List<Node> getFutureNodes(List<Integer> cores, long[] coreTime, Node[] currentExe){
        //LinkedHashMap<Integer, Long> id_to_waiting = new LinkedHashMap<>();
		// determine the core set based on medTime -- futureProc

		List<Node> nodesTobedone = new ArrayList<>();
		for (int i = 0; i < cores.size(); i++) {
			if (currentExe[i] != null && currentExe[i].finishAt <= coreTime[i]) {
				nodesTobedone.add(currentExe[i]);
			}

		}
		List<Node> futureNodes = getReleseNodes(nodesTobedone, nodesTobedone, new ArrayList<>());
        return futureNodes;
    }
    // private List<Node> getFutureNodes(Node[] currentExe, long nextTime, List<Node> readyNodes, List<List<Node>> history_level1){
	// 	List<Node> nodesTobedone1 = new ArrayList<>();
    //     List<Node> nodesTobedone2 = new ArrayList<>();
	// 	for (int i = 0; i < history_level1.size(); i++) {
	// 		if (currentExe[i] != null && currentExe[i].finishAt <= nextTime) {
	// 			nodesTobedone1.add(currentExe[i]);
	// 		}else if(currentExe[i] != null && currentExe[i].finishAt > nextTime){
    //             nodesTobedone2.add(currentExe[i]);
    //         }
	// 	}
    //     List<Node> totNodes = new ArrayList<>(nodesTobedone1);
    //     totNodes.addAll(nodesTobedone2);
    //     List<Node> releaseBeforeTime = getReleseNodes(nodesTobedone1, nodesTobedone1, new ArrayList<>());
    //     List<Node> releaseAfterTime = getReleseNodes(nodesTobedone2, totNodes, new ArrayList<>());
    //     if (releaseAfterTime.size() > 0){
    //         int debug = 1;
    //     }
	// 	// determine the node to be free -- futureNodes
    //     releaseBeforeTime.addAll(readyNodes);

    //     List<Node> currentExe_copy = new ArrayList<>();
    //     Collections.addAll(currentExe_copy, currentExe);
    //     List<Node> release = getReleseNodes(releaseBeforeTime, releaseBeforeTime, currentExe_copy);
    //     release.addAll(releaseAfterTime);
    //     return release;
    // }

    private List<Node> getReleseNodes(List<Node> nodesTobedone, List<Node> totTobedone, List<Node> extraDone){
        List<Node> releaseNodes = new ArrayList<>();
        for (Node tmp : nodesTobedone) {
			for (Node child : tmp.getChildren()) {
				if (releaseNodes.contains(child) || child.start != -1) {
					// already added
					continue;
				}
				//long worst_time = tmp.finishAt;
				boolean isReady = true;
				for (Node parent : child.getParent()) {
					// haven't been finished before and would not be finished this turn
					if (!parent.finish && !totTobedone.contains(parent) && !extraDone.contains(parent)) {
						isReady = false;
						break;
					}
				}
				if (isReady) {
					releaseNodes.add(child);
				}
			}
		}
        return releaseNodes;
    }

    private Map<Node, HitCoreNew> getHitCores(List<Node> readyNodes, List<Integer> availableCores, long[] availableTimeAllProcs, 
    List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, 
    List<List<Node>> allocHistory, long currentTime, boolean lcif){

        int level2ClusterNum = history_level2.size();
        int level2ClusterSize = history_level1.size() / level2ClusterNum;
        LinkedHashMap<Node, HitCoreNew> map = new LinkedHashMap<>();
        for (int i = 0; i < readyNodes.size(); i++){
            List<Integer> level1HitCore = new ArrayList<>();
            List<Integer> level2HitCore = new ArrayList<>();
            List<Integer> level3HitCore = new ArrayList<>();
            Node n = readyNodes.get(i);

            for (int j = 0; j < availableCores.size(); j++) {
                Integer core = availableCores.get(j);
                int hitCacheLevel = n.crp.computeET(-1, history_level1, history_level2, history_level3, n, core, true, 0, 0, lcif).getSecond();
                switch (hitCacheLevel) {
                    case 1:
                        level1HitCore.add(core);
                        break;
                    case 2:
                        level2HitCore.add(core);
                        break;
                    case 3:
                        level3HitCore.add(core);
                        break;
                    default:
                        break;
                }
            }
            map.put(n, new HitCoreNew(level1HitCore, level2HitCore, level3HitCore));
        }
        return map;
    }

	private Long getRecencyFree(int core, List<List<Node>> allocHistory, List<Integer> procs, 
                                List<List<Node>> history_level1,  List<List<Node>> history_level2, List<Node> history_level3){

		//long et_n = n.crp.computeET(-1, history_level1, history_level2, history_level3, n, core, true, 0, 0, false).getFirst().getFirst();
		int idx = procs.indexOf(core);
		if (idx == -1){
			System.out.println("*****");
		}
		List<Node> nodesInProc = allocHistory.get(idx);

		long nodeNum = 0; //Get the nodes that can hit level two cache in each free core.
		List<Node> affectedNodes = new ArrayList<>();
		for (int j = nodesInProc.size() - 1; j >= 0; j--) {
			nodeNum += nodesInProc.get(j).expectedET;

			if (nodeNum >= SystemParameters.v3) { //无法从cache受益的在计算impact时不考虑
				break;
			}
			affectedNodes.add(nodesInProc.get(j));
		}

		Long sum = (long) SystemParameters.v3 + 1;//空负载的核心优先
        if (affectedNodes.size() > 0){
            Node earliestNode = affectedNodes.get(affectedNodes.size() - 1);
            Long recencyL2 = earliestNode.crp.computeRecency(-1, history_level1, history_level2, history_level3, earliestNode, core, true, 0);
            sum = SystemParameters.v3 - recencyL2;
        }
		return sum;
	}

    public Map<Node, List<Pair<Integer, Long>>> getSUTForAllNodes(List<Node> preEligible, List<Integer> availableP, List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
        Map<Node, List<Pair<Integer, Long>>> speedUpTable = new LinkedHashMap<>();
		for (Node n : preEligible) {
			List<Pair<Integer, Long>> ETdrop = new ArrayList<>();
			for (int i = 0; i < availableP.size(); i++) {
				int proc = availableP.get(i);
                long WCET = n.getWCET();
                long realET = n.crp
                        .computeET(-1, history_level1, history_level2, history_level3, n, proc, true, 0, 0, false)
                        .getFirst().getFirst();
                long speedup = WCET - realET;

                ETdrop.add(new Pair<Integer, Long>(proc, speedup));
			}
			speedUpTable.put(n, ETdrop);
		}
        return speedUpTable;
    }

    private static long getSpeedUp(Map<Node, List<Pair<Integer, Long>>> filteredTable, Node node, int coreId) {
        List<Pair<Integer, Long>> coreSpeedPairs = filteredTable.get(node);
        if (coreSpeedPairs != null) {
            for (Pair<Integer, Long> pair : coreSpeedPairs) {
                if (pair.getFirst() == coreId) {
                    return pair.getSecond();
                }
            }
        }
        return 0; // Return 0 if no speed-up value is found for the core
    }

    private static Pair<Map<Integer, Long>, Map<Node, Long>> maskTable(Map<Node,List<Pair<Integer, Long>>> filteredTable, Node node, Integer core){
        // Map<Node, List<Pair<Integer, Long>>> newFilteredTable = new HashMap<>();

        // for (Map.Entry<Node, List<Pair<Integer, Long>>> entry : filteredTable.entrySet()) {
        //     List<Pair<Integer, Long>> newCoreSpeedPairs = new ArrayList<>();
        //     for (Pair<Integer, Long> pair : entry.getValue()) {
        //         // Copy each pair to avoid modifying the original
        //         newCoreSpeedPairs.add(new Pair<>(pair.getFirst(), pair.getSecond()));
        //     }
        //     newFilteredTable.put(entry.getKey(), newCoreSpeedPairs);
        // }

        Map<Integer, Long> maxPerCore = new HashMap<>();
        for (Map.Entry<Node, List<Pair<Integer, Long>>> entry : filteredTable.entrySet()) {
            Node n = entry.getKey();
            if (n.equals(node)) {
                continue;
            }
            List<Pair<Integer, Long>> coreSpeedPairs = entry.getValue();
            for (Pair<Integer, Long> pair : coreSpeedPairs) {
                int coreId = pair.getFirst();
                long speedUp = pair.getSecond();
                if (coreId == core) {
                    continue;
                }
                // Update minimum for this core
                maxPerCore.put(coreId, Math.max(maxPerCore.getOrDefault(coreId, Long.MIN_VALUE), speedUp));
            }
        }

        // Print minimum values per column (core)
        // System.out.println("Minimum values per core (column):");
        // for (Map.Entry<Integer, Long> entry : maxPerCore.entrySet()) {
        //     System.out.println("Core " + entry.getKey() + ": " + entry.getValue());
        // }

        // Step 2: Find minimum value for each row (node)
        Map<Node, Long> maxPerNode = new HashMap<>();
        for (Map.Entry<Node, List<Pair<Integer, Long>>> entry : filteredTable.entrySet()) {
            Node n = entry.getKey();
            if (n.equals(node)) {
                continue;
            }
            List<Pair<Integer, Long>> coreSpeedPairs = entry.getValue();

            long maxSpeedUp = Long.MIN_VALUE;
            for (Pair<Integer, Long> pair : coreSpeedPairs) {
                int coreId = pair.getFirst();
                if (coreId == core) {
                    continue;
                }
                maxSpeedUp = Math.max(maxSpeedUp, pair.getSecond());
            }

            maxPerNode.put(n, maxSpeedUp);
        }

        // if (node == 3 && core == 1){
        //     for (Map.Entry<Integer, List<Pair<Integer, Long>>> entry : filteredTable.entrySet()) {
        //         for (Pair<Integer, Long> pair : entry.getValue()) {
        //             System.out.println("node" + node + "Core" + core + "Core: " + pair.getFirst() + ", SpeedUp: " + pair.getSecond());
        //         }
        //     }
        // }

        // for (Map.Entry<Node, List<Pair<Integer, Long>>> entry : filteredTable.entrySet()) {
        //     for (Pair<Integer, Long> pair : entry.getValue()) {
        //         System.out.println("node" + node + "Core" + core + "Core: " + pair.getFirst() + ", SpeedUp: " + pair.getSecond());
        //     }
        // }
        return new Pair<Map<Integer, Long>, Map<Node, Long>>(maxPerCore, maxPerNode);
    }


    private static Map<Node, List<Pair<Integer, Long>>> getSacrifice_new(List<Node> preEligible, List<Integer> availableP, List<Integer> allocProcs, Map<Node, List<Pair<Integer, Long>>> speedUpTable){
        // Map<Integer, List<Pair<Integer, Long>>> sacrifice = new LinkedHashMap<>();

        // Filter out nodes not in preEligibleNodes
        Map<Node, List<Pair<Integer, Long>>> filteredTable = new HashMap<>();
        for (Map.Entry<Node, List<Pair<Integer, Long>>> entry : speedUpTable.entrySet()) {
            Node node = entry.getKey();
            if (preEligible.contains(node)) {
                // Only retain cores in idleCores
                List<Pair<Integer, Long>> filteredCores = new ArrayList<>();
                for (Pair<Integer, Long> coreSpeedPair : entry.getValue()) {
                    if (!allocProcs.contains(coreSpeedPair.getFirst())) {
                        filteredCores.add(coreSpeedPair);
                    }
                }
                filteredTable.put(node, filteredCores);
            }
        }
        
        Map<Node, List<Pair<Integer, Long>>> newTable = new HashMap<>();
        // Iterate over each node in filteredTable
        for (Map.Entry<Node, List<Pair<Integer, Long>>> entry : speedUpTable.entrySet()) {
            Node v_j = entry.getKey();
            List<Pair<Integer, Long>> coreSpeedPairs = entry.getValue();
            List<Pair<Integer, Long>> newCoreSpeedPairs = new ArrayList<>();

            // Iterate over each core and compute new values
            for (Pair<Integer, Long> coreSpeedPair : coreSpeedPairs) {
                int p_k = coreSpeedPair.getFirst();
                long S_vj_pk = coreSpeedPair.getSecond();

                // Calculate L(v_j, p_k) based on the formula provided
                long L_vj_pk = 0;
                Pair<Map<Integer, Long>, Map<Node, Long>> maxPerCoreNode = maskTable(filteredTable, v_j, p_k);
                Map<Integer, Long> maxPerCore = maxPerCoreNode.getFirst(); Map<Node, Long> maxPerNode = maxPerCoreNode.getSecond();
                for (Node v_i : preEligible) {
                    if (v_i.equals(v_j)) continue; // Skip v_j itself

                    // Get S(v_i, p_k)
                    long S_vi_pk = getSpeedUp(filteredTable, v_i, p_k);

                    // Find max(S(v_i, p_x)) for cores other than p_k
                    long maxSpeedUpOtherCores = Long.MIN_VALUE;
                    for (Integer p_x : availableP) {
                        if (p_x == p_k || allocProcs.contains(p_x)) continue;
                        long S_vi_px = getSpeedUp(filteredTable, v_i, p_x);
                        if (S_vi_px >= maxPerCore.get(p_x))
                            maxSpeedUpOtherCores = Math.max(maxSpeedUpOtherCores, S_vi_px);
                    }
                    maxSpeedUpOtherCores = maxSpeedUpOtherCores == Long.MIN_VALUE ? maxPerNode.get(v_i) : maxSpeedUpOtherCores;

                    // Compute the maximum difference
                    L_vj_pk = Math.max(L_vj_pk, S_vi_pk - maxSpeedUpOtherCores);
                }

                // Recalculate the speed-up \overline{S}(v_j, p_k)
                long recalibratedSpeedUp = Math.max(0, S_vj_pk - L_vj_pk);

                // Store the recalibrated value in the new table
                newCoreSpeedPairs.add(new Pair<>(p_k, recalibratedSpeedUp));
            }

            // Add the recalibrated list to the new table for node v_j
            newTable.put(v_j, newCoreSpeedPairs);
        }

        return newTable;
    }

    // public static void main(String[] args) {
    //     // Initialize nodes
    //     Integer v1 = 1;
    //     Integer v2 = 2;
    //     Integer v3 = 3;

    //     // Initialize cores
    //     List<Integer> preEligible = Arrays.asList(1, 2, 3);
    //     List<Integer> availableP = Arrays.asList(1, 2, 3); // p1, p2, p3
    //     List<Integer> allocProcs = new ArrayList<>(); // Assume p2 is allocated

    //     // Pre-eligible nodes
    //     // List<Node> preEligible = Arrays.asList(v1, v2, v3);

    //     // Initialize the speedUpTable with the given values
    //     Map<Integer, List<Pair<Integer, Long>>> speedUpTable = new HashMap<>();
    //     speedUpTable.put(v1, Arrays.asList(new Pair<>(1, 510L), new Pair<>(2, 500L), new Pair<>(3, 500L)));
    //     speedUpTable.put(v2, Arrays.asList(new Pair<>(1, 400L), new Pair<>(2, 600L), new Pair<>(3, 400L)));
    //     speedUpTable.put(v3, Arrays.asList(new Pair<>(1, 500L), new Pair<>(2, 200L), new Pair<>(3, 200L)));

    //     // Call the getSacrifice_new method
    //     Map<Integer, List<Pair<Integer, Long>>> result = getSacrifice_new(preEligible, availableP, allocProcs, speedUpTable);

    //     // Print the result
    //     for (Map.Entry<Integer, List<Pair<Integer, Long>>> entry : result.entrySet()) {
    //         for (Pair<Integer, Long> pair : entry.getValue()) {
    //             System.out.println("  Core: " + pair.getFirst() + ", SpeedUp: " + pair.getSecond());
    //         }
    //     }
    // }
}

