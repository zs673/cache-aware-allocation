package uk.ac.york.mocha.simulator.allocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;
import org.python.antlr.PythonParser.list_for_return;
import org.python.antlr.op.In;

import uk.ac.york.mocha.simulator.entity.DirectedAcyclicGraph;
import uk.ac.york.mocha.simulator.entity.Node;
import uk.ac.york.mocha.simulator.entity.Node.NodeType;
import uk.ac.york.mocha.simulator.parameters.SystemParameters;
import uk.ac.york.mocha.simulator.simulator.Utils;
import uk.ac.york.mocha.simulator.generator.HitCore;


public class OnlineYHX extends AllocationMethods {

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
		if (readyNodes.get(0).getType() == NodeType.SOURCE){
			System.out.println("A new instance starts");
		}
        Map<Node, HitCore> hitCore = getHitCores(readyNodes, availableCores, availableTimeAllProcs, history_level1, history_level2, history_level3, allocHistory, currentTime, lcif);

		/*
		 * Sort ready nodes list by FPS+WF, take first procNum nodes to allocate.  Order nodes by 1) its DAG priority and 2) its WCET.
		 */
		readyNodes.sort((c1, c2) -> Utils.compareNodeForYHX(dags, c1, c2, hitCore));

		List<Node> preEligible = new ArrayList<>();
		for (int i = 0; i < availableCores.size(); i++) {
			if (readyNodes.size() == i)
				break;
			preEligible.add(readyNodes.get(i)); //找readyNode和空闲核的最小值数目个readyNodes
		}

		List<Integer> availableP = new ArrayList<>(availableCores);

		// List<List<Long>> speedUpTable = new ArrayList<>();
		// //计算SUT
		// for (Node n : preEligible) {
		// 	List<Long> ETdrop = new ArrayList<>();

		// 	for (int i = 0; i < history_level1.size(); i++) {
		// 		int proc = i;
		// 		if (availableP.contains(proc)) {
		// 			/*
		// 			 * Speed up by ABSOLUTE value
		// 			 */
		// 			long WCET = n.getWCET();
		// 			long realET = n.crp
		// 					.computeET(-1, history_level1, history_level2, history_level3, n, proc, true, 0, 0,false)
		// 					.getFirst().getFirst();//computeET返回三个值，第一个是model估计的ET，第二个是err，第三个是命中第几层缓存
		// 			long speedup = WCET - realET;

		// 			ETdrop.add(speedup);
		// 		}
		// 	}

		// 	speedUpTable.add(ETdrop);
		// }

		List<Integer> allocProcs = new ArrayList<>();
		List<Node> allocNodes = new ArrayList<>();
		//空闲core的history_level1
		List<List<Node>> historyCut = new ArrayList<>();
		for (int i = 0; i < history_level1.size(); i++) {
			if (availableP.contains(i))
				historyCut.add(history_level1.get(i));
		}
		//空闲core的分配历史
		List<List<Node>> allocHistoryCut = new ArrayList<>();
		for (int i = 0; i < allocHistory.size(); i++) {
			if (availableP.contains(i))
				allocHistoryCut.add(allocHistory.get(i));
		}

		for (int k = 0; k < availableP.size(); k++) {
			if (k >= preEligible.size())
				break;//没那么多node分到多的核上
			
			Node n = preEligible.get(k);
			if (allocNodes.contains(n)){
				continue;
			}
			// Pair<Integer, Integer> p = setPartition(speedUpTable, allocNodes, allocProcs, allocHistoryCut, allocHistory,
			// 		preEligible, availableP, availableTimeAllProcs, currentTime, lcif, history_level1, history_level2,
			// 		history_level3);
			Integer core = setPartitionForYHX(n, hitCore, allocNodes, allocProcs, allocHistoryCut, allocHistory,
			preEligible, availableP, availableTimeAllProcs, currentTime, lcif, history_level1, history_level2,
			history_level3);

			n.partition = core;

			allocNodes.add(n);
			allocProcs.add(core);

			localRunqueue.get(n.partition).add(n);//加到localRunqueue
			allocHistory.get(n.partition).add(n);
			// Node n = preEligible.get(p.getFirst().intValue());

			// n.partition = availableP.get(p.getSecond().intValue());

			// allocNodes.add(p.getFirst().intValue());
			// allocProcs.add(p.getSecond().intValue());

			// localRunqueue.get(n.partition).add(n);//加到localRunqueue
			// allocHistory.get(n.partition).add(n);
		}
		//从readyNode移走已分配的结点
		for (int i = 0; i < readyNodes.size(); i++) {
			if (readyNodes.get(i).partition > -1) {
				readyNodes.remove(i);
				i--;
			}
		}

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
							Set<Integer> filteredLevel2 = level2HitCore.stream().filter(element -> availableCores.contains(element) && !level1HitCore.contains(element)).collect(Collectors.toSet());
							level2HitCore.clear();
							level2HitCore.addAll(filteredLevel2);

							level3HitCore.addAll(IntStream.rangeClosed(0, history_level1.size() - 1).boxed().collect(Collectors.toSet()));
							Set<Integer> filteredLevel3 = level3HitCore.stream().filter(element -> availableCores.contains(element) && !level2HitCore.contains(element) && !level1HitCore.contains(element)).collect(Collectors.toSet());
							level3HitCore.clear();
							level3HitCore.addAll(filteredLevel3);
                            break;
                        case 2:
							level2HitCore.add(j);
							level3HitCore.addAll(IntStream.rangeClosed(0, history_level1.size() - 1).boxed().collect(Collectors.toSet()));
							Set<Integer> _filteredLevel3 = level3HitCore.stream().filter(element -> availableCores.contains(element) && !level2HitCore.contains(element) && !level1HitCore.contains(element)).collect(Collectors.toSet());
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
        
	/*
	 * （1）和recency最小的不同cluster里面recency最小的（是不是全部里面第二小的 不一定 可能第二小的和上一个instance还是一个cluster） --- 维护一个recency table，排序一下，选择和最小的不一个cluster的
	 * （2）没有造成核上已分配任务的L2 recency miss or 距离L2 miss的阈值最远的，试试哪个好
	 * （3）为了多命中几个cluster，是不是要牺牲第二个instance（实现第一点好像就能，但后面只命中一个L2时还是会选择miss，违背初衷，还是要为第二个instance单独设立分配方式。其他的instance在只命中一个cluster的时候选择该cluster，第二个instance选择miss）
	 * （4）想一下这样有没有实现初衷(优先级那里倒序能不能实现让步，怎么样实现让步)
	 * 	(5) todo:判断是否命中多个cluster 以及分instance分配（分类讨论不同instance、是否只命中一个cluster） done
	 * （6）todo: 优先级怎么考虑争用（动态？不同readyNodes、不同可用核）
	 * （7）todo: 分配一个核之后更新其他结点的可用核  done
	 * （8）todo: allocProcs和allocNodes在setPartition中的使用，procsTime的使用  done
	 * （9）todo：debug simulator main
	 * （10）todo：experiment
	 * （11）todo：加system error
	 * （12）找crp怎么构建的
	 * （13）看看这样写能不能实现让步和多hit L2
	 * 	 
	 */
	private Integer setPartitionForYHX(Node n, Map<Node, HitCore> hitCore, List<Node> allocNodes,
			List<Integer> allocProcs, List<List<Node>> allocHistory, List<List<Node>> fullAllocHistory,
			List<Node> preEligible, List<Integer> procs, long[] availableTimeAllProcs, long time, boolean lcif,
			List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3) {
		
		HitCore oriNodeHitcore = hitCore.get(n);
		HitCore updateHitCore = oriNodeHitcore.deepCopy().excludCore(allocProcs);//排除已分配的procs

		Integer level2ClusterSize = history_level1.size() / history_level2.size();
		if (updateHitCore.isEmpty()){//hit no L1 and L2
			return rng.nextInt(procs.size());
		}
		if (updateHitCore.level1.size() > 1){//hit two L1 
			List<Entry<Integer, Long>> recencyTable = getRecencyTable(n, new ArrayList<Integer>(updateHitCore.level1), history_level1, history_level2, history_level3, 0);
			if (checkMultiCluster(n, updateHitCore.level1, history_level1, history_level2, history_level3)){
				/*
				 * 1)choose different cluster with min recency
				 * 2)choose max recency left for L2 and max speedup(speedup second, since almost same)
				 */
				Integer core = setPartitionRules(n, level2ClusterSize, recencyTable, allocHistory, history_level1, history_level2, history_level3);
				return core;
			}else{
				if (n.getDagInstNo() == 1){
					return setPartitionForIns2(n, updateHitCore, procs, allocProcs, allocHistory, history_level1, history_level2, history_level3);
				}else{
					return recencyTable.get(0).getKey();
				}
			}
			/*else只命中一个cluster
				if(n.getDagId.getInstanceNo == 2)
					return 核上已分配任务距离L2阈值最远的
				else
					return recency最小的

				*/
		}else if (updateHitCore.level1.size() == 1 && updateHitCore.level2.size() > 0){//hit L1 and L2
			Set<Integer> allHitCore = new HashSet<>();
			allHitCore.addAll(updateHitCore.level1);
			allHitCore.addAll(updateHitCore.level2);
			List<Entry<Integer, Long>> recencyTable = getRecencyTable(n, new ArrayList<Integer>(allHitCore), history_level1, history_level2, history_level3, 0);
			//if 命中多个cluster
			if (checkMultiCluster(n, allHitCore, history_level1, history_level2, history_level3)){
				Integer core = setPartitionRules(n, level2ClusterSize, recencyTable, allocHistory, history_level1, history_level2, history_level3);
				return core;
			}else{
				if (n.getDagInstNo() == 1){
					return setPartitionForIns2(n, updateHitCore, procs, allocProcs, allocHistory, history_level1, history_level2, history_level3);
				}else{
					return recencyTable.get(0).getKey();
				}
			}
		}else if (updateHitCore.level2.size() > 1){// hit two L2
			List<Entry<Integer, Long>> recencyTable = getRecencyTable(n, new ArrayList<Integer>(updateHitCore.level2), history_level1, history_level2, history_level3, 0);
			//if 命中多个cluster
			if (checkMultiCluster(n, updateHitCore.level2, history_level1, history_level2, history_level3)){
				Integer core = setPartitionRules(n, level2ClusterSize, recencyTable, allocHistory, history_level1, history_level2, history_level3);
				return core;
			}else{
				if (n.getDagInstNo() == 1){
					return setPartitionForIns2(n, updateHitCore, procs, allocProcs, allocHistory, history_level1, history_level2, history_level3);
				}else{
					return recencyTable.get(0).getKey();
				}
			}
		}else{
			if (n.getDagInstNo() == 1){
				return setPartitionForIns2(n, updateHitCore, procs, allocProcs, allocHistory, history_level1, history_level2, history_level3);
			}else{
				return updateHitCore.level2.iterator().next();
			}
		}
	}


	private Boolean checkMultiCluster(Node n, Set<Integer> level, List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
		int level2ClusterNum = history_level2.size();
		int level2ClusterSize = history_level1.size() / level2ClusterNum;
		Set<Integer> set = new HashSet<>();
		Iterator<Integer> iterator = level.iterator();
        while (iterator.hasNext()) {
            Integer element = iterator.next();
			if (set.contains(element / level2ClusterSize)){
				return true;
			}
			set.add(element / level2ClusterSize);
		}
		return false;
	}

	private Integer setPartitionRules(Node n, Integer level2ClusterSize, List<Entry<Integer, Long>> recencyTable, List<List<Node>> allocHistory, 
			List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
		
		Integer clusterIdx = recencyTable.get(0).getKey() / level2ClusterSize;

		Integer minMissIdx = Integer.MAX_VALUE;
		Long minMissCnt = Long.MAX_VALUE;
		for (int i = 1; i < recencyTable.size(); i++){
			int core = recencyTable.get(i).getKey();
			if (core / level2ClusterSize == clusterIdx)
				continue;
			
			Long cnt = getRecencyFree(n, core, allocHistory, history_level1, history_level2, history_level3);
			if (minMissCnt > cnt){
				minMissCnt = cnt;
				minMissIdx = i;
			}
		}
		return minMissIdx;
	}

	private Integer setPartitionForIns2(Node n, HitCore updateHitCore, List<Integer> procs,
			List<Integer> allocProcs, List<List<Node>> allocHistory,  
			List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
		//已经是空闲的了，排掉已分配的就行  其实updateHitCore已经排掉allocProcs
		List<Integer> candidateCore = procs.stream().filter(element -> !allocProcs.contains(element) && !updateHitCore.level1.contains(element) 
															&& !updateHitCore.level2.contains(element)).collect(Collectors.toList());
		
		Integer minMissIdx = Integer.MAX_VALUE;
		Long minMissCnt = Long.MAX_VALUE;
		for (int i = 0; i < candidateCore.size(); i++){
			int core = i;
			
			Long cnt = getRecencyFree(n, core, allocHistory, history_level1, history_level2, history_level3);
			if (minMissCnt > cnt){
				minMissCnt = cnt;
				minMissIdx = i;
			}
		}
		return minMissIdx;
	}

	private Long getRecencyFree(Node n, int core, List<List<Node>> allocHistory, List<List<Node>> history_level1, List<List<Node>> history_level2, List<Node> history_level3){
		long et_n = n.crp.computeET(-1, history_level1, history_level2, history_level3, n, core, true, 0, 0, false).getFirst().getFirst();
		List<Node> nodesInProc = allocHistory.get(core);

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
			long recencyDis = n.crp.computeRecency(-1, history_level1, history_level2, history_level3, n, i, true, additionalTime);
			rct.put(i, recencyDis);
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
}





