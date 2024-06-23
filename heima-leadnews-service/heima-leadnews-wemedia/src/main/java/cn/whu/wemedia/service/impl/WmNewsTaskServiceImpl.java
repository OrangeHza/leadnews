package cn.whu.wemedia.service.impl;

import cn.whu.apis.schedule.IScheduleClient;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.TaskTypeEnum;
import cn.whu.model.schedule.dtos.Task;
import cn.whu.model.wemedia.pojos.WmNews;
import cn.whu.utils.common.ProtostuffUtil;
import cn.whu.wemedia.service.WmNewsAutoScanService;
import cn.whu.wemedia.service.WmNewsService;
import cn.whu.wemedia.service.WmNewsTaskService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Resource
    private IScheduleClient scheduleClient;

    @Resource
    private WmNewsService wmNewsService;

    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 添加任务到延迟队列中
     *
     * @param id          文章的id
     * @param publishTime 发布时间  可以作为任务的执行时间
     */
    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {

        log.info("添加任务到到延迟服务中------begin");

        // 1. 封装task
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        // 参数比较麻烦，本来传id就行了，但是需要一个序列化对象
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        // 2. feign接口调用定时任务，添加任务到db和redis
        scheduleClient.addTask(task);

        log.info("添加任务到到延迟服务中------end");
    }

    /**
     * 消费延迟队列数据
     * 定时自动执行的任务
     */
    @Override
    @Scheduled(fixedRate = 1000) // 每秒执行一次
    public void scanNewsByTask() {

        log.info("文章审核---消费任务执行---begin---");

        ResponseResult responseResult = scheduleClient.poll(
                TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(),
                TaskTypeEnum.NEWS_SCAN_TIME.getPriority()
        );

        if (responseResult.getCode().equals(200) && responseResult.getData() != null) {
            // responseResult.getData()返回类型是T,强转不合适，用json转
            String jsonString = JSON.toJSONString(responseResult.getData());
            Task task = JSON.parseObject(jsonString, Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
            log.info("文章审核----文章id:{}", wmNews.getId());
        }

        log.info("文章审核---消费任务执行---end---");

    }
}
