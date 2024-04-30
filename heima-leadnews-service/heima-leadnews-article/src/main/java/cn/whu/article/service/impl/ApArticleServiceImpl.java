package cn.whu.article.service.impl;

import cn.whu.article.mapper.ApArticleMapper;
import cn.whu.article.service.ApArticleService;
import cn.whu.model.article.dtos.ArticleHomeDto;
import cn.whu.model.article.pojos.ApArticle;
import cn.whu.model.common.dtos.ResponseResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.whu.common.constants.ArticleConstants.*;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 自定义方法还是要注入Mapper的，BaseMapper里面没有
    @Resource
    private ApArticleMapper apArticleMapper;

    // 常量
    private final static short MAX_PAGE_SIZE = 50; // 应该是kconf动态热配置

    /**
     * 根据参数加载文章列表
     *
     * @param dto
     * @param loadType 1：加载更多   2：加载最新
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        log.info("文章列表请求参数: dto:{} type:{}", dto, type);
        // 1. 参数校验
        // 1.1 分页条数校验
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        // 分页值不超过50
        size = Math.min(size, MAX_PAGE_SIZE);
        dto.setSize(size);

        // 1.2 参数校验
        if (!type.equals(LOADTYPE_LOAD_MORE) && !type.equals(LOADTYPE_LOAD_NEW)) {
            type = LOADTYPE_LOAD_MORE; // 没有type时默认加载更多
        }

        // 1.3 频道参数校验
        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(DEFAULT_TAG); // 默认类型 推荐页面
        }

        // 1.4 时间参数校验 (没有值就默认填充当前时间)
        if (dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());
        if (dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());

        // 2. 查询DB数据
        List<ApArticle> articleList = apArticleMapper.loadArticleList(dto, type);

        // 3. 结果返回
        ArrayList<Long> ids = new ArrayList<>();
        for (ApArticle apArticle : articleList) {
            ids.add(apArticle.getId());
        }
        log.info("文章列表响应参数: articleList.size:{} articleList.ids:{}",
                articleList.size(),
                articleList.stream()
                        .map(ApArticle::getId)
                        .collect(Collectors.toList())
        );
        return ResponseResult.okResult(articleList);
    }
}
