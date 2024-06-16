package cn.whu.wemedia.service.impl;

import cn.whu.wemedia.WemediaApplication;
import cn.whu.wemedia.service.WmNewsAutoScanService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
class WmNewsAutoScanServiceImplTest {

    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Test
    void autoScanWmNews() {
        wmNewsAutoScanService.autoScanWmNews(6235);
        // wm_news的db表里面找status==1的 （提交待审核得）
    }
}