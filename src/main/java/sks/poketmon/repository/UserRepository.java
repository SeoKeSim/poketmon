package sks.poketmon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sks.poketmon.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자 ID로 조회 (로그인, 중복 체크용)
    Optional<User> findByUserId(String userId);

    // 사용자 ID 존재 여부 체크
    boolean existsByUserId(String userId);
}