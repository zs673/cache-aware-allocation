package uk.ac.york.mocha.simulator.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.antlr.op.In;

import uk.ac.york.mocha.simulator.parameters.SystemParameters;

public class HitCoreNew implements Serializable {
    public int coreNum;
	public int cacheLevel;
	public int level2ClusterNum;
	public int level2ClusterSize;
    public double priority;

	public List<Integer> level1 = new ArrayList<>();
	public List<Integer> level2 = new ArrayList<>();
	public List<Integer> level3 = new ArrayList<>();

    public HitCoreNew(List<Integer> level1, List<Integer> level2, List<Integer> level3){
        this.level1.addAll(level1);
        this.level2.addAll(level2);
        this.level3.addAll(level3);
        this.priority = this.level2.size() + this.level1.size() * SystemParameters.alpha; 
		// this.priority = this.level2.size() * SystemParameters.alpha + this.level1.size();

    }

    public Boolean isEmpty(){
        return this.level1.size() == 0 && this.level2.size() == 0;
    }

	public Integer getSize(){
        return this.level1.size() + this.level2.size();
    }

	public List<Integer> getCoreList(){
		List<Integer> tmp = new ArrayList<>(this.level1);
		tmp.addAll(this.level2);
		return tmp;
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

    public HitCoreNew excludCore(List<Integer> allocProcs){
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
