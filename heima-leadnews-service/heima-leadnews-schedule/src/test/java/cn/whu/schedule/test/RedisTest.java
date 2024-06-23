package cn.whu.schedule.test;

import cn.whu.common.redis.CacheService;
import cn.whu.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    private CacheService cacheService;

    @Test
    public void testList1(){
        // 在list的左边添加元素
        cacheService.lLeftPush("list_001","hello,redis1");//点进去看一下api的封装就是往list头部插入
        cacheService.lLeftPush("list_001","hello,redis2");//点进去看一下api的封装就是往list头部插入
    }

    @Test
    public void testList2(){
        // 在list的右边获取元素，并删除
        String list001 = cacheService.lRightPop("list_001");
        System.out.println(list001); // hello,redis1
    }


    @Test
    public void testZset1(){
        // 添加数据到zset中 有分值的
        cacheService.zAdd("zset_key_001","hello zset 001",1000);
        cacheService.zAdd("zset_key_001","hello zset 002",8888);
        cacheService.zAdd("zset_key_001","hello zset 003",7777);
        cacheService.zAdd("zset_key_001","hello zset 004",9999);
    }

    @Test
    public void testZset2(){
        // 按照分值获取数据
        // 获取分值在0~8888内的元素，且(应该自动是)按照分值升序排列
        Set<String> zsetKey001 = cacheService.zRangeByScore("zset_key_001", 0, 8888);
        System.out.println(zsetKey001);
        // [hello zset 001, hello zset 003, hello zset 002]
    }

}
