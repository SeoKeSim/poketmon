package sks.poketmon.dto.user;

public class JwtLoginResponseDto {
    private boolean success;
    private String message;
    private String token;
    private Long userCode;
    private String userId;
    private String userName;
    private String tokenType = "Bearer";

    // 기본 생성자
    public JwtLoginResponseDto() {}

    // 성공 응답 생성자
    public JwtLoginResponseDto(String token, Long userCode, String userId, String userName) {
        this.success = true;
        this.message = "로그인 성공";
        this.token = token;
        this.userCode = userCode;
        this.userId = userId;
        this.userName = userName;
    }

    // 실패 응답 생성자
    public JwtLoginResponseDto(String message) {
        this.success = false;
        this.message = message;
    }

    // 정적 팩토리 메서드
    public static JwtLoginResponseDto success(String token, Long userCode, String userId, String userName) {
        return new JwtLoginResponseDto(token, userCode, userId, userName);
    }

    public static JwtLoginResponseDto failure(String message) {
        return new JwtLoginResponseDto(message);
    }

    // Getter/Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserCode() {
        return userCode;
    }

    public void setUserCode(Long userCode) {
        this.userCode = userCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}