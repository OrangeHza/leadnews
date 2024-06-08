package cn.whu.wemedia.config;

import cn.whu.wemedia.interceptor.WmTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                new WmTokenInterceptor())//拦截器业务方法 (拦截到后执行的方法)  这里new一个也是单例
                .addPathPatterns("/**");//拦截哪些springMVC的Controller方法  这里拦截所有
    }

}
