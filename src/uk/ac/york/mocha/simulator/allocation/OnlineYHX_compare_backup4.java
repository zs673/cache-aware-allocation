package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
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

public class OnlineYHX_compare_backup4 extends AllocationMethodsYHX {
    static int delayCnt3 = 0;
    static int futureSac3 = 0;

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
        for (Node node : readyNodes){
            if (node.getDagInstNo() == 1 && node.getId() == 108){
                int m = 1;
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
        // List<Node> affectF = new ArrayList<>(readyNodes);
        // affectF.removeAll(preEligible);
        List<Node> affectF = new ArrayList<>();
        List<Node> futureNodes = getFutureNodes(cores, availableTimeAllProcs, currentExe);
        List<Node> releaseNodes = getReleseNodes(readyNodes, readyNodes, Arrays.asList(currentExe));
        //affectF.addAll(futureNodes);
        affectF.addAll(releaseNodes);
        // LinkedList<Node> backupNodes = new LinkedList<>(affectF);

        Map<Node, List<Pair<Integer, Long>>> sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                                                            allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, false);
        Map<Node, List<Pair<Integer, Long>>> sacrificeF = getSacrifice(preEligible, affectF, hitCore, availableCores, availableTimeAllProcs, 
                                                            allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, true);                                             
        Map<Node, List<Pair<Integer, Long>>> speedUpTable = getSUTForAllNodes(preEligible, availableP, history_level1, history_level2, history_level3);

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
			
			Pair<Node, Integer> p = setPartition2(dags, speedUpTable, sacrifice, sacrificeF, allocNodes, allocProcs, allocHistoryCut, allocHistory,
            preEligible, availableP, cores, availableTimeAllProcs, currentTime, lcif, history_level1, history_level2,
            history_level3);
            Node n = p.getFirst(); Integer core = p.getSecond();
            if (core != -1){
                hasAlloc = true;
                n.partition = core;
                // if (n.getDagInstNo() == 1 || n.getDagInstNo() == 0){
                //     System.out.println(n + "->" + core);
                // }
                allocNodes.add(n);
                allocProcs.add(core);

                localRunqueue.get(n.partition).add(n);//加到localRunqueue
                //更新代价
                affect.remove(n);
                preEligible.remove(n);
                sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                                        allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, false);
            }else{
                allocNodes.add(n);
                //更新代价
                affect.remove(n);
                affectF.add(n);
                preEligible.remove(n);
                // if (backupNodes.size() > 0){
                //     Node backup = backupNodes.removeFirst();
                //     preEligible.addLast(backup);
                //     affect.add(backup);
                //     affectF.remove(backup);
                //     speedUpTable = getSUTForAllNodes(preEligible, availableP, history_level1, history_level2, history_level3);
                //     k--;
                // }
                sacrifice = getSacrifice(preEligible, affect, hitCore, availableCores, availableTimeAllProcs, 
                                        allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, false);
                sacrificeF = getSacrifice(preEligible, affectF, hitCore, availableCores, availableTimeAllProcs, 
                                        allocHistory, currentExe, history_level1, history_level2, history_level3, currentTime, true);
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
        // System.out.println("compare future cnt: " + futureSac3);
        return hasAlloc;
	}

    // SAC + LCIF
	private Pair<Node, Integer> setPartition2(List<DirectedAcyclicGraph> dags, Map<Node, List<Pair<Integer, Long>>> SUT, Map<Node, List<Pair<Integer, Long>>> sacrifice, Map<Node, List<Pair<Integer, Long>>> sacrificeF, List<Node> allocNodes,
                                                List<Integer> allocProcs, List<List<Node>> allocHistory, List<List<Node>> fullAllocHistory,
                                                List<Node> preEligible, List<Integer> procs, List<Integer> cores, long[] availableTimeAllProcs, long time, boolean lcif,
                                                List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3) {
        
        Node nToAlloc = null; Integer core = -1;
        Long maxValue = Long.MIN_VALUE;
        Long minValueSac = (long)0; Long minValueSU = (long)0; Long minValueF = (long)0;
        for (Entry<Node, List<Pair<Integer, Long>>> entry : SUT.entrySet()) {
            Node n = entry.getKey();
            if (!allocNodes.contains(n)){
                List<Pair<Integer, Long>> sutList = entry.getValue();
                List<Pair<Integer, Long>> sacList = sacrifice.get(n);
                List<Pair<Integer, Long>> sacListF = sacrificeF.get(n);
                for (int i = 0; i < sutList.size(); i++){
                    if (!allocProcs.contains(sutList.get(i).getFirst())){
                        if (sutList.get(i).getSecond() - sacList.get(i).getSecond() - sacListF.get(i).getSecond() > maxValue){
                            
                            maxValue = sutList.get(i).getSecond() - sacList.get(i).getSecond() - sacListF.get(i).getSecond();
                            nToAlloc = n;
                            core = sutList.get(i).getFirst();
                            minValueF = sacListF.get(i).getSecond();
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
            long sut = 0; long sac = 0; long sacF = 0;
            if (!allocProcs.contains(proc)){
                sac = sacrifice.get(nToAlloc).get(i).getSecond();
                sut = SUT.get(nToAlloc).get(i).getSecond();
                sacF = sacrificeF.get(nToAlloc).get(i).getSecond();
                if (sut - sac - sacF == maxValue){
                    candidateC.add(proc);
                }
            
            }
        }

        long max = Long.MIN_VALUE;
        if (candidateC.size() > 1){
            for (int i = 0; i < candidateC.size(); i++){
                Integer c = candidateC.get(i);
                Integer index = procs.indexOf(c);
                long sut = 0; long sac = 0; long sacF = 0;
                sac = sacrifice.get(nToAlloc).get(index).getSecond();
                sut = SUT.get(nToAlloc).get(index).getSecond();
                sacF = sacrificeF.get(nToAlloc).get(index).getSecond();
                long recencyFree = getRecencyFree(c, allocHistory, procs, history_level1, history_level2, history_level3);
                if (recencyFree > max){
                    max = recencyFree;
                    core = c;
                    minValueSac = sac; minValueSU = sut; minValueF = sacF;
                }
            }
        }

        if (nToAlloc == null || core == -1) {
            System.err.println("SimpleCacheAware.getIndexOfMaximum(): Cannot find the max value!");
            System.exit(-1);
        }

        Integer cache = nToAlloc.crp.computeET(-1, history_level1, history_level2, history_level3, nToAlloc,
                            core, true, 0,0, false).getSecond(); 
        boolean delay1 = (cache < 3) && (maxValue <= 0);
        boolean delay2 = (cache >= 3) && (minValueF + minValueSac >= 0);
        boolean delay3 = (time + nToAlloc.getWCET()) <= (nToAlloc.release + Utils.getDagByIndex(dags, nToAlloc.getDagID(), nToAlloc.getDagInstNo()).sched_param.getPeriod());
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
        boolean delay = (delay1 || delay2) && delay3 && delay4;
        delay = false;
        if (!delay){
            return new Pair<Node, Integer>(nToAlloc, core);
        }else{
            nToAlloc.delayCnt++;
            List<Integer> tmp = new ArrayList<>(procs);
            tmp.removeAll(allocProcs);
            nToAlloc.notFitCore.addAll(tmp);
            delayCnt3++;
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

    private Pair<Integer, Long> findMaxValueKeyInMap(Map<Integer, Long> map, Integer core, List<Integer> availableP) {
        Integer maxKey = null;
        Long maxValue = Long.MIN_VALUE;

        for (Entry<Integer, Long> entry : map.entrySet()) {
            //跳过被占的核与非空闲的核
            if (entry.getKey() == core || !availableP.contains(entry.getKey())){
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
        Map<Integer, Long> speedUpTable = new LinkedHashMap<>();
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
    private Long getSUSac(Node n, Integer core, List<Node> affect, List<Integer> availableP, Map<Node, HitCoreNew> hitCore, 
            List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
        
        Long sum = (long) 0;
        Long max = Long.MIN_VALUE;
        for (int i = 0; i < affect.size(); i++){
            // if (n == affect.get(i)){
            //     continue;
            // }
            HitCoreNew tmp = hitCore.get(affect.get(i));
            List<Integer> coreList = tmp.getCoreList();
            Long rawSU = (long)0;
            Map<Integer, Long> SUT = getSUT(affect.get(i), coreList, history_level1, history_level2, history_level3);
            if (coreList.contains(core)){
                rawSU = SUT.get(core);
            }
            Pair<Integer, Long> pair = findMaxValueKeyInMap(SUT, core, availableP);

            //rawSU < maxSU -> no sacrifice
            if (n == affect.get(i)){
                //sum += (rawSU < pair.getSecond() ? pair.getSecond() - rawSU : 0);
                continue;
            }
            if (rawSU > pair.getSecond() && rawSU - pair.getSecond() > max){
                max = rawSU - pair.getSecond();
            }
            //if core is not available, predict the Node n will be allocated to the core with MSF
        }
        sum += (max == Long.MIN_VALUE ? 0 : max);
        return sum;
    }

    // private Long getSUSacForFutureNodes(Node n, Integer core, List<Node> affect, Node[] currentExe, 
    //         List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, long currentTime){

    //     Long max = Long.MIN_VALUE;
    //     List<Integer> coreList = new ArrayList<>();
    //     coreList.add(core);
    //     Map<Integer, Long> SUT = getSUT(n, coreList, history_level1, history_level2, history_level3);
    //     Long et_n = (long)n.getWCET() - SUT.get(core);
    //     long nextTime = currentTime + et_n;
    //     List <Node> future = getFutureNodes(currentExe, nextTime, affect, history_level1);
    //     for (int i = 0; i < future.size(); i++){
    //         Node futureNode = future.get(i);
    //         long affectedTime1 = futureNode.crp.computeET(-1, history_level1, history_level2,
    //                         history_level3, futureNode, core, true, et_n, 0,false).getFirst().getFirst();
    //         long affectedTime2 = futureNode.crp.computeET(-1, history_level1, history_level2, history_level3, futureNode,
    //                         core, true, 0,0, false).getFirst().getFirst(); 
    //         long affectedTime = affectedTime1 - affectedTime2;

    //         //affectedTime = affectedTime < 0 ? 0 : affectedTime;
    //         if (affectedTime < 0) {
    //             System.err.println("CacheAwareAlloc.setPartition(): the affected time is less than 0!");
    //             System.exit(-1);
    //         }
    //         if (affectedTime > max){
    //             max = affectedTime;
    //         }
    //     }
    //     return max == Long.MIN_VALUE ? 0 : max;
    // }

    private Long getSUSacForFutureNodes(Node n, Integer core, List<Node> future, Node[] currentExe, 
            List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3, long currentTime){

        Long max = Long.MIN_VALUE;
        List<Integer> coreList = new ArrayList<>();
        coreList.add(core);
        Map<Integer, Long> SUT = getSUT(n, coreList, history_level1, history_level2, history_level3);
        Long et_n = (long)n.getWCET() - SUT.get(core);
        for (int i = 0; i < future.size(); i++){
            Node futureNode = future.get(i);
            long affectedTime1 = futureNode.crp.computeET(-1, history_level1, history_level2,
                            history_level3, futureNode, core, true, et_n, 0,false).getFirst().getFirst();
            long affectedTime2 = futureNode.crp.computeET(-1, history_level1, history_level2, history_level3, futureNode,
                            core, true, 0,0, false).getFirst().getFirst(); 
            long affectedTime = affectedTime1 - affectedTime2;

            //affectedTime = affectedTime < 0 ? 0 : affectedTime;
            if (affectedTime < 0) {
                System.err.println("CacheAwareAlloc.setPartition(): the affected time is less than 0!");
                System.exit(-1);
            }
            if (affectedTime > max){
                max = affectedTime;
            }
        }
        return max == Long.MIN_VALUE ? 0 : max;
    }

    private Map<Node, List<Pair<Integer, Long>>> getSacrifice(List<Node> readyNodes, List<Node> affect, Map<Node, HitCoreNew> hitCore, List<Integer> availableCores, 
            long[] availableTimeAllProcs, List<List<Node>> allocHistory, Node[] currentExe, List<List<Node>> history_level1, List<List<Node>> history_level2, 
            List<Node> history_level3, long currentTime, boolean isFuture){
        
        Map<Node, List<Pair<Integer, Long>>> sacrifice = new LinkedHashMap<>();
        for (int i = 0; i < readyNodes.size(); i++){
            //sacrifice.add(new ArrayList<>());
            Node n = readyNodes.get(i);
            List<Pair<Integer, Long>> sacList = new ArrayList<>();
            //List<Integer> coreList = nHitCore.getCoreList();
            List<Integer> coreList = new ArrayList<>(availableCores);
            for (int j = 0; j < coreList.size(); j++){
                Integer core = coreList.get(j);
                Long metric = (long)0;
                if(isFuture){
                    metric = getSUSacForFutureNodes(n, core, affect, currentExe, history_level1, history_level2, history_level3, currentTime);
                    // if (metric > 0){
                    //     futureSac3++;
                    // }
                }else{
                    metric = getSUSac(n, core, affect, availableCores, hitCore, history_level1, history_level2, history_level3);
                }
                sacList.add(new Pair<Integer, Long>(core, metric));
            }
            if(sacList.size() > 0){
			    sacrifice.put(n, sacList);
            }
        }
        
        return sacrifice;
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
}

