package cn.whu.wemedia.hza;

import java.util.ArrayList;

public class HzaTest {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        list.add("java");
        String join = String.join(",", list);
        System.out.println(join);
    }
}
