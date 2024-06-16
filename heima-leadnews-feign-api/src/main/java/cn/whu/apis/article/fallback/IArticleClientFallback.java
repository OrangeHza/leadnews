package cn.whu.apis.article.fallback;

import cn.whu.apis.article.IArticleClient;
import cn.whu.model.article.dtos.ArticleDto;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

/**
 * feign失败时的处理逻辑
 */
@Component // 必须被spring管理
public class IArticleClientFallback implements IArticleClient {
    // 熔断降级时执行的逻辑
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "获取数据失败");
    }
}
