package sks.poketmon.controller;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sks.poketmon.config.JwtTokenProvider;
import sks.poketmon.dto.user.LoginRequestDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.dto.user.RegisterRequestDto;
import sks.poketmon.dto.user.RegisterResponseDto;
import sks.poketmon.entity.User;
import sks.poketmon.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 기존 로그인 로직 활용
            LoginRequestDto requestDto = new LoginRequestDto();
            requestDto.setUserId(loginRequest.getUserId());
            requestDto.setUserPw(loginRequest.getUserPw());

            LoginResponseDto loginResponse = userService.loginUser(requestDto);

            if (loginResponse.isSuccess()) {
                // JWT 토큰 생성
                String token = jwtTokenProvider.createToken(
                        loginResponse.getUserId(),
                        loginResponse.getUserCode()
                );

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("userCode", loginResponse.getUserCode());
                response.put("userId", loginResponse.getUserId());
                response.put("userName", loginResponse.getUserName());
                response.put("message", "로그인 성공");

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid credentials");
                error.put("message", loginResponse.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed");
            error.put("message", "로그인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 기존 회원가입 로직 활용
            RegisterRequestDto requestDto = new RegisterRequestDto();
            requestDto.setUserId(registerRequest.getUserId());
            requestDto.setUserPw(registerRequest.getUserPw());
            requestDto.setUserName(registerRequest.getUserName());

            RegisterResponseDto registerResponse = userService.registerUser(requestDto);

            if (registerResponse.getUserCode() != null) {
                // 등록 성공 시 바로 토큰 발급
                String token = jwtTokenProvider.createToken(
                        registerResponse.getUserId(),
                        registerResponse.getUserCode()
                );

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("userCode", registerResponse.getUserCode());
                response.put("userId", registerResponse.getUserId());
                response.put("userName", registerResponse.getUserName());
                response.put("message", "회원가입 성공");

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Registration failed");
                error.put("message", registerResponse.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }



    // 내부 클래스로 요청 DTO 정의
    public static class LoginRequest {
        private String userId;
        private String userPw;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserPw() { return userPw; }
        public void setUserPw(String userPw) { this.userPw = userPw; }
    }

    public static class RegisterRequest {
        private String userId;
        private String userPw;
        private String userName;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserPw() { return userPw; }
        public void setUserPw(String userPw) { this.userPw = userPw; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}