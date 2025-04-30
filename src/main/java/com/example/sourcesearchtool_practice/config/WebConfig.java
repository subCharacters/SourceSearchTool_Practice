package com.example.sourcesearchtool_practice.config;

import com.example.sourcesearchtool_practice.contorller.interceptor.IndexingCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final IndexingCheckInterceptor indexingCheckInterceptor;

    public WebConfig(IndexingCheckInterceptor indexingCheckInterceptor) {
        this.indexingCheckInterceptor = indexingCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(indexingCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/indexingProgress")
                .excludePathPatterns("/css/**"
                        , "/js/**"
                        , "/images/**"
                        , "/static/**");  // 정적 리소스 예외 처리
    }
}
