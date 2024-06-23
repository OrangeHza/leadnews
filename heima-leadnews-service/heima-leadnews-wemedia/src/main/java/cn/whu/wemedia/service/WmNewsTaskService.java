package cn.whu.wemedia.service;

import java.util.Date;

public interface WmNewsTaskService {

    /**
     * 添加任务到延迟队列中
     * @param id  文章的id
     * @param publishTime  发布时间  可以作为任务的执行时间
     */
    public void addNewsToTask(Integer id, Date publishTime);


    /**
     * 消费延迟队列数据
     */
    public void scanNewsByTask();
}
