package cn.whu.apis.schedule;


import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-schedule") // 也可以加个fallback吧
public interface IScheduleClient {

    /**
     * 添加任务
     * @param task   任务对象
     * @return       任务id long
     */
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) ;

    /**
     * 取消任务
     * @param task 任务对象
     * @return 取消成功还是失败 boolean
     */
    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId);

    /**
     * 按照类型和优先级拉取任务
     * 类型+优先级 -》 确定任务队列
     * @param type
     * @param priority
     * @return Task
     */
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type,@PathVariable("priority") int priority);


}
