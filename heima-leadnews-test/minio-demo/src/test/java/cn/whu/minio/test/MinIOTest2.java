package cn.whu.minio.test;

import cn.whu.file.service.FileStorageService;
import cn.whu.minio.MinIOApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest(classes = MinIOApplication.class)
@RunWith(SpringRunner.class)
public class MinIOTest2 {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 把list.html文件上传到minio中，并且可以在浏览器中访问
     */
    @Test
    public void test() throws FileNotFoundException {

        // 0. 先读取前面生成的静态文件，以便上传
        FileInputStream fis = new FileInputStream("E:/prodata/leader-news/list.html");
        // 1. 直接用fileStorageService上传
        String filePath = fileStorageService.uploadHtmlFile("", "list.html", fis);
        // 2. 浏览器访问路径(网址)
        System.out.println(filePath);
    }

    /**
     * 把xyy.jpg文件上传到minio中，并且可以在浏览器中访问
     */
    @Test
    public void test2() throws FileNotFoundException {

        // 0. 先读取前面生成的静态文件，以便上传
        FileInputStream fis = new FileInputStream("E:/prodata/leader-news/xyy.jpg");
        // 1. 直接用fileStorageService上传
        String filePath = fileStorageService.uploadImgFile("", "xyy.jpg", fis);
        // 2. 浏览器访问路径(网址)
        System.out.println(filePath);
    }
}
