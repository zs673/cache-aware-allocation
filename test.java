import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        // 创建一个HashMap
        Map<String, Integer> hashMap = new HashMap<>();

        // 插入键值对
        hashMap.put("A", 1);
        hashMap.put("B", 2);
        hashMap.put("C", 3);

        // 迭代HashMap的键值对
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }
}