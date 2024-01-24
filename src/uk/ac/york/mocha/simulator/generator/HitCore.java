package uk.ac.york.mocha.simulator.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public synchronized HitCore deepCopy() {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);

			HitCore hitCore = (HitCore) ois.readObject();

			oos.flush();
			baos.flush();

			baos.close();
			oos.close();
			bais.close();
			ois.close();

			return hitCore;
		} catch (EOFException eof) {
			eof.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

    public HitCore excludCore(List<Integer> allocProcs){
        for (int i = 0; i < allocProcs.size(); i++){
            Integer proc = allocProcs.get(i);
            if (this.level1.contains(proc)){
                this.level1.remove(proc);
            }
            if (this.level2.contains(proc)){
                this.level2.remove(proc);
            }
            if (this.level3.contains(proc)){
                this.level3.remove(proc);
            }
        }
        return this;
    }
}
