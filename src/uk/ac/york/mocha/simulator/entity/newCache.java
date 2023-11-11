package uk.ac.york.mocha.simulator.entity;

import java.io.Serializable;
import java.util.HashMap;

public class newCache implements Serializable {

    // 总容量
    public int capacity;
    public int ways;

    /*
     * hashmap
     * key = dag_id * 1000 + node_id
     * value = cache ways in used
     */
    HashMap<Integer, Long> read_only;// 查询cost用的是edge的起点key，也就是parent id
    HashMap<Integer, Long> read_write;
    HashMap<Integer, Integer> cnt_depend;

    public newCache(int ways, int capacity) {
        this.ways = ways;
        this.capacity = capacity;
        this.read_only = new HashMap<Integer, Long>();
        this.read_write = new HashMap<Integer, Long>();
        this.cnt_depend = new HashMap<Integer, Integer>();
    }

    public long find_rest_cap() {
        /*
         * 查询cache中剩余的容量
         */
        long used = 0;

        for (Long value : read_write.values()) {
            used += value;
        }
        for (Long value : read_only.values()) {
            used += value;
        }

        if (ways < used) {
            System.out.println("capacity is less than used!!!");
            System.exit(-1);
        }

        return ways - used;
    }

    /*
     * 节点完成执行后，转为read only cache
     * notes: 节点完成执行后调用,n为执行完成的节点
     */
    public void trans_read_only(Node n) {
        int key = n.getDagID() * 1000 + n.getId();
        /*
         * 更新父辈的read only是否可以释放
         */
        for (Node parent : n.getParent()) {
            int key_par = parent.getDagID() * 1000 + parent.getId();
            cnt_depend.put(key_par, cnt_depend.get(key_par) - 1);
            if (cnt_depend.get(key_par) == 0) {
                read_only.remove(key_par);
            }
        }

        // 节点自身的 read_write 转为 read only,并更新计数器
        read_only.put(key, read_write.get(key));
        read_write.remove(key);
        cnt_depend.put(key, n.getChildren().size());
    }

    /*
     * 申请read write cache
     */
    public void apply_read_write(long apply, Node n) {
        // 计算能分配的剩余cache (apply is data size)
        long require = (apply + capacity - 1) / capacity;
        long rest = find_rest_cap();
        long allo = Math.min(require, rest);

        int key = n.getDagID() * 1000 + n.getId();

        /*
         * 记录进read_write
         */
        read_write.put(key, allo);

        // 更新总capacity
        ways -= allo;
    }
}
