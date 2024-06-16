package cn.whu.apis.article;

import cn.whu.apis.article.fallback.IArticleClientFallback;
import cn.whu.model.article.dtos.ArticleDto;
import cn.whu.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// feign远程接口 连接的是leadnews-article微服务
@FeignClient(value = "leadnews-article",fallback = IArticleClientFallback.class)
public interface IArticleClient {
    // 和controller一样写声明接口  会自动帮你调用到leadnews-article微服务下对应的controller接口
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto);
}
