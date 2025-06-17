package sks.poketmon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private JwtWebInterceptor jwtWebInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API용 JWT 인터셉터 (기존)
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/jwt/**")  // JWT 인증이 필요한 API 경로
                .excludePathPatterns(
                        "/api/auth/**",      // 인증 관련 경로는 제외
                        "/api/public/**",    // 공개 API는 제외
                        "/h2-console/**"     // H2 콘솔은 제외
                );

        // 웹 페이지용 JWT 인터셉터 (새로 추가)
        registry.addInterceptor(jwtWebInterceptor)
                .addPathPatterns("/**")  // 모든 웹 페이지에 적용
                .excludePathPatterns(
                        "/api/**",          // API는 제외
                        "/css/**",          // 정적 리소스 제외
                        "/js/**",
                        "/image/**",
                        "/favicon.ico",
                        "/h2-console/**",
                        "/users/login",     // 로그인 페이지는 제외
                        "/users/register",  // 회원가입 페이지는 제외
                        "/users/register-success"
                );
    }
}