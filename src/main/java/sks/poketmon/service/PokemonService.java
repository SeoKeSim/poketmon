package sks.poketmon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import sks.poketmon.dto.PokemonDto;
import sks.poketmon.dto.PokemonSpeciesDto;
import sks.poketmon.entity.PokemonName;
import sks.poketmon.repository.PokemonNameRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PokemonService {

    private static final Logger logger = LoggerFactory.getLogger(PokemonService.class);

    private final RestTemplate restTemplate;
    private final PokemonNameRepository pokemonNameRepository;

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final String POKEAPI_SPECIES_URL = "https://pokeapi.co/api/v2/pokemon-species/";

    @Autowired
    public PokemonService(RestTemplate restTemplate, PokemonNameRepository pokemonNameRepository) {
        this.restTemplate = restTemplate;
        this.pokemonNameRepository = pokemonNameRepository;
    }

    public PokemonDto searchPokemon(String query) {
        try {
            logger.info("포켓몬 검색 시작: {}", query);

            // 1. 먼저 DB에서 한국어/영어 이름으로 검색
            String searchTerm = findEnglishNameFromDB(query);

            // 2. DB에 없으면 직접 검색 (영어나 숫자)
            if (searchTerm == null) {
                searchTerm = query.toLowerCase().trim();
            }

            logger.debug("최종 검색어: {}", searchTerm);

            // 3. Pokemon API 호출 - ResponseEntity 사용으로 더 안전하게
            String url = POKEAPI_BASE_URL + searchTerm;
            logger.debug("API 호출 URL: {}", url);

            ResponseEntity<PokemonDto> response;
            try {
                response = restTemplate.getForEntity(url, PokemonDto.class);
            } catch (HttpClientErrorException.NotFound e) {
                logger.warn("포켓몬을 찾을 수 없음: {}", searchTerm);
                return null;
            } catch (RestClientException e) {
                logger.error("API 호출 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("포켓몬 API 호출 중 오류가 발생했습니다: " + e.getMessage());
            }

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                logger.warn("API 응답이 비정상적입니다. Status: {}", response.getStatusCode());
                return null;
            }

            PokemonDto pokemon = response.getBody();
            logger.info("포켓몬 데이터 조회 성공: {} (ID: {})", pokemon.getName(), pokemon.getId());

            // 4. 성공했고 DB에 없는 포켓몬이면 species 정보를 가져와서 DB에 저장
            if (!pokemonNameRepository.existsByPokemonId(pokemon.getId())) {
                savePokemonNameFromSpecies(pokemon.getId());
            }

            // 5. pokemonDto에 한글 이름 세팅
            Optional<PokemonName> nameEntity = pokemonNameRepository.findByPokemonId(pokemon.getId());
            nameEntity.ifPresent(name -> {
                pokemon.setKoreanName(name.getKoreanName());
                logger.debug("한국어 이름 설정: {}", name.getKoreanName());
            });

            // 6. 타입과 스탯 이름을 한국어로 변환
            convertToKorean(pokemon);

            return pokemon;

        } catch (Exception e) {
            logger.error("포켓몬 검색 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("포켓몬 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 타입과 스탯을 한국어로 변환하는 메소드 추가
    private void convertToKorean(PokemonDto pokemon) {
        if (pokemon == null) return;

        // 타입 이름 변환
        if (pokemon.getTypes() != null) {
            for (PokemonDto.TypeDto typeDto : pokemon.getTypes()) {
                if (typeDto.getType() != null) {
                    String koreanTypeName = getKoreanTypeName(typeDto.getType().getName());
                    typeDto.getType().setKoreanName(koreanTypeName);
                }
            }
        }

        // 스탯 이름 변환
        if (pokemon.getStats() != null) {
            for (PokemonDto.StatDto statDto : pokemon.getStats()) {
                if (statDto.getStat() != null) {
                    String koreanStatName = getKoreanStatName(statDto.getStat().getName());
                    statDto.getStat().setKoreanName(koreanStatName);
                }
            }
        }
    }

    // DB에서 영어 이름 찾기
    private String findEnglishNameFromDB(String query) {
        // 한국어나 영어 이름으로 검색
        Optional<PokemonName> pokemonName = pokemonNameRepository.findByAnyName(query);

        if (pokemonName.isPresent()) {
            return pokemonName.get().getEnglishName();
        }

        // 숫자로 검색하는 경우 ID로 찾기
        try {
            int pokemonId = Integer.parseInt(query);
            Optional<PokemonName> pokemonById = pokemonNameRepository.findByPokemonId(pokemonId);
            if (pokemonById.isPresent()) {
                return pokemonById.get().getEnglishName();
            }
        } catch (NumberFormatException ignored) {
            // 숫자가 아닌 경우 무시
        }

        return null;
    }

    // Pokemon Species API에서 다국어 이름 가져와서 DB에 저장
    private void savePokemonNameFromSpecies(int pokemonId) {
        try {
            String speciesUrl = POKEAPI_SPECIES_URL + pokemonId;
            logger.debug("Species API 호출: {}", speciesUrl);

            ResponseEntity<PokemonSpeciesDto> response = restTemplate.getForEntity(speciesUrl, PokemonSpeciesDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PokemonSpeciesDto species = response.getBody();
                String englishName = species.getName();
                String koreanName = species.getKoreanName();

                // 한국어 이름이 있으면 저장
                if (koreanName != null && !koreanName.equals(englishName)) {
                    PokemonName pokemonName = new PokemonName(pokemonId, englishName, koreanName, "ko");
                    pokemonNameRepository.save(pokemonName);
                    logger.debug("포켓몬 이름 저장: {} -> {}", englishName, koreanName);
                }
            }
        } catch (Exception e) {
            // Species 정보 가져오기 실패해도 메인 검색에는 영향 없음
            logger.warn("Species 정보 저장 실패 (Pokemon ID: {}): {}", pokemonId, e.getMessage());
        }
    }

    // 초기 데이터 로딩 (애플리케이션 시작 시 실행)
    public void loadInitialPokemonNames(int maxPokemon) {
        logger.info("포켓몬 이름 데이터 로딩 시작...");

        for (int i = 1; i <= maxPokemon; i++) {
            if (!pokemonNameRepository.existsByPokemonId(i)) {
                try {
                    savePokemonNameFromSpecies(i);
                    // API 호출 제한을 위한 딜레이
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.error("Pokemon ID {} 로딩 실패: {}", i, e.getMessage());
                }
            }
        }

        logger.info("포켓몬 이름 데이터 로딩 완료!");
    }

    // 특정 포켓몬의 한국어 이름 가져오기
    public String getKoreanName(int pokemonId) {
        Optional<PokemonName> pokemonName = pokemonNameRepository.findByPokemonId(pokemonId);
        return pokemonName.map(PokemonName::getKoreanName).orElse(null);
    }

    // 타입 한국어 변환
    public String getKoreanTypeName(String englishType) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("normal", "노말");
        typeMap.put("fire", "불꽃");
        typeMap.put("water", "물");
        typeMap.put("electric", "전기");
        typeMap.put("grass", "풀");
        typeMap.put("ice", "얼음");
        typeMap.put("fighting", "격투");
        typeMap.put("poison", "독");
        typeMap.put("ground", "땅");
        typeMap.put("flying", "비행");
        typeMap.put("psychic", "에스퍼");
        typeMap.put("bug", "벌레");
        typeMap.put("rock", "바위");
        typeMap.put("ghost", "고스트");
        typeMap.put("dragon", "드래곤");
        typeMap.put("dark", "악");
        typeMap.put("steel", "강철");
        typeMap.put("fairy", "페어리");

        return typeMap.getOrDefault(englishType, englishType);
    }

    // 스탯 한국어 변환
    public String getKoreanStatName(String englishStat) {
        Map<String, String> statMap = new HashMap<>();
        statMap.put("hp", "HP");
        statMap.put("attack", "공격");
        statMap.put("defense", "방어");
        statMap.put("special-attack", "특수공격");
        statMap.put("special-defense", "특수방어");
        statMap.put("speed", "스피드");

        return statMap.getOrDefault(englishStat, englishStat);
    }
}