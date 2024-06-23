package cn.whu.schedule.service;

import cn.whu.model.schedule.dtos.Task;

/**
 * 对外访问接口
 */
public interface TaskService {

    /**
     * 添加任务
     * @param task   任务对象
     * @return       任务id
     */
    public long addTask(Task task) ;

    /**
     * 取消任务
     * @param task 任务对象
     * @return 取消成功还是失败
     */
    public boolean cancelTask(long taskId);

    /**
     * 按照类型和优先级拉取任务
     * 类型+优先级 -》 确定任务队列
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type,int priority);

}