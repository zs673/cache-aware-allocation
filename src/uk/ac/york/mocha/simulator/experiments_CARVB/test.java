package uk.ac.york.mocha.simulator.experiments_CARVB;

public class test {
    public static void main(String[] args) {
        // 创建一个HashMap
        int[] nums = new int[]{-1,-100,3,99};
        Solution s = new Solution();
        s.rotate(nums, 2);
    }
}

class Solution {
    public void rotate(int[] nums, int k) {
        int n = nums.length;
        if (k <= 0 || n <= 0)   return;

        int cnt = gcd(n, k);
        for (int i = 0; i < cnt; i++){
            int cur = -1;
            int prev = nums[i];
            while (cur != i){
                int next = (cur + k) % n;
                int temp = nums[next];
                nums[next] = prev;
                prev = temp;
                cur = next;
            }
        }
        return;
    }

    public int gcd(int x, int y){
        return y > 0 ? gcd(y, x % y) : x;
    }
}