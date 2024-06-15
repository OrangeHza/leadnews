package cn.whu.wemedia.hza;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HzaTest2 {
    /*public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("hello");list.add("world");list.add("java");
        System.out.println(String.join(",", list));
        System.out.println(StringUtils.join(list,","));
    }*/

    /*public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1000);list.add(2000);list.add(3000);
        //System.out.println(String.join(",", list));//报错
        System.out.println(StringUtils.join(list,","));
    }*/

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("aaa");list.add("bbb");list.add("ccc");list.add("ddd");
        List<String> subList = list.stream().limit(3).collect(Collectors.toList());
        System.out.println(subList);
    }
}
