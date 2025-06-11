package sks.poketmon.service;

import sks.poketmon.entity.Pokemon;
import sks.poketmon.repository.PokemonRepository;
import sks.poketmon.dto.PokemonTestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class PokemonService {

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private PokemonNameMappingService nameMappingService;

    /**
     * 포켓몬 검색 (DB 우선, 없으면 API 호출)
     */
    public PokemonTestDTO getPokemon(String name) {
        // 1. 먼저 DB에서 검색
        Optional<Pokemon> pokemonFromDB = pokemonRepository.findByPokemonNameIgnoreCase(name);

        if (pokemonFromDB.isPresent()) {
            // DB에 있으면 DTO로 변환해서 반환
            return convertToDTO(pokemonFromDB.get());
        } else {
            // 2. DB에 없으면 외부 API 호출
            return getPokemonFromAPI(name);
        }
    }

    /**
     * 한국어 이름으로 포켓몬 검색
     */
    public PokemonTestDTO getPokemonByKoreanName(String koreanName) {
        String englishName = nameMappingService.getEnglishName(koreanName);
        if (englishName != null) {
            return getPokemon(englishName);
        } else {
            // 영어 이름으로도 시도
            return getPokemon(koreanName);
        }
    }

    /**
     * 외부 API에서 포켓몬 정보 가져오기
     */
    private PokemonTestDTO getPokemonFromAPI(String name) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase();
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, PokemonTestDTO.class);
    }

    /**
     * 포켓몬 저장 (API에서 가져온 데이터를 DB에 저장)
     */
    public Pokemon savePokemon(Pokemon pokemon) {
        return pokemonRepository.save(pokemon);
    }

    /**
     * 모든 포켓몬 조회
     */
    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    /**
     * 타입으로 포켓몬 검색
     */
    public List<Pokemon> getPokemonByType(String type) {
        return pokemonRepository.findByPokemonType1IgnoreCaseOrPokemonType2IgnoreCase(type, type);
    }

    /**
     * 전설의 포켓몬들 조회
     */
    public List<Pokemon> getLegendaryPokemon() {
        return pokemonRepository.findByIsLegendaryTrue();
    }

    /**
     * 환상의 포켓몬들 조회
     */
    public List<Pokemon> getMythicalPokemon() {
        return pokemonRepository.findByIsMythicalTrue();
    }

    /**
     * 이름으로 부분 검색
     */
    public List<Pokemon> searchPokemonByName(String name) {
        return pokemonRepository.findByPokemonNameContainingIgnoreCase(name);
    }

    /**
     * Entity를 DTO로 변환
     */
    private PokemonTestDTO convertToDTO(Pokemon pokemon) {
        PokemonTestDTO dto = new PokemonTestDTO();
        dto.setName(pokemon.getPokemonName());
        dto.setHeight(pokemon.getHeight() != null ? pokemon.getHeight() : 0);
        dto.setWeight(pokemon.getWeight() != null ? pokemon.getWeight() : 0);
        return dto;
    }

    /**
     * API 응답을 Entity로 변환하여 저장
     */
    public Pokemon saveFromAPI(String name) {
        try {
            PokemonTestDTO dto = getPokemonFromAPI(name);
            if (dto != null) {
                Pokemon pokemon = new Pokemon();
                pokemon.setPokemonName(dto.getName());
                pokemon.setHeight(dto.getHeight());
                pokemon.setWeight(dto.getWeight());
                // 추가 필드들은 별도 API 호출로 채워야 함

                return savePokemon(pokemon);
            }
        } catch (Exception e) {
            System.out.println("Error saving pokemon from API: " + e.getMessage());
        }
        return null;
    }
}