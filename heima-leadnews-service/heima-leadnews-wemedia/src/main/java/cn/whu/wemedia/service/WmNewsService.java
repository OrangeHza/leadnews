package cn.whu.wemedia.service;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.dtos.WmNewsDto;
import cn.whu.model.wemedia.dtos.WmNewsPageReqDto;
import cn.whu.model.wemedia.pojos.WmNews;
import com.baomidou.mybatisplus.extension.service.IService;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 根据条件，批量查询文章
     * @param dto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto dto);

    /**
     * 发布文章、修改文章、文章保存为草稿  的公用方法
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);
}