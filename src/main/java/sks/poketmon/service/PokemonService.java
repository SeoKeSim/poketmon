package sks.poketmon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import sks.poketmon.dto.PokemonDto;
import java.util.HashMap;
import java.util.Map;

@Service
public class PokemonService {

    private final RestTemplate restTemplate;
    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/pokemon/";

    // 한국어 매핑 (일부 포켓몬만 예시)
    private static final Map<String, String> KOREAN_TO_ENGLISH = new HashMap<>();

    static {
        // 1세대 포켓몬 한글 매핑 (예시)
        KOREAN_TO_ENGLISH.put("피카츄", "pikachu");
        KOREAN_TO_ENGLISH.put("라이츄", "raichu");
        KOREAN_TO_ENGLISH.put("파이리", "charmander");
        KOREAN_TO_ENGLISH.put("리자드", "charmeleon");
        KOREAN_TO_ENGLISH.put("리자몽", "charizard");
        KOREAN_TO_ENGLISH.put("꼬부기", "squirtle");
        KOREAN_TO_ENGLISH.put("어니부기", "wartortle");
        KOREAN_TO_ENGLISH.put("거북왕", "blastoise");
        KOREAN_TO_ENGLISH.put("이상해씨", "bulbasaur");
        KOREAN_TO_ENGLISH.put("이상해풀", "ivysaur");
        KOREAN_TO_ENGLISH.put("이상해꽃", "venusaur");
        KOREAN_TO_ENGLISH.put("캐터피", "caterpie");
        KOREAN_TO_ENGLISH.put("단데기", "metapod");
        KOREAN_TO_ENGLISH.put("버터플", "butterfree");
        KOREAN_TO_ENGLISH.put("뿔충이", "weedle");
        KOREAN_TO_ENGLISH.put("딱충이", "kakuna");
        KOREAN_TO_ENGLISH.put("독침봉", "beedrill");
        KOREAN_TO_ENGLISH.put("구구", "pidgey");
        KOREAN_TO_ENGLISH.put("피죤", "pidgeotto");
        KOREAN_TO_ENGLISH.put("피죤투", "pidgeot");
        KOREAN_TO_ENGLISH.put("꼬렛", "rattata");
        KOREAN_TO_ENGLISH.put("레트라", "raticate");
        KOREAN_TO_ENGLISH.put("깨비참", "spearow");
        KOREAN_TO_ENGLISH.put("깨비드릴조", "fearow");
        KOREAN_TO_ENGLISH.put("아보", "ekans");
        KOREAN_TO_ENGLISH.put("아보크", "arbok");
        KOREAN_TO_ENGLISH.put("야돈", "slowpoke");
        KOREAN_TO_ENGLISH.put("야도란", "slowbro");
        KOREAN_TO_ENGLISH.put("마임맨", "mr-mime");
        KOREAN_TO_ENGLISH.put("망나뇽", "dragonite");
        KOREAN_TO_ENGLISH.put("뮤", "mew");
        KOREAN_TO_ENGLISH.put("뮤츠", "mewtwo");
        KOREAN_TO_ENGLISH.put("잠만보", "snorlax");
        KOREAN_TO_ENGLISH.put("롱스톤", "onix");
    }

    @Autowired
    public PokemonService() {
        this.restTemplate = new RestTemplate();
    }

    public PokemonDto searchPokemon(String query) {
        try {
            // 한글 입력인 경우 영어로 변환
            String searchTerm = convertToEnglish(query);

            // 영어나 숫자가 아닌 경우 소문자로 변환
            searchTerm = searchTerm.toLowerCase().trim();

            String url = POKEAPI_BASE_URL + searchTerm;
            PokemonDto pokemon = restTemplate.getForObject(url, PokemonDto.class);

            return pokemon;

        } catch (HttpClientErrorException.NotFound e) {
            // 포켓몬을 찾을 수 없는 경우
            return null;
        } catch (Exception e) {
            // 기타 오류
            throw new RuntimeException("포켓몬 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String convertToEnglish(String koreanName) {
        // 한국어 매핑에서 찾기
        if (KOREAN_TO_ENGLISH.containsKey(koreanName)) {
            return KOREAN_TO_ENGLISH.get(koreanName);
        }

        // 매핑에 없으면 원래 값 반환 (영어나 숫자일 수 있음)
        return koreanName;
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