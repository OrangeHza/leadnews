package cn.whu.wemedia.service.impl;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.pojos.WmChannel;
import cn.whu.wemedia.mapper.WmChannelMapper;
import cn.whu.wemedia.service.WmChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {
    /**
     * 查询所有频道
     *
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());//list方法就是查询当前表的所有记录
    }
}