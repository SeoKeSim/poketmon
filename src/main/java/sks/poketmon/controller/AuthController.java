package sks.poketmon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sks.poketmon.dto.user.*;
import sks.poketmon.service.UserService;
import sks.poketmon.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * JWT 기반 회원가입
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<JwtRegisterResponseDto> registerWithJwt(@RequestBody RegisterRequestDto request) {
        try {
            // 기존 회원가입 로직 사용
            RegisterResponseDto registerResponse = userService.registerUser(request);

            if (registerResponse.getUserCode() != null) {
                // 회원가입 성공 시 JWT 토큰 생성
                String token = jwtUtil.generateToken(
                        registerResponse.getUserId(),
                        registerResponse.getUserName(),
                        registerResponse.getUserCode()
                );

                JwtRegisterResponseDto response = JwtRegisterResponseDto.success(
                        token,
                        registerResponse.getUserCode(),
                        registerResponse.getUserId(),
                        registerResponse.getUserName()
                );

                return ResponseEntity.ok(response);
            } else {
                // 회원가입 실패
                return ResponseEntity.badRequest()
                        .body(JwtRegisterResponseDto.failure(registerResponse.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JwtRegisterResponseDto.failure("회원가입 중 오류가 발생했습니다."));
        }
    }

    /**
     * JWT 기반 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<JwtLoginResponseDto> loginWithJwt(@RequestBody LoginRequestDto request) {
        try {
            // 기존 로그인 로직 사용
            LoginResponseDto loginResponse = userService.loginUser(request);

            if (loginResponse.isSuccess()) {
                // 로그인 성공 시 JWT 토큰 생성
                String token = jwtUtil.generateToken(
                        loginResponse.getUserId(),
                        loginResponse.getUserName(),
                        loginResponse.getUserCode()
                );

                JwtLoginResponseDto response = JwtLoginResponseDto.success(
                        token,
                        loginResponse.getUserCode(),
                        loginResponse.getUserId(),
                        loginResponse.getUserName()
                );

                return ResponseEntity.ok(response);
            } else {
                // 로그인 실패
                return ResponseEntity.badRequest()
                        .body(JwtLoginResponseDto.failure(loginResponse.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JwtLoginResponseDto.failure("로그인 중 오류가 발생했습니다."));
        }
    }

    /**
     * JWT 토큰 검증
     * POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "올바르지 않은 토큰 형식입니다."));
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);

            if (isValid) {
                String userId = jwtUtil.getUserIdFromToken(token);
                String userName = jwtUtil.getUserNameFromToken(token);
                Long userCode = jwtUtil.getUserCodeFromToken(token);

                return ResponseEntity.ok(new ValidateTokenResponse(true, "유효한 토큰입니다.",
                        userCode, userId, userName));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "유효하지 않은 토큰입니다."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "토큰 검증 중 오류가 발생했습니다."));
        }
    }

    /**
     * JWT 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "올바르지 않은 토큰 형식입니다."));
            }

            String token = authHeader.substring(7);
            String newToken = jwtUtil.refreshToken(token);

            if (newToken != null) {
                return ResponseEntity.ok(new RefreshTokenResponse(true, "토큰이 갱신되었습니다.", newToken));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "토큰 갱신에 실패했습니다."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "토큰 갱신 중 오류가 발생했습니다."));
        }
    }

    // 내부 응답 클래스들
    public static class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getter/Setter
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ValidateTokenResponse extends ApiResponse {
        private Long userCode;
        private String userId;
        private String userName;

        public ValidateTokenResponse(boolean success, String message, Long userCode, String userId, String userName) {
            super(success, message);
            this.userCode = userCode;
            this.userId = userId;
            this.userName = userName;
        }

        // Getter/Setter
        public Long getUserCode() { return userCode; }
        public void setUserCode(Long userCode) { this.userCode = userCode; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }

    public static class RefreshTokenResponse extends ApiResponse {
        private String token;
        private String tokenType = "Bearer";

        public RefreshTokenResponse(boolean success, String message, String token) {
            super(success, message);
            this.token = token;
        }

        // Getter/Setter
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    }
}