package sks.poketmon.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sks.poketmon.dto.user.LoginResponseDto;

@Slf4j
@Component
public class JwtWebInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        log.debug("JwtWebInterceptor 처리 중: {}", requestUri);

        HttpSession session = request.getSession();

        // 이미 세션 로그인이 되어있으면 JWT 처리 건너뛰기
        if (session.getAttribute("loginUser") != null) {
            log.debug("세션 로그인 사용자 존재, JWT 처리 건너뛰기");
            return true;
        }

        // JWT 토큰 확인 (여러 방법으로 시도)
        String token = getTokenFromRequest(request);
        log.debug("추출된 토큰: {}", token != null ? "존재" : "없음");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 유효한 JWT 토큰인 경우 사용자 정보 추출
            String userId = jwtTokenProvider.getUserId(token);
            Long userCode = jwtTokenProvider.getUserCode(token);

            log.debug("JWT 토큰에서 추출: userId={}, userCode={}", userId, userCode);

            // JWT 사용자 정보를 LoginResponseDto 형태로 생성
            LoginResponseDto jwtUser = new LoginResponseDto();
            jwtUser.setUserId(userId);
            jwtUser.setUserCode(userCode);
            jwtUser.setUserName(userId); // userName이 없으므로 userId로 임시 설정
            jwtUser.setSuccess(true);
            jwtUser.setMessage("JWT 로그인");

            // request에 JWT 사용자 정보 저장 (컨트롤러에서 사용)
            request.setAttribute("jwtUser", jwtUser);
            request.setAttribute("userCode", userCode); // 직접적으로도 설정
            request.setAttribute("userId", userId);

            // 세션에도 임시로 저장하여 뷰에서 사용할 수 있도록 함
            session.setAttribute("tempJwtUser", jwtUser);

            log.debug("JWT 사용자 정보 설정 완료");
            return true;
        }

        log.warn("유효한 JWT 토큰이 없음: {}", requestUri);
        // JWT가 필요한 페이지인데 토큰이 없으면 로그인 페이지로 리다이렉트
        response.sendRedirect("/users/login");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 요청 완료 후 임시 JWT 사용자 정보 제거 (선택사항)
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") == null) {
            // 세션 로그인이 없는 경우에만 임시 JWT 정보 제거
            // session.removeAttribute("tempJwtUser");
        }
    }

    // 다양한 방법으로 토큰 추출 시도 - 개선된 버전
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. Authorization 헤더에서 확인 (Bearer 토큰)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!token.isEmpty()) {
                log.debug("Authorization 헤더에서 Bearer 토큰 발견");
                return token;
            }
        }

        // 2. Authorization 헤더에서 직접 토큰 확인 (Bearer 없이)
        if (authHeader != null && !authHeader.startsWith("Bearer ") && !authHeader.isEmpty()) {
            log.debug("Authorization 헤더에서 직접 토큰 발견");
            return authHeader;
        }

        // 3. X-Auth-Token 헤더에서 확인
        String xAuthToken = request.getHeader("X-Auth-Token");
        if (xAuthToken != null && !xAuthToken.isEmpty()) {
            log.debug("X-Auth-Token 헤더에서 토큰 발견");
            return xAuthToken;
        }

        // 4. 쿠키에서 확인 - 개선된 로직
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                String cookieValue = cookie.getValue();

                if (("jwtToken".equals(cookieName) || "authToken".equals(cookieName))
                        && cookieValue != null && !cookieValue.isEmpty()) {
                    log.debug("쿠키에서 토큰 발견: {} = {}", cookieName,
                            cookieValue.length() > 20 ? cookieValue.substring(0, 20) + "..." : cookieValue);
                    return cookieValue;
                }
            }
        }

        // 5. 쿼리 파라미터에서 확인 (보안상 권장하지 않지만 fallback으로)
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isEmpty()) {
            log.debug("쿼리 파라미터에서 토큰 발견");
            return queryToken;
        }

        log.debug("모든 방법에서 토큰을 찾을 수 없음");
        return null;
    }
}