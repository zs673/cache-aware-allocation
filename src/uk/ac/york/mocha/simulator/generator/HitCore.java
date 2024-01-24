package uk.ac.york.mocha.simulator.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HitCore {
    public int coreNum;
	public int cacheLevel;
	public int level2ClusterNum;
	public int level2ClusterSize;
    public int priority;

	public Set<Integer> level1 = new HashSet<>();
	public Set<Integer> level2 = new HashSet<>();
	public Set<Integer> level3 = new HashSet<>();

    public HitCore(Set<Integer> level1, Set<Integer> level2, Set<Integer> level3){
        this.level1.addAll(level1);
        this.level2.addAll(level2);
        this.level3.addAll(level3);
        this.priority = this.level2.size() + this.level1.size() * 2; // todo:考虑争用
    }

    public Boolean isEmpty(){
        return this.level1.size() == 0 && this.level2.size() == 0;
    }
}
