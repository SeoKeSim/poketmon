package sks.poketmon.repository;

import sks.poketmon.entity.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {

    // 이름으로 포켓몬 찾기 (대소문자 무시)
    Optional<Pokemon> findByPokemonNameIgnoreCase(String pokemonName);

    // 타입으로 포켓몬들 찾기
    List<Pokemon> findByPokemonType1IgnoreCaseOrPokemonType2IgnoreCase(String type1, String type2);

    // 전설의 포켓몬만 찾기
    List<Pokemon> findByIsLegendaryTrue();

    // 환상의 포켓몬만 찾기
    List<Pokemon> findByIsMythicalTrue();

    // 이름으로 부분 검색 (LIKE 검색)
    @Query("SELECT p FROM Pokemon p WHERE LOWER(p.pokemonName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Pokemon> findByPokemonNameContainingIgnoreCase(@Param("name") String name);

    // 높이와 무게 범위로 검색
    List<Pokemon> findByHeightBetweenAndWeightBetween(int minHeight, int maxHeight, int minWeight, int maxWeight);

    // 포켓몬 ID 범위로 검색 (세대별 검색에 유용)
    List<Pokemon> findByPokemonIdBetween(Long startId, Long endId);
}