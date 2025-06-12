package sks.poketmon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sks.poketmon.entity.PokemonName;
import java.util.Optional;

@Repository
public interface PokemonNameRepository extends JpaRepository<PokemonName, Long> {

    // 한국어 이름으로 포켓몬 찾기
    @Query("SELECT p FROM PokemonName p WHERE p.koreanName = :koreanName")
    Optional<PokemonName> findByKoreanName(@Param("koreanName") String koreanName);

    // 영어 이름으로 포켓몬 찾기
    @Query("SELECT p FROM PokemonName p WHERE p.englishName = :englishName")
    Optional<PokemonName> findByEnglishName(@Param("englishName") String englishName);

    // 포켓몬 ID로 찾기
    @Query("SELECT p FROM PokemonName p WHERE p.pokemonId = :pokemonId")
    Optional<PokemonName> findByPokemonId(@Param("pokemonId") Integer pokemonId);

    // 한국어나 영어 이름으로 검색 (유연한 검색)
    @Query("SELECT p FROM PokemonName p WHERE p.koreanName = :name OR p.englishName = :name")
    Optional<PokemonName> findByAnyName(@Param("name") String name);

    // 포켓몬 ID 존재 여부 확인
    boolean existsByPokemonId(Integer pokemonId);
}