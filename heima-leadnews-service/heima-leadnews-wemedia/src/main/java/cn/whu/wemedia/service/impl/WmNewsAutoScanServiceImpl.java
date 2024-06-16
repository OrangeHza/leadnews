package cn.whu.wemedia.service.impl;

import cn.whu.apis.article.IArticleClient;
import cn.whu.common.aliyun.GreenImageScan;
import cn.whu.common.aliyun.GreenTextScan;
import cn.whu.common.tess4j.Tess4jClient;
import cn.whu.file.service.FileStorageService;
import cn.whu.model.article.dtos.ArticleDto;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.pojos.WmChannel;
import cn.whu.model.wemedia.pojos.WmNews;
import cn.whu.model.wemedia.pojos.WmSensitive;
import cn.whu.model.wemedia.pojos.WmUser;
import cn.whu.utils.common.SensitiveWordUtil;
import cn.whu.wemedia.mapper.WmChannelMapper;
import cn.whu.wemedia.mapper.WmNewsMapper;
import cn.whu.wemedia.mapper.WmSensitiveMapper;
import cn.whu.wemedia.mapper.WmUserMapper;
import cn.whu.wemedia.service.WmNewsAutoScanService;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Resource
    private WmNewsMapper wmNewsMapper;

    // 文本审核工具
    @Resource
    private GreenTextScan greenTextScan; // 导入了feign-api模块依赖，就可以直接用工具类 且已经创建成bean了

    // 图片审核工具
    @Resource
    private GreenImageScan greenImageScan;

    // 访问minIO下载图片
    @Resource
    private FileStorageService fileStorageService;

    // 远程调用，直接注入feign-api模块的client接口即可  会帮你找到对应微服务的controller并http调用并封装返回数据的
    @Resource
    private IArticleClient articleClient;

    // 文章频道基本信息，到自媒体模块的wm_channel表查
    @Resource
    private WmChannelMapper wmChannelMapper;

    // 查询自媒体用户名称
    @Resource
    private WmUserMapper wmUserMapper;

    //查询自定义敏感词库
    @Resource
    private WmSensitiveMapper wmSensitiveMapper;

    // ocr图片文字识别
    @Resource
    private Tess4jClient tess4jClient;

    /**
     * 自媒体文章审核
     *
     * @param id 自媒体文章id
     */
    @Override
    @Async // 标明当前方法是一个异步方法
    public void autoScanWmNews(Integer id) {
        // 1. 查询自媒体文章 （自媒体端的库中也有）
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-autoScanWmNews-文章不存在");
        }

        if (!wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            // 非"待审核"状态
            return;
        }

        // 从内容中提取存文本内容和图片 (传wnNews是因为封面图片)
        Map<String, Object> textAndImages = handleTextAndImages(wmNews);

        // 补：自管理的敏感词过滤：
        boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
        if (!isSensitive) return; // 自定义敏感词审核失败，直接return

        // 2. 审核文本内容  阿里云接口
        boolean isTextScan = handleTextScan((String) textAndImages.get("content"), wmNews);//审核失败需要修改wmNews的状态，直接传进去了
        // 模块化编程！！！
        if (!isTextScan) return; // 文本审核失败 图片也就没必要继续审核了

        // 3. 审核图片  阿里云接口
        boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"), wmNews);
        if (!isImageScan) return; // 图片审核失败 下面不能保存文章

        // 4. 审核成功，保存app端的相关文章数据
        ResponseResult responseResult = saveAppArticle(wmNews);
        if (responseResult.getCode() != 200) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-autoScanWmNews-" +
                    "文章审核成功后，保存app端相关文章数据失败");
        }
        // 回填article_id
        if (wmNews.getArticleId() == null) {
            wmNews.setArticleId((Long) responseResult.getData());//app端对应得文章有啦
        }
        updateWmNews(wmNews, WmNews.Status.PUBLISHED, "审核成功");

    }

    /**
     * 自管理(自定义)的敏感词审核
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag = true;
        // 获取所有敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery()
                .select(WmSensitive::getSensitives)//只查一个字段
        );
        List<String> sensitiveList = wmSensitives.stream()
                .map(WmSensitive::getSensitives)
                .collect(Collectors.toList());
        // 初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);
        // 查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content + "-" + wmNews.getTitle());
        if (map != null && map.size() > 0) {
            // 注意审核失败需要更新db  wmNews
            updateWmNews(wmNews, WmNews.Status.FAIL, "当前文章中存在违规内容" + map);
            flag = false;
        }
        return flag;
    }

    /**
     * 保存app端相关文章数据  (果然业务复杂起来都得这样搞)
     *
     * @param wmNews
     * @return
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        // 1. 属性拷贝
        BeanUtils.copyProperties(wmNews, dto);
        // 2. 其他重要属性手动填充
        // 文章的布局
        dto.setLayout(wmNews.getType()); // 就是无图、单图、多图
        // 频道名称
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            dto.setChannelName(wmChannel.getName());
        }
        // 作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            dto.setAuthorName(wmUser.getName());
        }
        // 修改还是新增：设置文章id
        if (wmNews.getArticleId() != null) {
            //审核成功会回填ArticleId，有就说明之前审核成功过，就能断定本次是修改不是新增
            dto.setId(wmNews.getArticleId()); // 有id就会自动走修改逻辑
        }
        // app端文章的创建时间应该是此刻
        dto.setCreatedTime(new Date());

        // 3. 远程调用，保存文章
        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;
    }

    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        // 0. 参数校验
        if (images == null || images.size() == 0) return true; // 无图肯定不违规

        // 1. 下载图片
        // 1.1 图片去重 （比如封面和正文哪张图片重了）
        images = images.stream().distinct().collect(Collectors.toList());
        // 1.2 下载
        ArrayList<byte[]> imageList = new ArrayList<>();

        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                // 补：ocr
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage read = ImageIO.read(in);
                String result = tess4jClient.doOCR(read);
                // 过滤文字  只过滤自定义敏感词了（不做阿里的那个文本审核了）
                boolean isSensitive = handleSensitiveScan(result, wmNews);
                if(!isSensitive) return false;

                imageList.add(bytes);
            }
        } catch (Exception e) {
            log.info("WmNewsAutoScanServiceImpl.handleImageScan OCR识别失败 wmNews.id:{}",
                    wmNews.getId(), e);
            e.printStackTrace();
        }

        // 2. 审核图片
        try {
            Map map = greenImageScan.imageScan(imageList);
            if (map != null && map.containsKey("suggestion")) {
                String suggestion = (String) map.get("suggestion");
                if ("block".equals(suggestion)) {
                    // 1) 审核失败
                    flag = false;
                    updateWmNews(wmNews, WmNews.Status.FAIL, "当前文章中存在违规图片");
                } else if ("review".equals(suggestion)) {
                    // 2) 不确定要进一步转人工审核
                    flag = false;
                    updateWmNews(wmNews, WmNews.Status.ADMIN_AUTH, "当前文章中存在不确定图片");
                } else if ("pass".equals(suggestion)) {
                    // 3) 审核通过
                    updateWmNews(wmNews, WmNews.Status.SUCCESS, "");
                }
            } else {
                log.info("WmNewsAutoScanServiceImpl-handleImageScan map is null: wmNewsId:{}", wmNews.getId());
            }
        } catch (Exception e) {
            flag = false;
            log.info("WmNewsAutoScanServiceImpl-handleImageScan error: wmNewsId:{}", wmNews.getId(), e);
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 审核纯文本内容
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;
        if (StringUtils.isBlank(content) && StringUtils.isBlank(wmNews.getTitle())) return true;
        try {
            // 标题也要审核
            Map map = greenTextScan.greenTextScan(content + "-" + wmNews.getTitle());
            if (map != null && map.containsKey("suggestion")) {
                // 工具类又封装了一遍 key为suggestion  取值pass为通过
                String suggestion = (String) map.get("suggestion");

                if (suggestion.equals("block")) {
                    // 1) 审核失败
                    updateWmNews(wmNews, WmNews.Status.FAIL, "当前文章中存在违规内容");
                    flag = false;
                } else if (suggestion.equals("review")) {
                    // 2) 不确定 需要转人工审核
                    updateWmNews(wmNews, WmNews.Status.ADMIN_AUTH, "当前文章中存在不确定内容");
                    flag = false;
                } else if (suggestion.equals("pass")) {
                    // 3) 审核成功
                    updateWmNews(wmNews, WmNews.Status.SUCCESS, "");
                }
            } else {
                log.info("WmNewsAutoScanServiceImpl-handleTextScan map is null: wmNewsId:{}", wmNews.getId());
            }
        } catch (Exception e) {
            flag = false;
            log.info("WmNewsAutoScanServiceImpl-handleTextScan error: wmNewsId:{}", wmNews.getId(), e);
            e.printStackTrace();
        }

        return flag;
    }

    private void updateWmNews(WmNews wmNews, WmNews.Status status, String reason) {
        wmNews.setStatus(status.getCode());
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);//更新db呀
    }

    /**
     * 1.从自媒体的文章内容中提取图片和文本
     * 2.提取文章的封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        // 纯文本，连接到一起，做一次审核
        StringBuilder text = new StringBuilder();
        // 需要审核的一张张图片url (图片有分辨率限制，无法整合在一起 图像拼接不简单呀)
        List<String> images = new ArrayList<>();

        // 1.从自媒体的文章内容中提取图片和文本
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            // json转对象数组，parseArray即可
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if ("text".equals(map.get("type"))) {
                    text.append(map.get("value"));
                }

                if ("image".equals(map.get("type"))) {
                    images.add((String) map.get("value"));
                }
            }
        }

        // 2.提取文章的封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            // addAll和asList 会用api呀 ★
            images.addAll(Arrays.asList(split));
        }

        // 3. 返回结果
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", text.toString());
        resultMap.put("images", images);
        return resultMap;

    }
}
