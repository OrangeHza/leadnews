package cn.whu.schedule.feign;

import cn.whu.apis.schedule.IScheduleClient;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.schedule.dtos.Task;
import cn.whu.schedule.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class ScheduleClient implements IScheduleClient {

    @Resource
    private TaskService taskService;

    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id long
     */
    @Override
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) {
        long taskId = taskService.addTask(task);
        return ResponseResult.okResult(taskId);
    }

    /**
     * 取消任务
     *
     * @param taskId@return 取消成功还是失败 boolean
     */
    @Override
    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId) {
        boolean b = taskService.cancelTask(taskId);
        return ResponseResult.okResult(b);
    }

    /**
     * 按照类型和优先级拉取任务
     * 类型+优先级 -》 确定任务队列
     *
     * @param type
     * @param priority
     * @return Task  返回该任务，以便执行
     */
    @Override
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type,@PathVariable("priority") int priority) {
        Task task = taskService.poll(type, priority);
        return ResponseResult.okResult(task);
    }
}
