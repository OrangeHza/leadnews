package cn.whu.article.service;

import cn.whu.model.article.dtos.ArticleDto;
import cn.whu.model.article.dtos.ArticleHomeDto;
import cn.whu.model.article.pojos.ApArticle;
import cn.whu.model.common.dtos.ResponseResult;
import com.baomidou.mybatisplus.extension.service.IService;

// 接口要实体类就行了  实现类需要Mapper和实体类
public interface ApArticleService extends IService<ApArticle> {
    // 一个load方法就行了，根据type不同实现不同的功能

    /**
     * 根据参数加载文章列表
     * @param dto
     * @param loadType 1：加载更多   2：加载最新
     * @return
     */
    ResponseResult load(ArticleHomeDto dto, Short loadType);

    /**
     * 保存文章(审核通过后的文章)
     * @param dto
     * @return
     */
    ResponseResult saveArticle(ArticleDto dto);

}
