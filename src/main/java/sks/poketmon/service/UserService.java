package sks.poketmon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sks.poketmon.dto.user.LoginRequestDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.dto.user.RegisterRequestDto;
import sks.poketmon.dto.user.RegisterResponseDto;
import sks.poketmon.dto.user.JwtLoginResponseDto;
import sks.poketmon.dto.user.JwtRegisterResponseDto;
import sks.poketmon.entity.User;
import sks.poketmon.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ==================== 세션 로그인 관련 메서드 ====================

    /**
     * 세션 기반 회원가입 처리
     */
    public RegisterResponseDto registerUser(RegisterRequestDto request) {
        // 1. 사용자 ID 중복 체크
        if (userRepository.existsByUserId(request.getUserId())) {
            return new RegisterResponseDto(null, null, null, "이미 존재하는 사용자 ID입니다.");
        }

        // 2. 입력값 검증
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return new RegisterResponseDto(null, null, null, "사용자 ID는 필수입니다.");
        }

        if (request.getUserPw() == null || request.getUserPw().trim().isEmpty()) {
            return new RegisterResponseDto(null, null, null, "비밀번호는 필수입니다.");
        }

        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            return new RegisterResponseDto(null, null, null, "사용자 이름은 필수입니다.");
        }

        // 3. 새 사용자 생성 및 저장
        try {
            User newUser = new User(
                    request.getUserId().trim(),
                    request.getUserPw(), // 실제로는 여기서 비밀번호 암호화 필요
                    request.getUserName().trim()
            );

            User savedUser = userRepository.save(newUser);

            return new RegisterResponseDto(
                    savedUser.getUserCode(),
                    savedUser.getUserId(),
                    savedUser.getUserName(),
                    "회원가입이 완료되었습니다."
            );

        } catch (Exception e) {
            return new RegisterResponseDto(null, null, null, "회원가입 중 오류가 발생했습니다.");
        }
    }

    /**
     * 세션 기반 로그인 처리
     */
    public LoginResponseDto loginUser(LoginRequestDto request) {
        // 1. 입력값 검증
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return LoginResponseDto.failure("사용자 ID를 입력해주세요.");
        }

        if (request.getUserPw() == null || request.getUserPw().trim().isEmpty()) {
            return LoginResponseDto.failure("비밀번호를 입력해주세요.");
        }

        // 2. 사용자 조회
        try {
            Optional<User> userOptional = userRepository.findByUserId(request.getUserId().trim());

            if (!userOptional.isPresent()) {
                return LoginResponseDto.failure("존재하지 않는 사용자 ID입니다.");
            }

            User user = userOptional.get();

            // 3. 비밀번호 검증 (실제로는 암호화된 비밀번호와 비교해야 함)
            if (!user.getUserPw().equals(request.getUserPw())) {
                return LoginResponseDto.failure("비밀번호가 일치하지 않습니다.");
            }

            // 4. 로그인 성공
            return LoginResponseDto.success(
                    user.getUserCode(),
                    user.getUserId(),
                    user.getUserName()
            );

        } catch (Exception e) {
            return LoginResponseDto.failure("로그인 중 오류가 발생했습니다.");
        }
    }

    // ==================== JWT 로그인 관련 메서드 ====================

    /**
     * JWT 기반 회원가입 처리
     */
    public JwtRegisterResponseDto jwtRegisterUser(RegisterRequestDto request) {
        // 1. 사용자 ID 중복 체크
        if (userRepository.existsByUserId(request.getUserId())) {
            return JwtRegisterResponseDto.failure("이미 존재하는 사용자 ID입니다.");
        }

        // 2. 입력값 검증
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return JwtRegisterResponseDto.failure("사용자 ID는 필수입니다.");
        }

        if (request.getUserPw() == null || request.getUserPw().trim().isEmpty()) {
            return JwtRegisterResponseDto.failure("비밀번호는 필수입니다.");
        }

        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            return JwtRegisterResponseDto.failure("사용자 이름은 필수입니다.");
        }

        // 3. 새 사용자 생성 및 저장
        try {
            User newUser = new User(
                    request.getUserId().trim(),
                    request.getUserPw(), // 실제로는 여기서 비밀번호 암호화 필요
                    request.getUserName().trim()
            );

            User savedUser = userRepository.save(newUser);

            // 4. JWT 토큰 생성 (JwtService에서 처리)
            // String token = jwtService.generateToken(savedUser);

            return JwtRegisterResponseDto.success(
                    savedUser.getUserCode(),
                    savedUser.getUserId(),
                    savedUser.getUserName(),
                    null, // 토큰은 별도로 생성
                    "회원가입이 완료되었습니다."
            );

        } catch (Exception e) {
            return JwtRegisterResponseDto.failure("회원가입 중 오류가 발생했습니다.");
        }
    }

    /**
     * JWT 기반 로그인 처리
     */
    public JwtLoginResponseDto jwtLoginUser(LoginRequestDto request) {
        // 1. 입력값 검증
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return JwtLoginResponseDto.failure("사용자 ID를 입력해주세요.");
        }

        if (request.getUserPw() == null || request.getUserPw().trim().isEmpty()) {
            return JwtLoginResponseDto.failure("비밀번호를 입력해주세요.");
        }

        // 2. 사용자 조회
        try {
            Optional<User> userOptional = userRepository.findByUserId(request.getUserId().trim());

            if (!userOptional.isPresent()) {
                return JwtLoginResponseDto.failure("존재하지 않는 사용자 ID입니다.");
            }

            User user = userOptional.get();

            // 3. 비밀번호 검증 (실제로는 암호화된 비밀번호와 비교해야 함)
            if (!user.getUserPw().equals(request.getUserPw())) {
                return JwtLoginResponseDto.failure("비밀번호가 일치하지 않습니다.");
            }

            // 4. JWT 토큰 생성 (JwtService에서 처리)
            // String token = jwtService.generateToken(user);

            // 5. 로그인 성공
            return JwtLoginResponseDto.success(
                    user.getUserCode(),
                    user.getUserId(),
                    user.getUserName(),
                    null, // 토큰은 별도 서비스에서 생성
                    "로그인이 완료되었습니다."
            );

        } catch (Exception e) {
            return JwtLoginResponseDto.failure("로그인 중 오류가 발생했습니다.");
        }
    }

    // ==================== 공통 메서드 ====================

    /**
     * 사용자 ID 중복 체크
     */
    public boolean isUserIdExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    /**
     * 사용자 정보 조회 (로그인한 사용자용)
     */
    public Optional<User> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }
}