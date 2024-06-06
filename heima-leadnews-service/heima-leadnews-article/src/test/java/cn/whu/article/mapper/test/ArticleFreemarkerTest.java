package cn.whu.article.mapper.test;

import cn.whu.article.ArticleApplication;
import cn.whu.article.mapper.ApArticleContentMapper;
import cn.whu.article.service.ApArticleService;
import cn.whu.file.service.FileStorageService;
import cn.whu.model.article.pojos.ApArticle;
import cn.whu.model.article.pojos.ApArticleContent;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    // 查文章内容 db里面的longtext（最大4G的字符串）
    @Autowired
    private ApArticleContentMapper articleContentMapper;

    // 读取模版，将文章内容填充到模版里面
    @Autowired
    private Configuration configuration;//注意是：freemarker.template.Configuration

    // 将流上传到minio对象存储服务器
    @Autowired
    private FileStorageService fileStorageService;

    // 需要更新ap_article表的static_url字段 (内容静态html访问url)  注意注入的是service 上节完善了service 夸表也应该是service
    @Autowired
    private ApArticleService apArticleService;

    @Test
    public void createStaticUrlTest() throws IOException, TemplateException {
        // 假设已知文章的id：ap_article表的主键id, ap_article_content 表的 article_id（非主键）
        long articleId = 1302865306489733122l;

        // 1.获取文章内容 (ap_article_content表里根据article_id来查，注意不是主键)
        ApArticleContent apArticleContent = articleContentMapper.selectOne(
                Wrappers.<ApArticleContent>lambdaQuery()
                        .eq(ApArticleContent::getArticleId, articleId)
        );
        if (apArticleContent == null || StringUtils.isBlank(apArticleContent.getContent())) {
            return;
        }

        // 2.文章内容通过freemarker生成htmL文件
        // 2.1 读取模板，就是本地.ftl文件 (只有格式没有数据的html模板)
        Template template = configuration.getTemplate("article.ftl");
        // 2.2 获取数据
        Map<String, Object> content = new HashMap<>();
        // (db里面查到的是string，转一下Object)
        Object obj = JSONArray.parse(apArticleContent.getContent());
        // key必须"content" ftl模板里面写死了，遍历的以content为key的map
        content.put("content", obj);
        // 2.3 将数据填充到模板，得到静态html，并将其暂存于输出流
        StringWriter out = new StringWriter(); // 需要一个Writer类型的输出流
        // 2.4 数据合并到模板内，并暂存out以作为返回
        template.process(content,out); // 暂存out，下面会直接上传(写到)minIO服务器上

        // 3.把html文件上传到minio中
        // 3.1 html文件目前暂存在out流中,转换为输入流InputStream
        InputStream in = new ByteArrayInputStream(out.toString().getBytes());
        // 3.2 上传，id作为文件名称
        String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", in);

        // 4.修改ap_article表，保存static_url字段
        apArticleService.update(Wrappers.<ApArticle>lambdaUpdate()
                .eq(ApArticle::getId,articleId)
                .set(ApArticle::getStaticUrl,path)
        );
        System.out.println(path);
    }

}
