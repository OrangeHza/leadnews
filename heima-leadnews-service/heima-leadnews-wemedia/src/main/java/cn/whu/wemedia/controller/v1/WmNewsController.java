package cn.whu.wemedia.controller.v1;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.dtos.WmNewsDto;
import cn.whu.model.wemedia.dtos.WmNewsPageReqDto;
import cn.whu.wemedia.service.WmNewsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Resource
    private WmNewsService wmNewsService;

    /**
     * 根据条件查询文章
     *
     * @param dto
     * @return
     */
    @PostMapping("list") // post请求的请求体中有json参数
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto) {//接收json参数
        return wmNewsService.findList(dto);
    }

    @PostMapping("submit") // post请求才有请求体   get请求只有简单url参数
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        return wmNewsService.submitNews(dto);
    }


}
