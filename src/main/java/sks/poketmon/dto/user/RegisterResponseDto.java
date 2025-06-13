package sks.poketmon.dto.user;

public class RegisterResponseDto {
    private Long userCode;
    private String userId;
    private String userName;
    private String message;

    public RegisterResponseDto() {}

    public RegisterResponseDto(Long userCode, String userId, String userName, String message) {
        this.userCode = userCode;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
    }

    public Long getUserCode() { return userCode; }
    public void setUserCode(Long userCode) { this.userCode = userCode; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
