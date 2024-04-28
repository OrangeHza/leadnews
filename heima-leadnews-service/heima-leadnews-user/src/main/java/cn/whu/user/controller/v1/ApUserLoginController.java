package cn.whu.user.controller.v1;


import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.user.dtos.LoginDto;
import cn.whu.user.service.ApUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.xml.transform.Result;

@RestController // 包含@Controller和@ResponseBody(将返回对象转为json)两个注解了
@RequestMapping("/api/v1/login")
public class ApUserLoginController {

    @Resource
    private ApUserService apUserService;


    @PostMapping("/login_auth")
    public ResponseResult login(LoginDto dto) {
        return apUserService.login(dto);
    }



}
