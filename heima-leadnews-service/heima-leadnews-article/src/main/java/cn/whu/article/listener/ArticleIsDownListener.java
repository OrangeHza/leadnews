package cn.whu.article.listener;


import cn.whu.article.service.ApArticleConfigService;
import cn.whu.common.constants.WmNewsMessageConstants;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Slf4j
public class ArticleIsDownListener {

    @Resource
    private ApArticleConfigService apArticleConfigService;

    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void OnMessage(String message) {
        if(StringUtils.isNotBlank(message)){
            Map<String,Object> map = JSON.parseObject(message, Map.class);
            apArticleConfigService.updateByMap(map);
        }
    }

}
