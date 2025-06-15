package sks.poketmon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sks.poketmon.entity.UserFavoritePokemon;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoritePokemonRepository extends JpaRepository<UserFavoritePokemon, Long> {

    // 특정 사용자의 즐겨찾기 목록 조회
    List<UserFavoritePokemon> findByUserCodeOrderByCreatedAtDesc(Long userCode);

    // 특정 사용자의 특정 포켓몬 즐겨찾기 여부 확인
    Optional<UserFavoritePokemon> findByUserCodeAndPokemonId(Long userCode, Integer pokemonId);

    // 특정 사용자의 특정 포켓몬 즐겨찾기 존재 여부 확인
    boolean existsByUserCodeAndPokemonId(Long userCode, Integer pokemonId);

    // 특정 사용자의 특정 포켓몬 즐겨찾기 삭제
    void deleteByUserCodeAndPokemonId(Long userCode, Integer pokemonId);

    // 특정 사용자의 즐겨찾기 개수 조회
    long countByUserCode(Long userCode);

    // 특정 포켓몬을 즐겨찾기한 사용자 수 조회
    long countByPokemonId(Integer pokemonId);
}