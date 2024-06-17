package cn.whu.article.service.impl;

import cn.whu.article.mapper.ApArticleContentMapper;
import cn.whu.article.service.ApArticleService;
import cn.whu.article.service.ArticleFreemarkerService;
import cn.whu.file.service.FileStorageService;
import cn.whu.model.article.pojos.ApArticle;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    // 查文章内容 db里面的longtext（最大4G的字符串）
    @Resource
    private ApArticleContentMapper articleContentMapper;

    // 读取模版，将文章内容填充到模版里面
    @Resource
    private Configuration configuration;//注意是：freemarker.template.Configuration

    // 将流上传到minio对象存储服务器
    @Resource
    private FileStorageService fileStorageService;

    // 需要更新ap_article表的static_url字段 (内容静态html访问url)  注意注入的是service 上节完善了service 夸表也应该是service
    @Resource
    private ApArticleService apArticleService;


    /**
     * 生成静态文件上传到minIO中
     *
     * @param apArticle
     * @param content
     */
    @Async // 此方法被调用时，会是异步调用
    @Override
    public void buildArticleToMinIo(ApArticle apArticle, String content) {

        // 1. 参数检查
        if (StringUtils.isBlank(content)) {
            return;
        }

        // 2.文章内容通过freemarker生成htmL文件
        StringWriter out = null; // 需要一个Writer类型的输出流
        try {
            // 2.1 读取模板，就是本地.ftl文件 (只有格式没有数据的html模板)
            Template template = configuration.getTemplate("article.ftl");
            // 2.2 获取数据
            Map<String, Object> contentDataModel = new HashMap<>();
            // (db里面查到的是string，转一下Object)
            Object obj = JSONArray.parse(content);
            // key必须"content" ftl模板里面写死了，遍历的以content为key的map
            contentDataModel.put("content", obj);
            // 2.3 将数据填充到模板，得到静态html，并将其暂存于输出流
            out = new StringWriter();
            // 2.4 数据合并到模板内，并暂存out以作为返回
            template.process(contentDataModel, out); // 暂存out，下面会直接上传(写到)minIO服务器上
        } catch (Exception e) {
            log.info("ArticleFreemarkerServiceImpl-buildArticleToMinIo 生成模板失败 apArticle.id:{}",
                    apArticle.getId(), e);
            e.printStackTrace();
        }

        // 3.把html文件上传到minio中
        // 3.1 html文件目前暂存在out流中,转换为输入流InputStream
        InputStream in = new ByteArrayInputStream(out.toString().getBytes());
        // 3.2 上传，id作为文件名称
        String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", in);

        // 4.修改ap_article表，保存static_url字段 ★
        apArticleService.update(Wrappers.<ApArticle>lambdaUpdate()
                .eq(ApArticle::getId, apArticle.getId())
                .set(ApArticle::getStaticUrl, path)
        );
        System.out.println(path);

    }
}
