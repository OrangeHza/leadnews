package cn.whu.article.feign;

import cn.whu.apis.article.IArticleClient;
import cn.whu.article.service.ApArticleService;
import cn.whu.model.article.dtos.ArticleDto;
import cn.whu.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ArticleClient implements IArticleClient {

    @Resource
    private ApArticleService apArticleService;

    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) {
        return apArticleService.saveArticle(dto);
    }
}
