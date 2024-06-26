package cn.whu.kafka.controller;

import cn.whu.kafka.pojo.User;
import com.alibaba.fastjson.JSON;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello() {
        //kafkaTemplate.send("whu-topic","毕业啦");

        User user = new User();
        user.setName("whu");
        user.setAge(121);

        kafkaTemplate.send("whu-topic", JSON.toJSONString(user));

        return "ok";
    }

}
