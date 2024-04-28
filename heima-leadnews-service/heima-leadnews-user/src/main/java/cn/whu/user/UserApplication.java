package cn.whu.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication // boot启动类
@EnableDiscoveryClient // 注册中心发现  将自己注册到nacos注册中心，然后连上nacos自己也就可以查询其他微服务了
@MapperScan("cn.whu.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
}
