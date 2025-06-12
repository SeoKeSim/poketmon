package sks.poketmon.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class PokemonNameMappingService {

    private Map<String, String> korToEngMap = new ConcurrentHashMap<>();
    private Map<String, String> engToKorMap = new ConcurrentHashMap<>();

    @Value("${pokemon.generation.max:9}") // 현재 9세대까지
    private int maxGeneration;

    // 세대별 포켓몬 수 (대략적)
    private static final Map<Integer, Integer> GENERATION_RANGES = Map.of(
            1, 151,   // 1세대: 1-151
            2, 251,   // 2세대: 152-251
            3, 386,   // 3세대: 252-386
            4, 493,   // 4세대: 387-493
            5, 649,   // 5세대: 494-649
            6, 721,   // 6세대: 650-721
            7, 809,   // 7세대: 722-809
            8, 905,   // 8세대: 810-905
            9, 1010   // 9세대: 906-1010
    );

    @PostConstruct
    public void loadMapping() {
        // 비동기로 모든 세대 로딩
        loadAllGenerationsAsync();
    }

    /**
     * 모든 세대를 비동기로 로딩
     */
    private void loadAllGenerationsAsync() {
        int maxPokemonId = GENERATION_RANGES.get(Math.min(maxGeneration, 9));

        // 병렬 처리로 빠르게 로딩
        List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, maxPokemonId)
                .boxed()
                .map(id -> CompletableFuture.runAsync(() -> loadSinglePokemon(id)))
                .toList();

        // 모든 비동기 작업 완료 대기 (백그라운드에서)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> System.out.println("포켓몬 이름 매핑 로딩 완료: " + korToEngMap.size() + "개"));
    }

    /**
     * 단일 포켓몬 정보 로딩
     */
    private void loadSinglePokemon(int id) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://pokeapi.co/api/v2/pokemon-species/" + id;

        try {
            PokemonSpeciesResponse response = restTemplate.getForObject(url, PokemonSpeciesResponse.class);
            if (response != null && response.names != null) {
                String korName = null;
                String engName = null;

                for (Name nameObj : response.names) {
                    if ("ko".equals(nameObj.language.name)) {
                        korName = nameObj.name;
                    } else if ("en".equals(nameObj.language.name)) {
                        engName = nameObj.name.toLowerCase();
                    }
                }

                if (korName != null && engName != null) {
                    korToEngMap.put(korName, engName);
                    engToKorMap.put(engName, korName);
                }
            }
        } catch (Exception e) {
            // 로그 레벨을 조정하거나 재시도 로직 추가 가능
            System.err.println("포켓몬 ID " + id + " 로딩 실패: " + e.getMessage());
        }
    }

    /**
     * 실시간 이름 변환 (캐시 활용)
     */
    @Cacheable("pokemonNames")
    public String getEnglishName(String korName) {
        // 1. 기존 매핑에서 찾기
        String cached = korToEngMap.get(korName);
        if (cached != null) {
            return cached;
        }

        // 2. 실시간 API 호출로 찾기
        return findNameFromAPI(korName, true);
    }

    /**
     * 영어 -> 한국어 변환
     */
    @Cacheable("pokemonNamesReverse")
    public String getKoreanName(String engName) {
        String cached = engToKorMap.get(engName.toLowerCase());
        if (cached != null) {
            return cached;
        }

        return findNameFromAPI(engName, false);
    }

    /**
     * API에서 실시간으로 이름 찾기
     */
    private String findNameFromAPI(String name, boolean korToEng) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 먼저 pokemon API로 시도 (영어 이름인 경우)
            if (!korToEng) {
                String pokemonUrl = "https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase();
                PokemonBasicResponse pokemon = restTemplate.getForObject(pokemonUrl, PokemonBasicResponse.class);
                if (pokemon != null) {
                    return getSpeciesName(pokemon.species.url, korToEng);
                }
            }

            // 전체 포켓몬 목록에서 검색 (비효율적이므로 최후 수단)
            return searchInAllPokemon(name, korToEng);

        } catch (Exception e) {
            System.err.println("실시간 이름 변환 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * species URL에서 이름 정보 가져오기
     */
    private String getSpeciesName(String speciesUrl, boolean korToEng) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            PokemonSpeciesResponse response = restTemplate.getForObject(speciesUrl, PokemonSpeciesResponse.class);
            if (response != null && response.names != null) {
                String targetLang = korToEng ? "en" : "ko";
                for (Name nameObj : response.names) {
                    if (targetLang.equals(nameObj.language.name)) {
                        String result = korToEng ? nameObj.name.toLowerCase() : nameObj.name;

                        // 캐시에 저장
                        if (korToEng) {
                            korToEngMap.put(nameObj.name, result);
                        } else {
                            engToKorMap.put(nameObj.name.toLowerCase(), result);
                        }

                        return result;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Species 정보 로딩 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 전체 포켓몬에서 검색 (최후 수단)
     */
    private String searchInAllPokemon(String name, boolean korToEng) {
        // 이 방법은 매우 비효율적이므로 실제로는 DB나 다른 방법 사용 권장
        System.out.println("전체 검색 모드 - 비효율적이므로 DB 사용을 권장합니다.");
        return null;
    }

    /**
     * 매핑 상태 확인
     */
    public Map<String, Object> getMappingStatus() {
        return Map.of(
                "koreanToEnglish", korToEngMap.size(),
                "englishToKorean", engToKorMap.size(),
                "maxGeneration", maxGeneration
        );
    }

    /**
     * 특정 세대만 로딩
     */
    public void loadGeneration(int generation) {
        if (generation < 1 || generation > 9) return;

        int startId = generation == 1 ? 1 : GENERATION_RANGES.get(generation - 1) + 1;
        int endId = GENERATION_RANGES.get(generation);

        IntStream.rangeClosed(startId, endId)
                .parallel()
                .forEach(this::loadSinglePokemon);
    }

    // 내부 클래스들
    static class PokemonSpeciesResponse {
        public Name[] names;
    }

    static class Name {
        public String name;
        public Language language;
    }

    static class Language {
        public String name;
    }

    static class PokemonBasicResponse {
        public Species species;
    }

    static class Species {
        public String url;
    }
}