package cn.whu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.user.dtos.LoginDto;
import cn.whu.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {
    /**
     * app端登录功能
     * @param dto
     * @return
     */
    public ResponseResult login(LoginDto dto);
}
