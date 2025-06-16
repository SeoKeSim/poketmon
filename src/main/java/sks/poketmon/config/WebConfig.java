package sks.poketmon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/jwt/**")  // JWT 인증이 필요한 경로
                .excludePathPatterns(
                        "/api/auth/**",      // 인증 관련 경로는 제외
                        "/api/public/**",    // 공개 API는 제외
                        "/h2-console/**"     // H2 콘솔은 제외
                );
    }
}