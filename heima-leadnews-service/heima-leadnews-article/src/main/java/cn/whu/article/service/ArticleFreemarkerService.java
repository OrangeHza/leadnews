package cn.whu.article.service;

import cn.whu.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传
     *
     * @param apArticle
     * @param content
     */
    public void buildArticleToMinIo(ApArticle apArticle, String content);

}
