package cn.whu.article.service.impl;

import cn.whu.article.mapper.ApArticleConfigMapper;
import cn.whu.article.mapper.ApArticleContentMapper;
import cn.whu.article.mapper.ApArticleMapper;
import cn.whu.article.service.ApArticleService;
import cn.whu.model.article.dtos.ArticleDto;
import cn.whu.model.article.dtos.ArticleHomeDto;
import cn.whu.model.article.pojos.ApArticle;
import cn.whu.model.article.pojos.ApArticleConfig;
import cn.whu.model.article.pojos.ApArticleContent;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.AppHttpCodeEnum;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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

    // 操作配置表
    @Resource
    private ApArticleConfigMapper apArticleConfigMapper;

    // 操作内容表
    @Resource
    private ApArticleContentMapper apArticleContentMapper;

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

    /**
     * 保存app端文章(审核通过后的文章)
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        // 1. 检查参数
        if (dto == null || dto.getContent() == null || "".equals(dto.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 为了立刻拿到保存后的文章id,是需要一个apArticle实例对象的
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        // 2. 判断是否存在id
        if (dto.getId() == null) {
            // 2.1 不存在id, 保存：文章表、文章配置表、文章内容表
            // 1) 保存文章表
            //apArticleMapper.insert(dto); // 就是本service 直接save保存就行了
            save(apArticle); // 保存成功，id自动就有了

            // 2) 保存配置表 (需要默认配置 新增了带参构造器)
            ApArticleConfig config = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(config);

            // 3) 保存文章内容表
            ApArticleContent content = new ApArticleContent();
            content.setArticleId(apArticle.getId()); // 注意不是setId
            content.setContent(dto.getContent());
            apArticleContentMapper.insert(content);

        } else {
            // 2.2 存在id, 修改：文章表、文章内容表
            // 1) 修改文章表
            updateById(apArticle); // 最简单的还不会 （content自动就改了嘛  id也是那条记录的id）

            // 2) 修改文章内容表
            // 不查了，直接改吧
            apArticleContentMapper.update(null, Wrappers.<ApArticleContent>lambdaUpdate()
                    .eq(ApArticleContent::getArticleId, dto.getId())
                    .set(ApArticleContent::getContent, dto.getContent())
            );
        }

        // 3. 结果返回  文章的id (凭此能找到所有文章相关信息)
        return ResponseResult.okResult(apArticle.getId());
    }
}
