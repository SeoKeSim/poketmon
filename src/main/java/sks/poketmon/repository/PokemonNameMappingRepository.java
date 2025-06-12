package sks.poketmon.repository;

import sks.poketmon.entity.PokemonNameMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonNameMappingRepository extends JpaRepository<PokemonNameMapping, Long> {

    /**
     * 한국어 이름으로 매핑 찾기
     */
    Optional<PokemonNameMapping> findByKoreanName(String koreanName);

    /**
     * 영어 이름으로 매핑 찾기 (대소문자 무시)
     */
    Optional<PokemonNameMapping> findByEnglishNameIgnoreCase(String englishName);

    /**
     * 영어 이름 존재 여부 확인 (대소문자 무시)
     */
    boolean existsByEnglishNameIgnoreCase(String englishName);

    /**
     * 한국어, 영어 이름 조합 존재 여부 확인
     */
    boolean existsByKoreanNameAndEnglishNameIgnoreCase(String koreanName, String englishName);

    /**
     * 최근 생성된 매핑 10개 조회
     */
    List<PokemonNameMapping> findTop10ByOrderByCreatedAtDesc();

    /**
     * 한국어 이름으로 부분 검색
     */
    @Query("SELECT m FROM PokemonNameMapping m WHERE m.koreanName LIKE %:koreanName%")
    List<PokemonNameMapping> findByKoreanNameContaining(@Param("koreanName") String koreanName);

    /**
     * 영어 이름으로 부분 검색
     */
    @Query("SELECT m FROM PokemonNameMapping m WHERE LOWER(m.englishName) LIKE LOWER(CONCAT('%', :englishName, '%'))")
    List<PokemonNameMapping> findByEnglishNameContainingIgnoreCase(@Param("englishName") String englishName);

    /**
     * 특정 날짜 이후 생성된 매핑들 조회
     */
    List<PokemonNameMapping> findByCreatedAtAfter(LocalDateTime date);

    /**
     * 전체 매핑 수 조회
     */
    @Query("SELECT COUNT(m) FROM PokemonNameMapping m")
    long countAllMappings();

    /**
     * 오늘 생성된 매핑 수 조회
     */
    @Query("SELECT COUNT(m) FROM PokemonNameMapping m WHERE DATE(m.createdAt) = CURRENT_DATE")
    long countTodayMappings();

    /**
     * 중복된 한국어 이름 찾기 (데이터 정리용)
     */
    @Query("SELECT m.koreanName FROM PokemonNameMapping m GROUP BY m.koreanName HAVING COUNT(m) > 1")
    List<String> findDuplicateKoreanNames();

    /**
     * 중복된 영어 이름 찾기 (데이터 정리용)
     */
    @Query("SELECT m.englishName FROM PokemonNameMapping m GROUP BY m.englishName HAVING COUNT(m) > 1")
    List<String> findDuplicateEnglishNames();

    /**
     * 페이징을 위한 모든 매핑 조회 (생성일 기준 정렬)
     */
    List<PokemonNameMapping> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 영어 이름들에 대한 매핑들 일괄 조회
     */
    @Query("SELECT m FROM PokemonNameMapping m WHERE LOWER(m.englishName) IN :englishNames")
    List<PokemonNameMapping> findByEnglishNameInIgnoreCase(@Param("englishNames") List<String> englishNames);

    /**
     * 특정 한국어 이름들에 대한 매핑들 일괄 조회
     */
    List<PokemonNameMapping> findByKoreanNameIn(List<String> koreanNames);
}