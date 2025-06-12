package sks.poketmon.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonBasicResponse {
    private Long id;
    private String name;
    private Integer height;
    private Integer weight;
    private SpeciesReference species;

    public PokemonBasicResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public SpeciesReference getSpecies() { return species; }
    public void setSpecies(SpeciesReference species) { this.species = species; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpeciesReference {
        private String name;
        private String url;

        public SpeciesReference() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
