package sks.poketmon.service;

import sks.poketmon.entity.PokemonNameMapping;
import sks.poketmon.repository.PokemonNameMappingRepository;
import sks.poketmon.dto.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DatabasePokemonMappingService {

    @Autowired
    private PokemonNameMappingRepository mappingRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String POKEMON_API_BASE = "https://pokeapi.co/api/v2";

    /**
     * 한국어 -> 영어 이름 변환 (DB 우선, 없으면 API 호출 후 저장)
     */
    @Cacheable(value = "pokemonKorToEng", key = "#koreanName")
    public String getEnglishName(String koreanName) {
        if (koreanName == null || koreanName.trim().isEmpty()) {
            return null;
        }

        // 1. DB에서 먼저 찾기
        Optional<PokemonNameMapping> mapping = mappingRepository.findByKoreanName(koreanName.trim());
        if (mapping.isPresent()) {
            return mapping.get().getEnglishName();
        }

        // 2. API에서 찾아서 DB에 저장
        return fetchAndSaveMapping(koreanName.trim());
    }

    /**
     * 영어 -> 한국어 이름 변환
     */
    @Cacheable(value = "pokemonEngToKor", key = "#englishName.toLowerCase()")
    public String getKoreanName(String englishName) {
        if (englishName == null || englishName.trim().isEmpty()) {
            return null;
        }

        Optional<PokemonNameMapping> mapping = mappingRepository.findByEnglishNameIgnoreCase(englishName.trim());
        if (mapping.isPresent()) {
            return mapping.get().getKoreanName();
        }

        // API에서 영어 이름으로 찾아서 저장
        return fetchMappingByEnglishName(englishName.trim());
    }

    /**
     * 부분 검색 - 한국어 이름
     */
    public List<PokemonNameMapping> searchByKoreanName(String partialName) {
        if (partialName == null || partialName.trim().isEmpty()) {
            return List.of();
        }
        return mappingRepository.findByKoreanNameContaining(partialName.trim());
    }

    /**
     * 부분 검색 - 영어 이름
     */
    public List<PokemonNameMapping> searchByEnglishName(String partialName) {
        if (partialName == null || partialName.trim().isEmpty()) {
            return List.of();
        }
        return mappingRepository.findByEnglishNameContainingIgnoreCase(partialName.trim());
    }

    /**
     * API에서 매핑 정보를 가져와 DB에 저장 (한국어 이름으로 검색)
     */
    @Transactional
    private String fetchAndSaveMapping(String koreanName) {
        try {
            // 전체 포켓몬 종족 목록에서 한국어 이름으로 검색
            String speciesListUrl = POKEMON_API_BASE + "/pokemon-species?limit=2000";
            PokemonListResponse listResponse = restTemplate.getForObject(speciesListUrl, PokemonListResponse.class);

            if (listResponse != null && listResponse.getResults() != null) {
                for (PokemonListResponse.PokemonListItem item : listResponse.getResults()) {
                    String koreanNameFound = findKoreanNameFromSpecies(item.getUrl());
                    if (koreanName.equals(koreanNameFound)) {
                        // 매핑 저장
                        PokemonNameMapping mapping = new PokemonNameMapping(koreanName, item.getName());
                        mappingRepository.save(mapping);
                        return item.getName();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("한국어 이름으로 매핑 정보 저장 실패: " + koreanName + ", 오류: " + e.getMessage());
        }
        return null;
    }

    /**
     * 영어 이름으로 매핑 정보 찾기
     */
    @Transactional
    private String fetchMappingByEnglishName(String englishName) {
        try {
            // pokemon-species API에서 직접 조회
            String speciesUrl = POKEMON_API_BASE + "/pokemon-species/" + englishName.toLowerCase();
            PokemonSpeciesResponse species = restTemplate.getForObject(speciesUrl, PokemonSpeciesResponse.class);

            if (species != null && species.getNames() != null) {
                for (PokemonSpeciesResponse.NameEntry nameEntry : species.getNames()) {
                    if ("ko".equals(nameEntry.getLanguage().getName())) {
                        String koreanName = nameEntry.getName();

                        // DB에 저장
                        PokemonNameMapping mapping = new PokemonNameMapping(koreanName, englishName.toLowerCase());
                        mappingRepository.save(mapping);

                        return koreanName;
                    }
                }
            }
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("포켓몬을 찾을 수 없음: " + englishName);
        } catch (Exception e) {
            System.err.println("영어 이름으로 매핑 정보 찾기 실패: " + englishName + ", 오류: " + e.getMessage());
        }
        return null;
    }

    /**
     * 특정 종족 URL에서 한국어 이름 추출
     */
    private String findKoreanNameFromSpecies(String speciesUrl) {
        try {
            PokemonSpeciesResponse species = restTemplate.getForObject(speciesUrl, PokemonSpeciesResponse.class);
            if (species != null && species.getNames() != null) {
                for (PokemonSpeciesResponse.NameEntry nameEntry : species.getNames()) {
                    if ("ko".equals(nameEntry.getLanguage().getName())) {
                        return nameEntry.getName();
                    }
                }
            }
        } catch (Exception e) {
            // 조용히 무시 (대량 처리 중 일부 실패는 허용)
        }
        return null;
    }

    /**
     * 주기적으로 새로운 포켓몬 데이터 업데이트 (24시간마다)
     */
    @Scheduled(fixedRate = 86400000)
    @Async
    public void updatePokemonMappings() {
        System.out.println("포켓몬 매핑 데이터 업데이트 시작...");

        try {
            String listUrl = POKEMON_API_BASE + "/pokemon-species?limit=2000";
            PokemonListResponse response = restTemplate.getForObject(listUrl, PokemonListResponse.class);

            if (response != null && response.getResults() != null) {
                List<CompletableFuture<Void>> futures = response.getResults().stream()
                        .filter(item -> !mappingRepository.existsByEnglishNameIgnoreCase(item.getName()))
                        .map(item -> updateSinglePokemonMapping(item.getUrl()))
                        .collect(Collectors.toList());

                // 모든 비동기 작업 완료 대기
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            System.out.println("포켓몬 매핑 데이터 업데이트 완료");
        } catch (Exception e) {
            System.err.println("정기 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 단일 포켓몬 매핑 업데이트
     */
    @Async
    @Transactional
    public CompletableFuture<Void> updateSinglePokemonMapping(String speciesUrl) {
        try {
            PokemonSpeciesResponse species = restTemplate.getForObject(speciesUrl, PokemonSpeciesResponse.class);
            if (species != null && species.getNames() != null) {
                String koreanName = null;
                String englishName = null;

                for (PokemonSpeciesResponse.NameEntry nameEntry : species.getNames()) {
                    if ("ko".equals(nameEntry.getLanguage().getName())) {
                        koreanName = nameEntry.getName();
                    } else if ("en".equals(nameEntry.getLanguage().getName())) {
                        englishName = nameEntry.getName();
                    }
                }

                if (koreanName != null && englishName != null) {
                    // 중복 체크 후 저장
                    if (!mappingRepository.existsByKoreanNameAndEnglishNameIgnoreCase(koreanName, englishName)) {
                        PokemonNameMapping mapping = new PokemonNameMapping(koreanName, englishName.toLowerCase());
                        mappingRepository.save(mapping);
                        System.out.println("새 매핑 추가: " + koreanName + " -> " + englishName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("단일 포켓몬 매핑 업데이트 실패: " + speciesUrl + ", 오류: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 매핑 통계 조회
     */
    public MappingStatsResponse getMappingStats() {
        long totalMappings = mappingRepository.countAllMappings();
        long todayMappings = mappingRepository.countTodayMappings();
        List<PokemonNameMapping> recentMappings = mappingRepository.findTop10ByOrderByCreatedAtDesc();

        return new MappingStatsResponse(totalMappings, todayMappings, recentMappings);
    }

    /**
     * 수동으로 매핑 추가
     */
    @Transactional
    public PokemonNameMapping addManualMapping(String koreanName, String englishName) {
        if (koreanName == null || englishName == null ||
                koreanName.trim().isEmpty() || englishName.trim().isEmpty()) {
            throw new IllegalArgumentException("한국어 이름과 영어 이름은 필수입니다.");
        }

        // 중복 체크
        if (mappingRepository.existsByKoreanNameAndEnglishNameIgnoreCase(koreanName.trim(), englishName.trim())) {
            throw new IllegalArgumentException("이미 존재하는 매핑입니다.");
        }

        PokemonNameMapping mapping = new PokemonNameMapping(koreanName.trim(), englishName.trim().toLowerCase());
        return mappingRepository.save(mapping);
    }

    /**
     * 중복 데이터 정리
     */
    @Transactional
    public void cleanupDuplicateData() {
        List<String> duplicateKoreanNames = mappingRepository.findDuplicateKoreanNames();
        List<String> duplicateEnglishNames = mappingRepository.findDuplicateEnglishNames();

        System.out.println("중복된 한국어 이름: " + duplicateKoreanNames.size() + "개");
        System.out.println("중복된 영어 이름: " + duplicateEnglishNames.size() + "개");

        // 실제 중복 제거 로직은 비즈니스 규칙에 따라 구현
        // 예: 가장 최근 것만 남기고 나머지 삭제
    }

    /**
     * 대량 매핑 조회 (성능 최적화)
     */
    public List<PokemonNameMapping> findMappingsByEnglishNames(List<String> englishNames) {
        if (englishNames == null || englishNames.isEmpty()) {
            return List.of();
        }

        List<String> lowerCaseNames = englishNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return mappingRepository.findByEnglishNameInIgnoreCase(lowerCaseNames);
    }

    /**
     * 대량 매핑 조회 (한국어)
     */
    public List<PokemonNameMapping> findMappingsByKoreanNames(List<String> koreanNames) {
        if (koreanNames == null || koreanNames.isEmpty()) {
            return List.of();
        }

        List<String> trimmedNames = koreanNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

        return mappingRepository.findByKoreanNameIn(trimmedNames);
    }
}