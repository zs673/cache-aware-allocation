import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class test {

	public static void main(String[] args) {
		// int rows = 3; // 列表的行数
        // int columns = 4; // 列表的列数

        // ArrayList<ArrayList<Integer>> list = new ArrayList<>();

        // // 生成随机整数并填充列表
        // for (int i = 0; i < rows; i++) {
        //     ArrayList<Integer> row = new ArrayList<>();
        //     for (int j = 0; j < columns; j++) {
        //         row.add(12); // 生成0到99之间的随机整数
        //     }
        //     list.add(row);
        // }

        // // 打印列表
        // for (ArrayList<Integer> row : list) {
        //     for (int num : row) {
        //         System.out.print(num + " ");
        //     }
        //     System.out.println();
        // }

		// ArrayList<Integer> tmp = list.get(0);
		// tmp.remove(0);
		

        // // 打印列表
        // for (ArrayList<Integer> row : list) {
        //     for (int num : row) {
        //         System.out.print(num + " ");
        //     }
        //     System.out.println();
        // }

            Set<String> set = new HashSet<String>();
            set.add("one");
            set.add("two");
            set.add("three");
            
            Iterator it = set.iterator();
            
            while(it.hasNext()){
                System.out.println(it.next());
            }

	}

}

