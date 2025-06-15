package sks.poketmon.dto.user;

public class LoginResponseDto {
    private boolean success;
    private Long userCode;
    private String userId;
    private String userName;
    private String message;

    public LoginResponseDto() {}

    public LoginResponseDto(boolean success, Long userCode, String userId, String userName, String message) {
        this.success = success;
        this.userCode = userCode;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
    }

    // 성공 응답 생성자
    public static LoginResponseDto success(Long userCode, String userId, String userName) {
        return new LoginResponseDto(true, userCode, userId, userName, "로그인에 성공했습니다.");
    }

    // 실패 응답 생성자
    public static LoginResponseDto failure(String message) {
        return new LoginResponseDto(false, null, null, null, message);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Long getUserCode() { return userCode; }
    public void setUserCode(Long userCode) { this.userCode = userCode; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}