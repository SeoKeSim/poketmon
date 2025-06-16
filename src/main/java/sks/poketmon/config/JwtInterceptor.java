package sks.poketmon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 통과
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = jwtTokenProvider.resolveToken(request.getHeader("Authorization"));

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 유효한 토큰인 경우 사용자 정보를 request에 저장
            String userId = jwtTokenProvider.getUserId(token);
            Long userCode = jwtTokenProvider.getUserCode(token);

            request.setAttribute("userId", userId);
            request.setAttribute("userCode", userCode);
            return true;
        }

        // 토큰이 없거나 유효하지 않은 경우
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"유효한 JWT 토큰이 필요합니다.\"}");
        return false;
    }
}