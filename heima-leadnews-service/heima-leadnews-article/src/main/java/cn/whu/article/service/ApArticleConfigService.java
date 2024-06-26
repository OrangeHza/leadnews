package cn.whu.article.service;

import cn.whu.model.article.pojos.ApArticleConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface ApArticleConfigService extends IService<ApArticleConfig> {
    /**
     * 修改文章
     * @param map
     */
    void updateByMap(Map<String, Object> map);
}
