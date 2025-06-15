package sks.poketmon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sks.poketmon.dto.FavoriteResponseDto;
import sks.poketmon.entity.PokemonName;
import sks.poketmon.entity.UserFavoritePokemon;
import sks.poketmon.exception.FavoriteException;
import sks.poketmon.repository.PokemonNameRepository;
import sks.poketmon.repository.UserFavoritePokemonRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteService {

    @Autowired
    private UserFavoritePokemonRepository favoritePokemonRepository;

    @Autowired
    private PokemonNameRepository pokemonNameRepository;

    // 즐겨찾기 추가
    public FavoriteResponseDto addFavorite(Long userCode, Integer pokemonId) {
        // 이미 즐겨찾기에 있는지 확인
        if (favoritePokemonRepository.existsByUserCodeAndPokemonId(userCode, pokemonId)) {
            throw new FavoriteException.AlreadyExistsException(pokemonId);
        }

        // 즐겨찾기 추가
        UserFavoritePokemon favorite = new UserFavoritePokemon(userCode, pokemonId);
        UserFavoritePokemon savedFavorite = favoritePokemonRepository.save(favorite);

        // DTO로 변환하여 반환
        return convertToDto(savedFavorite);
    }

    // 즐겨찾기 삭제
    public void removeFavorite(Long userCode, Integer pokemonId) {
        // 즐겨찾기에 있는지 확인
        if (!favoritePokemonRepository.existsByUserCodeAndPokemonId(userCode, pokemonId)) {
            throw new FavoriteException.NotFoundException(pokemonId);
        }

        // 즐겨찾기 삭제
        favoritePokemonRepository.deleteByUserCodeAndPokemonId(userCode, pokemonId);
    }

    // 내 즐겨찾기 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteResponseDto> getFavoriteList(Long userCode) {
        List<UserFavoritePokemon> favorites = favoritePokemonRepository.findByUserCodeOrderByCreatedAtDesc(userCode);

        return favorites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 포켓몬 즐겨찾기 여부 확인
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userCode, Integer pokemonId) {
        return favoritePokemonRepository.existsByUserCodeAndPokemonId(userCode, pokemonId);
    }

    // 즐겨찾기 개수 조회
    @Transactional(readOnly = true)
    public long getFavoriteCount(Long userCode) {
        return favoritePokemonRepository.countByUserCode(userCode);
    }

    // Entity를 DTO로 변환
    private FavoriteResponseDto convertToDto(UserFavoritePokemon favorite) {
        // 포켓몬 이름 정보 조회
        PokemonName pokemonName = pokemonNameRepository.findByPokemonId(favorite.getPokemonId())
                .orElse(null);

        String englishName = pokemonName != null ? pokemonName.getEnglishName() : "Unknown";
        String koreanName = pokemonName != null ? pokemonName.getKoreanName() : "알 수 없음";

        // 포켓몬 이미지 URL 생성 (PokeAPI 기본 이미지)
        String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"
                + favorite.getPokemonId() + ".png";

        return new FavoriteResponseDto(
                favorite.getId(),
                favorite.getPokemonId(),
                englishName,
                koreanName,
                imageUrl,
                favorite.getCreatedAt()
        );
    }
}