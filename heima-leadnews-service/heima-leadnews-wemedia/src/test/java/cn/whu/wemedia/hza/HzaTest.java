package cn.whu.wemedia.hza;

import com.alibaba.fastjson.JSONArray;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HzaTest {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        list.add("java");
        String join = String.join(",", list);
        System.out.println(join);
    }

    @Test
    public void test(){
        String js = "[{\"name\": \"Alice\", \"age\": 30}, {\"name\": \"Bob\", \"age\": 25}]";
        List<Map> maps = JSONArray.parseArray(js, Map.class);
        System.out.println(maps);
    }
}
