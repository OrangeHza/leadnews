package cn.whu.common.exception;


import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// 配置文件spring.factories里将此类配置为自动配置类了，项目启动时会自动创建实例并放到IOC容器中
// 后面的微服务只要引入common就有了全局异常了
@ControllerAdvice  //控制器增强类  异常都抛到controller  然后这里统一拦截所有controller异常并进行AOP增强
@Slf4j
public class ExceptionCatch {

    /**
     * 处理不可控异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class) // 拦截Exception类异常 返回对象json数据
    @ResponseBody // 返回对象转json
    public ResponseResult exception(Exception e){
        e.printStackTrace();
        log.error("catch exception:{}",e.getMessage());

        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }

    /**
     * 处理可控异常  自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(CustomException.class) // 拦截自定类异常，也就是CustomException类异常
    @ResponseBody // 返回的不是页面，而是数据  并自动将返回的对象转json
    public ResponseResult exception(CustomException e){
        log.error("catch exception:{}",e);
        return ResponseResult.errorResult(e.getAppHttpCodeEnum());
    }
}
