package cn.whu.wemedia.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("cn.whu.apis.article.fallback") // 扫描远程调用时的熔断降级逻辑代码
public class InitConfig {
}
