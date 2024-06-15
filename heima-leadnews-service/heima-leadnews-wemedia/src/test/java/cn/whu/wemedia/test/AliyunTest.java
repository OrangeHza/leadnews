package cn.whu.wemedia.test;

import cn.whu.common.aliyun.GreenImageScan;
import cn.whu.common.aliyun.GreenTextScan;
import cn.whu.file.service.FileStorageService;
import cn.whu.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void testScanText() throws Exception {
        Map map = greenTextScan.greenTextScan("我是一个好人,破口大骂傻逼");
        System.out.println(map);
    }

    @Test
    public void testScanImage() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile("http://192.168.141.102:9000/leadnews/2024/06/06/xyy.jpg");
        Map map = greenImageScan.imageScan(Arrays.asList(bytes));
        System.out.println(map);
    }
}