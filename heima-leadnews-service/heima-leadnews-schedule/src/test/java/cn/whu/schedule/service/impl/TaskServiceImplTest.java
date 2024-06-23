package cn.whu.schedule.service.impl;

import cn.whu.common.redis.CacheService;
import cn.whu.model.schedule.dtos.Task;
import cn.whu.schedule.ScheduleApplication;
import cn.whu.schedule.service.TaskService;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
class TaskServiceImplTest {

    @Resource
    private TaskService taskService;


    @Test
    void addTask() {
        Task task = new Task();
        task.setTaskType(105);
        task.setPriority(50);
        task.setParameters("task test".getBytes());
        task.setExecuteTime(System.currentTimeMillis());
        //task.setExecuteTime(System.currentTimeMillis()+500);
        //task.setExecuteTime(System.currentTimeMillis()+500000); // 超过5分钟 redis中就没有新记录了

        long taskId = taskService.addTask(task);
        System.out.println("taskId = " + taskId);

    }



    @Test
    public void cancelTask() {
        taskService.cancelTask(1802971828875091969l);
    }

    @Test
    public void poll() {
        Task task = taskService.poll(100, 50);
        System.out.println("task = " + task);
    }


    @Test
    void addTasks() {
        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(50);
        task.setParameters("task test".getBytes());

        for (int i = 101; i <= 105; i++) {
            task.setTaskType(i);
            task.setExecuteTime(System.currentTimeMillis() + 500);
            task.setTaskId(null);//写db时不能有id
            long taskId = taskService.addTask(task);
            System.out.println("taskId = " + taskId);
        }
    }

    @Resource
    private CacheService cacheService;

    @Test
    public void testZset() {
        // 模糊匹配语法
        Set<String> keys = cacheService.keys("future_*");
        System.out.println("keys = " + keys);

        Set<String> scan = cacheService.scan("future_*");
        System.out.println("scan = " + scan);

    }


    /*-------------------------------------------------*/


    //耗时4864
    @Test
    public  void testPipe1(){
        long start =System.currentTimeMillis();
        for (int i = 0; i <10000 ; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            // 只push到list中  redis的一个list中(一个队列中新增1w条数据)
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }
        System.out.println("耗时"+(System.currentTimeMillis()- start));
    }

    // 642毫秒
    @Test
    public void testPipe2(){
        long start  = System.currentTimeMillis();
        //使用管道技术
        List<Object> objectList = cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                for (int i = 0; i <10000 ; i++) {
                    Task task = new Task();
                    task.setTaskType(1001);
                    task.setPriority(1);
                    task.setExecuteTime(new Date().getTime());
                    redisConnection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
                }
                return null;
            }
        });
        System.out.println("使用管道技术执行10000次自增操作共耗时:"+(System.currentTimeMillis()-start)+"毫秒");
        // 使用管道技术执行10000次自增操作共耗时:642毫秒
    }
}