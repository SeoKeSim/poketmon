package sks.poketmon.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonSpeciesResponse {
    private Long id;
    private String name;
    private List<NameEntry> names;

    // 기본 생성자
    public PokemonSpeciesResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<NameEntry> getNames() { return names; }
    public void setNames(List<NameEntry> names) { this.names = names; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameEntry {
        private String name;
        private Language language;

        public NameEntry() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Language getLanguage() { return language; }
        public void setLanguage(Language language) { this.language = language; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Language {
        private String name;
        private String url;

        public Language() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}