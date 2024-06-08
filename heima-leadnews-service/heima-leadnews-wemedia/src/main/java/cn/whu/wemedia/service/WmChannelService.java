package cn.whu.wemedia.service;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.pojos.WmChannel;
import com.baomidou.mybatisplus.extension.service.IService;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();
}