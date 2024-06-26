package cn.whu.kafka.listener;

import cn.whu.kafka.pojo.User;
import com.alibaba.fastjson.JSON;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HelloListener {

    @KafkaListener(topics = "whu-topic") // 发送方定义的topic
    public void onMessage(String message) { // 有了@KafkaListener注解加持，message就是发送消息的data
        if (!StringUtils.isEmpty(message)) {
            //System.out.println(message);
            User user = JSON.parseObject(message, User.class);
            System.out.println(user);
        }
    }

}
