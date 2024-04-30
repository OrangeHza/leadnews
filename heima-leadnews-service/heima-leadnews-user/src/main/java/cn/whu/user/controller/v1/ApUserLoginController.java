package cn.whu.user.controller.v1;


import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.user.dtos.LoginDto;
import cn.whu.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController // 包含@Controller和@ResponseBody(将返回对象转为json)两个注解了
@RequestMapping("/api/v1/login")
@Api(value = "app端用户登录",tags = "app端用户登录")
public class ApUserLoginController {

    @Resource
    private ApUserService apUserService;

    @PostMapping("/login_auth")
    @ApiOperation("用户登录")
    public ResponseResult login(@RequestBody LoginDto dto) {//加上@RequestBody才能接收请求提的json参数，否则只能接受url参数
        return apUserService.login(dto);
    }
}
