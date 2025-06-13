package sks.poketmon.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_code")
    private Long userCode;

    @Column(name = "user_id", unique = true, nullable = false, length = 50)
    private String userId;

    @Column(name = "user_pw", nullable = false, length = 255)
    private String userPw;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 기본 생성자
    public User() {}

    // 생성자
    public User(String userId, String userPw, String userName) {
        this.userId = userId;
        this.userPw = userPw;
        this.userName = userName;
        this.createdAt = LocalDateTime.now();
    }

    // Getter/Setter
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

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}