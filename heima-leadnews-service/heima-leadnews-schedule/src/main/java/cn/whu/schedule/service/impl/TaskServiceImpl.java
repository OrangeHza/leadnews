package cn.whu.schedule.service.impl;

import cn.whu.common.constants.ScheduleConstants;
import cn.whu.common.redis.CacheService;
import cn.whu.model.schedule.dtos.Task;
import cn.whu.model.schedule.pojos.Taskinfo;
import cn.whu.model.schedule.pojos.TaskinfoLogs;
import cn.whu.schedule.service.TaskService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.whu.schedule.mapper.TaskinfoLogsMapper;
import cn.whu.schedule.mapper.TaskinfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    // 访问db的两个mapper
    @Resource
    private TaskinfoMapper taskinfoMapper;

    @Resource
    private TaskinfoLogsMapper taskinfoLogsMapper;

    // 操作redis
    @Resource
    private CacheService cacheService;

    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    public long addTask(Task task) {

        // 1. 添加任务到数据库中
        boolean success = addTaskToDb(task);
        if (!success) return -1; // 写db失败，直接返回-1失败

        // 2. 添加任务到redis中
        addTaskToCache(task);

        return task.getTaskId();
    }

    private void addTaskToCache(Task task) {
        // 任务类型+优先级  确定一个任务队列 list
        String key = task.getTaskType() + "_" + task.getPriority();
        long delay = 5 * 60 * 1000;//延迟时间 5min

        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            // 2.1 如果任务的执行时间<=当前时间，存入list (redis的list结构)
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= System.currentTimeMillis() + delay) {
            // 2.2 如果任务的执行时间>当前时间 && 小于等于预设时间(未来5分钟) 存入zset (redis的zset结构)
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
            // 分值就是任务执行时间的ms值
        }

    }

    /**
     * 添加任务到数据库中
     *
     * @param task
     * @return
     */
    private boolean addTaskToDb(Task task) {
        boolean flag = true;

        try {
            // 1. 保存任务表
            // 1.1 准备数据
            Taskinfo taskinfo = new Taskinfo();
            // 1)拷贝数据
            BeanUtils.copyProperties(task, taskinfo);
            // 2)特殊字段处理：执行时间的类型不一样，long->Date, 需要手动处理
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            // 1.2 写db
            taskinfoMapper.insert(taskinfo);

            // 设置一下taskId  task引用传递，可以返回到主调方
            task.setTaskId(taskinfo.getTaskId());

            // 2. 保存任务日志数据
            // 2.1 准备数据
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            // 1）拷贝数据
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            // 2）特殊字段处理
            taskinfoLogs.setVersion(1); // 乐观锁版本号
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED); // 初始化(init)状态0
            // 2.2 写DB
            taskinfoLogsMapper.insert(taskinfoLogs);
        } catch (BeansException e) {
            flag = false;
            log.info("TaskServiceImpl-addTaskToDb exception task.id:{}", task.getTaskId(), e);
            e.printStackTrace();
        }

        return flag;
    }

    /*---------------------------删除任务-----------------------------*/

    /**
     * 取消任务
     *
     * @param taskId
     * @return 取消成功还是失败
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;

        // 删除任务，更新任务日志 （taskinfo表删除一条记录  taskinfo_logs表更新一条记录）
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);
        // 删除redis需要task的两个字段找到key，以及执行时间判断在哪里
        // 所以更新完db干脆直接返回task

        // 删除redis的数据(任务记录)
        if (task != null) {
            removeTaskFromCache(task);
            flag = true;
        }

        return flag;
    }

    /**
     * 删除redis中的数据 (就是那条任务记录)
     *
     * @param task
     */
    private void removeTaskFromCache(Task task) {
        // 任务类型+优先级  确定一个任务队列 list
        String key = task.getTaskType() + "_" + task.getPriority();

        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            // 删除根据value来删的 key只是找到了那个任务队列
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        } else {
            // 注意这里没有index参数了 (因为set不会重复的)
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }
    }

    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            // 删除db表-taskinfo 记录
            taskinfoMapper.deleteById(taskId);
            // 更新db表-taskinfo_logs 记录
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            // 返回刚删除的task数据
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (BeansException e) {
            log.error("task cancel exception taskId={}", taskId);
            e.printStackTrace();
        }
        return task;
    }


    /*-----------------------------拉取任务----------------------------------*/

    /**
     * 按照类型和优先级拉取任务
     * 类型+优先级 -》 确定任务队列
     *
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;

        try {
            // 1. 从redis拉取数据
            String key = type + "_" + priority;
            // 待消费的任务只能在list中
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(taskJson)) {
                task = JSON.parseObject(taskJson, Task.class);

                // 2. 修改db数据
                // 删除任务  日志状态修改为已执行
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("TaskServiceImpl.poll task error taskType={},taskPriority={}", type, priority);
            e.printStackTrace();
        }

        return task;
    }

    /**
     * 未来数据定时刷新
     *
     * @Scheduled注解就是任务调度注解 括号内容配置的含义就是每分钟执行1次
     * @Scheduled修饰的定时方法必须是无参且无返回值的方法
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 30 * 1000);//30s
        // 这样就轻易实现了互斥了
        if (StringUtils.isNotBlank(token)) {
            log.info("未来数据定时刷新---定时任务");

            // 获取所有未来数据的keys  (就zset未来任务所有队列名称)
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

            for (String futureKey : futureKeys) {
                // 获取当前任务到list执行队列后的key
                String topicKey = ScheduleConstants.TOPIC + futureKey.substring("future_".length());
                // futureKey: future_100_50
                // topicKey: topic_100_50

                // 按照key和分值查询符合条件的数据
                // 0~当前时间的分数范围内查找  其实就是查(futureKey队列中)小于当前时间的记录
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                // 同步数据 (futureKey队的数据)
                if (!tasks.isEmpty()) {
                    // 将数据tasks，从futureKey，移动到，topicKey
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    log.info("成功地将 {} 刷新到 {}, 本次共刷新 {} 个任务", futureKey, topicKey, tasks.size());
                }
            }
        }

    }

    /**
     * 数据库任务定时同步到redis中
     */
    @PostConstruct // 微服务启动时会立即执行一次  (防止服务挂掉后重启不能立即同步)
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void reloadData() {
        // 不用setNX防止并发抢占吗？

        // 清理缓存中的数据 list  zset  (db里面重新同步最近的数据到redis 原来的redis缓存可以都不要了)
        clearCache();

        // 查询符合条件的任务
        // 先获取5分钟后的时间实例
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5); // calendar类很方便就能实现
        // long ms = calendar.getTimeInMillis(); // ms值
        // Date date = calendar.getTime(); // date日期值
        // 把任务添加到redis
        List<Taskinfo> taskinfoList = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery()
                .lt(Taskinfo::getExecuteTime, calendar.getTime())
        );
        // 把任务添加到redis
        if (taskinfoList != null && taskinfoList.size() > 0) { // !!! 安全性呀
            for (Taskinfo taskinfo : taskinfoList) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }

        log.info("重新清空redis缓存，同步db数据到redis,本次共同步 {} 条数据", taskinfoList.size());

    }

    /**
     * 清理缓存(redis)中的数据
     */
    public void clearCache() {
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }


}
