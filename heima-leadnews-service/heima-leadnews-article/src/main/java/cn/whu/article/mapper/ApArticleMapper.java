package cn.whu.article.mapper;

import cn.whu.model.article.dtos.ArticleHomeDto;
import cn.whu.model.article.pojos.ApArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {// 千万注意泛型是pojo，查的就是你呀

    /**
     * 加载文章列表 xml方式做的sql实现的  sql写好，这个方法就已经实现完毕了
     * @param dto
     * @param type 1：加载更多   2：加载最新
     * @return
     */
    public List<ApArticle> loadArticleList(ArticleHomeDto dto,Short type);
    // ApArticle 文章表(基本信息表)对应的 pojo
    // 因为涉及多表查询，所以需要自己写一个方法，然后自己写sql

}
