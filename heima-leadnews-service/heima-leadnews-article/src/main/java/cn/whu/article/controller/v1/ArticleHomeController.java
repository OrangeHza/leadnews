package cn.whu.article.controller.v1;

import cn.whu.article.service.ApArticleService;
import cn.whu.model.article.dtos.ArticleHomeDto;
import cn.whu.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.whu.common.constants.ArticleConstants.LOADTYPE_LOAD_MORE;
import static cn.whu.common.constants.ArticleConstants.LOADTYPE_LOAD_NEW;

@RestController // @Controller 和 @ResponseBody
@RequestMapping("/api/v1/article")
public class ArticleHomeController {

    @Resource
    private ApArticleService apArticleService;

    /**
     * 加载首页
     * @param dto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        // 默认加载更多 前端会传递过来一个2063年的日期，DB查询<2063年的数据
        return apArticleService.load(dto, LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载更多
     * @param dto
     * @return
     */
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载最新
     * @param dto
     * @return
     */
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, LOADTYPE_LOAD_NEW);
    }

}
