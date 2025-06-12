package sks.poketmon.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pokemon")
public class Pokemon {

    @Id
    @Column(name = "pokemon_id")
    private Long pokemonId;

    @Column(name = "pokemon_name", nullable = false, length = 100)
    private String pokemonName;

    @Column(name = "pokemon_type1", nullable = false, length = 20)
    private String pokemonType1;

    @Column(name = "pokemon_type2", length = 20)
    private String pokemonType2;

    @Column(name = "pokemon_image_url", length = 500)
    private String pokemonImageUrl;

    @Column(name = "is_legendary")
    private Boolean isLegendary = false;

    @Column(name = "is_mythical")
    private Boolean isMythical = false;

    private Integer height;

    private Integer weight;

    @Column(name = "base_experience")
    private Integer baseExperience;

    @Column(name = "rdate")
    private LocalDateTime rdate = LocalDateTime.now();

    // 기본 생성자
    public Pokemon() {}

    // 생성자
    public Pokemon(Long pokemonId, String pokemonName, String pokemonType1) {
        this.pokemonId = pokemonId;
        this.pokemonName = pokemonName;
        this.pokemonType1 = pokemonType1;
    }

    // Getters and Setters
    public Long getPokemonId() { return pokemonId; }
    public void setPokemonId(Long pokemonId) { this.pokemonId = pokemonId; }

    public String getPokemonName() { return pokemonName; }
    public void setPokemonName(String pokemonName) { this.pokemonName = pokemonName; }

    public String getPokemonType1() { return pokemonType1; }
    public void setPokemonType1(String pokemonType1) { this.pokemonType1 = pokemonType1; }

    public String getPokemonType2() { return pokemonType2; }
    public void setPokemonType2(String pokemonType2) { this.pokemonType2 = pokemonType2; }

    public String getPokemonImageUrl() { return pokemonImageUrl; }
    public void setPokemonImageUrl(String pokemonImageUrl) { this.pokemonImageUrl = pokemonImageUrl; }

    public Boolean getIsLegendary() { return isLegendary; }
    public void setIsLegendary(Boolean isLegendary) { this.isLegendary = isLegendary; }

    public Boolean getIsMythical() { return isMythical; }
    public void setIsMythical(Boolean isMythical) { this.isMythical = isMythical; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Integer getBaseExperience() { return baseExperience; }
    public void setBaseExperience(Integer baseExperience) { this.baseExperience = baseExperience; }

    public LocalDateTime getRdate() { return rdate; }
    public void setRdate(LocalDateTime rdate) { this.rdate = rdate; }
}