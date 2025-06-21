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
                savePokemonNameFromSpecies(pokemon.getId(), pokemon.getName());
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

    // Pokemon Species API에서 다국어 이름 가져와서 DB에 저장 (개선됨)
    private void savePokemonNameFromSpecies(int pokemonId, String actualPokemonName) {
        try {
            // 먼저 하드코딩된 특수 케이스들을 확인
            String koreanName = getHardcodedKoreanName(pokemonId, actualPokemonName);

            if (koreanName != null) {
                // 하드코딩된 이름이 있는 경우
                PokemonName pokemonName = new PokemonName(pokemonId, actualPokemonName, koreanName, "ko");
                pokemonNameRepository.save(pokemonName);
                logger.debug("하드코딩된 포켓몬 이름 저장: {} -> {}", actualPokemonName, koreanName);
                return;
            }

            // Species API에서 기본 종족 정보 가져오기
            int speciesId = getSpeciesIdFromPokemonId(pokemonId, actualPokemonName);
            String speciesUrl = POKEAPI_SPECIES_URL + speciesId;
            logger.debug("Species API 호출: {} (Pokemon ID: {}, Species ID: {})", speciesUrl, pokemonId, speciesId);

            ResponseEntity<PokemonSpeciesDto> response = restTemplate.getForEntity(speciesUrl, PokemonSpeciesDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PokemonSpeciesDto species = response.getBody();
                String baseKoreanName = species.getKoreanNameByStream(); // getKoreanName() 대신 더 안전한 메서드 사용

                // 한국어 이름이 있으면 저장
                if (baseKoreanName != null && !baseKoreanName.equals(species.getName())) {
                    // 폼 변종인 경우 기본 이름에 폼 정보 추가
                    String finalKoreanName = buildFormName(actualPokemonName, baseKoreanName);

                    PokemonName pokemonName = new PokemonName(pokemonId, actualPokemonName, finalKoreanName, "ko");
                    pokemonNameRepository.save(pokemonName);
                    logger.debug("포켓몬 이름 저장: {} -> {} (Species: {})", actualPokemonName, finalKoreanName, baseKoreanName);
                } else {
                    logger.debug("한국어 이름을 찾을 수 없음: {}", actualPokemonName);
                }
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Species 정보를 찾을 수 없음 (Pokemon ID: {}, Name: {})", pokemonId, actualPokemonName);
        } catch (Exception e) {
            // Species 정보 가져오기 실패해도 메인 검색에는 영향 없음
            logger.warn("Species 정보 저장 실패 (Pokemon ID: {}, Name: {}): {}", pokemonId, actualPokemonName, e.getMessage());
        }
    }

    // 특수 케이스 하드코딩 (폼 변종 포켓몬들)
    private String getHardcodedKoreanName(int pokemonId, String englishName) {
        Map<String, String> specialCases = new HashMap<>();

        // Morpeko 관련
        specialCases.put("morpeko-full-belly", "모르페코");
        specialCases.put("morpeko-hangry", "모르페코 (배고픈모습)");

        // Toxapex 등 다른 특수 케이스들 추가 가능
        // specialCases.put("toxapex", "더시마루");

        // 추가 특수 케이스들...
        specialCases.put("minior-red-meteor", "미뇰 (빨간색 유성)");
        specialCases.put("minior-orange-meteor", "미뇽 (주황색 유성)");
        specialCases.put("mimikyu-disguised", "따라큐 (둔갑)");
        specialCases.put("mimikyu-busted", "따라큐 (들킨)");

        return specialCases.get(englishName.toLowerCase());
    }

    // 폼 이름 생성 (기본 이름 + 폼 정보)
    private String buildFormName(String actualName, String baseKoreanName) {
        if (actualName.contains("-")) {
            String[] parts = actualName.split("-");
            if (parts.length > 1) {
                String formPart = parts[parts.length - 1];

                // 폼 이름 매핑
                Map<String, String> formNames = new HashMap<>();
                formNames.put("alolan", "알로라");
                formNames.put("galarian", "가라르");
                formNames.put("hisuian", "히스이");
                formNames.put("paldean", "팔데아");
                formNames.put("mega", "메가");
                formNames.put("primal", "원시");
                formNames.put("origin", "오리진");
                formNames.put("sky", "스카이");
                formNames.put("hangry", "배고픈모습");
                formNames.put("full-belly", "포만한모습");

                String koreanForm = formNames.get(formPart.toLowerCase());
                if (koreanForm != null) {
                    return baseKoreanName + " (" + koreanForm + ")";
                }
            }
        }

        return baseKoreanName;
    }

    // Pokemon ID로부터 Species ID 결정 (폼 변종 처리)
    private int getSpeciesIdFromPokemonId(int pokemonId, String pokemonName) {
        // 대부분의 포켓몬은 Pokemon ID와 Species ID가 동일
        // 하지만 일부 폼 변종들은 다른 Species를 참조할 수 있음

        // 폼 변종인 경우 기본 종족 ID 반환
        if (pokemonName.contains("-")) {
            // 특수 케이스들 처리
            Map<String, Integer> specialSpeciesIds = new HashMap<>();

            // Morpeko: 둘 다 같은 species (877)
            specialSpeciesIds.put("morpeko-full-belly", 877);
            specialSpeciesIds.put("morpeko-hangry", 877);

            // Mimikyu: 둘 다 같은 species (778)
            specialSpeciesIds.put("mimikyu-disguised", 778);
            specialSpeciesIds.put("mimikyu-busted", 778);

            // Minior: 모든 색상이 같은 species (774)
            String[] miniorForms = {"red-meteor", "orange-meteor", "yellow-meteor",
                    "green-meteor", "blue-meteor", "indigo-meteor", "violet-meteor",
                    "red", "orange", "yellow", "green", "blue", "indigo", "violet"};
            for (String form : miniorForms) {
                specialSpeciesIds.put("minior-" + form, 774);
            }

            // Wishiwashi
            specialSpeciesIds.put("wishiwashi-solo", 746);
            specialSpeciesIds.put("wishiwashi-school", 746);

            Integer speciesId = specialSpeciesIds.get(pokemonName.toLowerCase());
            if (speciesId != null) {
                return speciesId;
            }

            // 일반적인 경우: 하이픈 앞의 이름으로 species 추정
            // 예: "pikachu-gmax" -> species는 25 (pikachu)
            // 하지만 이건 복잡하므로 일단 pokemonId 사용
        }

        return pokemonId;
    }

    // 초기 데이터 로딩 (애플리케이션 시작 시 실행) - 개선됨
    public void loadInitialPokemonNames(int maxPokemon) {
        logger.info("포켓몬 이름 데이터 로딩 시작...");

        for (int i = 1; i <= maxPokemon; i++) {
            if (!pokemonNameRepository.existsByPokemonId(i)) {
                try {
                    // 먼저 기본 포켓몬 정보 가져오기
                    String url = POKEAPI_BASE_URL + i;
                    ResponseEntity<PokemonDto> response = restTemplate.getForEntity(url, PokemonDto.class);

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        PokemonDto pokemon = response.getBody();
                        savePokemonNameFromSpecies(i, pokemon.getName());
                    }

                    // API 호출 제한을 위한 딜레이
                    Thread.sleep(150); // 약간 늘림
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