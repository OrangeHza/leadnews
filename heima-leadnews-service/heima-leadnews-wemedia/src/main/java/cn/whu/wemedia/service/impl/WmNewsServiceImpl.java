package cn.whu.wemedia.service.impl;

import cn.whu.common.constants.WemediaConstants;
import cn.whu.common.exception.CustomException;
import cn.whu.model.common.dtos.PageResponseResult;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.AppHttpCodeEnum;
import cn.whu.model.wemedia.dtos.WmNewsDto;
import cn.whu.model.wemedia.dtos.WmNewsPageReqDto;
import cn.whu.model.wemedia.pojos.WmMaterial;
import cn.whu.model.wemedia.pojos.WmNews;
import cn.whu.model.wemedia.pojos.WmNewsMaterial;
import cn.whu.utils.thread.WmThreadLocalUtil;
import cn.whu.wemedia.mapper.WmMaterialMapper;
import cn.whu.wemedia.mapper.WmNewsMapper;
import cn.whu.wemedia.mapper.WmNewsMaterialMapper;
import cn.whu.wemedia.service.WmNewsAutoScanService;
import cn.whu.wemedia.service.WmNewsService;
import cn.whu.wemedia.service.WmNewsTaskService;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Resource
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Resource
    private WmMaterialMapper wmMaterialMapper;

    // 文章发布成功后 需要调自动审核文章
    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Resource
    private WmNewsTaskService wmNewsTaskService;

    /**
     * 根据条件，批量查询文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        // 1. 参数检查
        dto.checkParam();

        // 2. 条件查询
        // 2.1 分页条件 (页面，页大小)
        IPage iPage = new Page(dto.getPage(), dto.getSize());
        // 2.2 实际业务条件
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        // 所有条件都要提前判断是否有 安全性考虑
        // 1) 状态精确查询
        Short status = dto.getStatus();
        lqw.eq(status != null, WmNews::getStatus, status);
        // 2) 频道精确查询
        Integer channelId = dto.getChannelId();
        lqw.eq(channelId != null, WmNews::getChannelId, channelId);
        // 3) 时间范围查询
        Date date1 = dto.getBeginPubDate();
        Date date2 = dto.getEndPubDate();
        // 发布时间在 起止时间内
        lqw.between(date1 != null && date2 != null, WmNews::getPublishTime, date1, date2);
        // 4) 关键字的模糊查询
        String keyword = dto.getKeyword();
        lqw.like(StringUtils.isNotBlank(keyword), WmNews::getTitle, keyword);
        // 5) 查询当前登陆人的文章
        lqw.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());
        // 6) 按发布时间倒序排序
        lqw.orderByDesc(WmNews::getPublishTime);
        // 最后查询结果
        iPage = this.page(iPage, lqw); // 会直接作用到iPage 也会返回
        // 3. 返回结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) iPage.getTotal());
        responseResult.setData(iPage.getRecords());
        log.info("用户{} 查询文章列表:{}条", WmThreadLocalUtil.getUser().getId(), iPage.getRecords().size());
        return responseResult;
    }

    /**
     * 发布文章、修改文章、文章保存为草稿  的公用方法
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        // 0. 条件判断
        // dto本身以及内容不能为空
        if (dto == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 1. 判断是保存还是修改文章
        // 1.1 属性拷贝
        WmNews wmNews = new WmNews();
        // spring的工具类，属性相同且类型相同时，会将dto的值拷贝给wmNews
        BeanUtils.copyProperties(dto, wmNews);
        // 1.2 不同类型属性转换
        // 封面图片 list ---> string(逗号分隔)  【只是单独的封面图片】
        if (dto.getImages() != null && dto.getImages().size() > 0) {//写代码一定要严谨
            // [a.jpg,b.jpg,c.jpg] --> "a.jpg,b.jpg,c.jpg"
            wmNews.setImages(StringUtils.join(dto.getImages(), ","));
            // 注意下类型org.apache.commons.lang3.StringUtils
            // 相比String.join的好处，可以处理非String元素
        }
        // 1.3 自动类型需要单独处理一下，下面好识别
        // 前端传过来-1表示自动类型，但是db的type是unsigned，没有负数
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        // 1.4 执行保存或者更新
        saveOrUpdateWmNews(wmNews); // 逻辑可能比较复杂，自己写一个吧，自己写的越多越好

        // 2. 判断是否为草稿，若是草稿，则结束当前方法
        // ▲ 需求是：草稿不需要关联 图片素材和文章之间的关系，可以随便删除的，本来就是草稿嘛
        if (wmNews.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        // 3. 不是草稿：保存文章内容图片与素材的关系
        // 图片信息（urls）
        List<String> materials = extractUrlInfo(dto.getContent());
        // 根据url查询素材id   再中间表存储关系id-id
        saveRelativeInfoForContent(wmNews.getId(), materials);

        //4.不是草稿：保存文章封面图片与素材的关系 (文章封面可以有单独的一张或几张图片，需要单独关联一下)
        // 如果当前布局是自动，需要自动匹配封面图片(materials里拿 所以要传materials)
        saveRelativeInfoForCover(dto, wmNews, materials);//Cover封面的意思

        // 5. 【新增】 审核文章.  发布成功，调用方法自动审核文章 （配置的方法，做到异步调用执行）
        //wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        // 放到db里面，然后再由定时任务根据执行时间慢慢刷新到redis里
        wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * // 内容图片url分散在content字段的map里
     * // 封面图片在一个单独字段images里，最多3张，直接以逗号分隔存储了
     * 第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1,如果内容图片大于等于1，小于3单图  type 1
     * 2,如果内容图片大于等于3多图  type 3
     * 3,如果内容没有图片，无图  type 0
     *
     * @param dto
     * @param wmNews
     * @param materials
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        List<String> images = dto.getImages();

        //1. 如果当前封面类型为自动，则images为空  需要从content中提取图片设置封面类型的数据
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            // 1.1 设置封面images和封面类型type
            // materials就是文章内容(content)中提取的图片
            if (materials.size() >= 1 && materials.size() < 3) {
                // 单图封面
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else if (materials.size() > 3) {
                // 多图封面
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else {
                // 无图封面 (纯文本文章)
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
                // image保持为none就行了
            }
            if (images != null && images.size() > 0) { // 提前判断下，又忘了，唉
                wmNews.setImages(StringUtils.join(images, ","));
            }
            // 修改了wnNews的字段，所以更新下表 (本篇文章的一条记录)
            updateById(wmNews);
        }

        // 2. 保存文章和封面图片的关联关系 （还是wm_news_material表 只是type变了而已）
        if (images != null && images.size() > 0) {
            saveRelativeInfo(wmNews.getId(), images, WemediaConstants.WM_COVER_REFERENCE);
        }


    }

    /**
     * 保存文章和素材的关系到db中
     *
     * @param newsId
     * @param materials
     */
    private void saveRelativeInfoForContent(Integer newsId, List<String> materials) {
        // 抽取出来，保存主内容图片索引时要调，保存封面图片索引时也要调
        saveRelativeInfo(newsId, materials, WemediaConstants.WM_CONTENT_REFERENCE);
    }

    private void saveRelativeInfo(Integer newsId, List<String> materials, Short type) {
        // 0. 参数检查
        if (materials == null || materials.size() == 0) {
            // 这篇文章不包含任何素材 下面的操作均不需要进行了
            return;
        }

        // 1. 写好了的文章中的素材应该是已经上传好里的，直接拿着url去wm_material表中查id即可
        List<WmMaterial> dbMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery()
                .in(WmMaterial::getUrl, materials));
        List<Integer> materialIds = dbMaterials.stream()
                .map(WmMaterial::getId)
                .collect(Collectors.toList());

        // 2. 健壮性判断：判断素材是否有效，无效的素材是不能被关联的
        if (dbMaterials == null || dbMaterials.size() != materials.size()) {
            // 手动抛出异常  第一个功能:能够提示调用者素材失效了，  第二个功能:进行数据的回滚
            throw new CustomException(AppHttpCodeEnum.MATERIALS_REFERENCE_FAIL);
        }

        // 3. 批量保存：批量写db中间表wm_news_material
        wmNewsMaterialMapper.saveRelations(materialIds, newsId, type);
    }

    /**
     * 提取文章内容中的图片信息  就是每个图片的url
     *
     * @param content
     * @return
     */
    private List<String> extractUrlInfo(String content) {
        List<Map> maps = JSONArray.parseArray(content, Map.class);
        List<String> materials = maps.stream()
                .filter(map -> "image".equals(map.get("type")))
                .map(map -> (String) map.get("value"))
                .collect(Collectors.toList());
        return materials;
    }


    /**
     * 保存或修改文章
     *
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        // 0. 补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date()); // !!!!千万注意不是publishTime
        wmNews.setEnable((short) 1);//1默认值，代表上架
        if (wmNews.getId() == null) {
            // 1. 保存
            save(wmNews);
        } else {
            // 2. 修改
            // 2.1 删除文章图片与素材的关系 （wmNews.getId()相关的全部删除）
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery()
                    .eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews); // 修改则需要根据id来更新db
        }
    }
}