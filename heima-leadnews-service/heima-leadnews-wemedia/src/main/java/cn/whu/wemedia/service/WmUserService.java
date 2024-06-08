package cn.whu.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.dtos.WmLoginDto;
import cn.whu.model.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmLoginDto dto);

}