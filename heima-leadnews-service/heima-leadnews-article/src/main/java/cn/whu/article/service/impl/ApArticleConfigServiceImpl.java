package cn.whu.article.service.impl;

import cn.whu.article.mapper.ApArticleConfigMapper;
import cn.whu.article.service.ApArticleConfigService;
import cn.whu.model.article.pojos.ApArticleConfig;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@Transactional
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {

    /**
     * 修改文章
     *
     * @param map
     */
    @Override
    public void updateByMap(Map<String, Object> map) {
        Long articleId = (Long) map.get("articleId");
        Integer enable = (Integer) map.get("enable");// 1上架  0下架  大一点转换不报错
        // 直接修改文章上下架了
        update(Wrappers.<ApArticleConfig>lambdaUpdate()
                .eq(ApArticleConfig::getArticleId, articleId)
                .set(ApArticleConfig::getIsDown, enable == 0)
        );

        log.info("ApArticleConfigServiceImpl.updateByMap success articleId:{} enable:{}", articleId, enable);

    }
}
