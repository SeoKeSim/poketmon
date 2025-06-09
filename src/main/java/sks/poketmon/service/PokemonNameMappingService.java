package sks.poketmon.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PokemonNameMappingService {

    private Map<String, String> korToEngMap = new HashMap<>();

    @PostConstruct
    public void loadMapping() {
        RestTemplate restTemplate = new RestTemplate();

        for (int id = 1; id <= 151; id++) { // 1세대 포켓몬만 예시
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
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading species id: " + id);
            }
        }
    }

    public String getEnglishName(String korName) {
        return korToEngMap.get(korName);
    }

    // 내부 클래스 정의는 응답 JSON에 맞게 선언해야 함
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
}
