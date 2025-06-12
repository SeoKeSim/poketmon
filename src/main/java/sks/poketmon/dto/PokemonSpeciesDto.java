package sks.poketmon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonSpeciesDto {

    private int id;
    private String name;
    private List<NameDto> names;

    // 기본 생성자
    public PokemonSpeciesDto() {}

    // Getter/Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NameDto> getNames() {
        return names;
    }

    public void setNames(List<NameDto> names) {
        this.names = names;
    }

    // 특정 언어의 이름 가져오기
    public String getNameByLanguage(String languageCode) {
        if (names == null) return name;

        return names.stream()
                .filter(nameDto -> languageCode.equals(nameDto.getLanguage().getName()))
                .map(NameDto::getName)
                .findFirst()
                .orElse(name);
    }

    // 한국어 이름 가져오기
    public String getKoreanName() {
        return getNameByLanguage("ko");
    }

    // 내부 클래스들
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameDto {
        private String name;
        private LanguageDto language;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LanguageDto getLanguage() {
            return language;
        }

        public void setLanguage(LanguageDto language) {
            this.language = language;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class LanguageDto {
            private String name;
            private String url;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}