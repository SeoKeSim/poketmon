package sks.poketmon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sks.poketmon.dto.user.LoginResponseDto;

@Component
public class JwtWebInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        // 이미 세션 로그인이 되어있으면 JWT 처리 건너뛰기
        if (session.getAttribute("loginUser") != null) {
            return true;
        }

        // Authorization 헤더에서 JWT 토큰 확인
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenProvider.resolveToken(authHeader);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 유효한 JWT 토큰인 경우 사용자 정보 추출
            String userId = jwtTokenProvider.getUserId(token);
            Long userCode = jwtTokenProvider.getUserCode(token);

            // JWT 사용자 정보를 LoginResponseDto 형태로 생성
            LoginResponseDto jwtUser = new LoginResponseDto();
            jwtUser.setUserId(userId);
            jwtUser.setUserCode(userCode);
            jwtUser.setUserName(userId); // userName이 없으므로 userId로 임시 설정
            jwtUser.setSuccess(true);
            jwtUser.setMessage("JWT 로그인");

            // request에 JWT 사용자 정보 저장 (컨트롤러에서 사용)
            request.setAttribute("jwtUser", jwtUser);

            // 세션에도 임시로 저장하여 뷰에서 사용할 수 있도록 함
            // 단, 세션 만료 시간을 짧게 설정하거나 요청 완료 후 제거
            session.setAttribute("tempJwtUser", jwtUser);
        }

        return true;
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
}