package cn.whu.wemedia.controller.v1;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.wemedia.service.WmChannelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/channel")
public class WmChannelController {

    @Resource
    private WmChannelService wmChannelService;

    @GetMapping("channels")
    public ResponseResult findAll() {
        return wmChannelService.findAll();
    }
}
