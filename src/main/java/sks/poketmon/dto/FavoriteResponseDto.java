package sks.poketmon.dto;

import java.time.LocalDateTime;

public class FavoriteResponseDto {

    private Long id;
    private Integer pokemonId;
    private String pokemonName;
    private String pokemonKoreanName;
    private String pokemonImageUrl;
    private LocalDateTime createdAt;

    // 기본 생성자
    public FavoriteResponseDto() {}

    // 생성자
    public FavoriteResponseDto(Long id, Integer pokemonId, String pokemonName,
                               String pokemonKoreanName, String pokemonImageUrl,
                               LocalDateTime createdAt) {
        this.id = id;
        this.pokemonId = pokemonId;
        this.pokemonName = pokemonName;
        this.pokemonKoreanName = pokemonKoreanName;
        this.pokemonImageUrl = pokemonImageUrl;
        this.createdAt = createdAt;
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(Integer pokemonId) {
        this.pokemonId = pokemonId;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public void setPokemonName(String pokemonName) {
        this.pokemonName = pokemonName;
    }

    public String getPokemonKoreanName() {
        return pokemonKoreanName;
    }

    public void setPokemonKoreanName(String pokemonKoreanName) {
        this.pokemonKoreanName = pokemonKoreanName;
    }

    public String getPokemonImageUrl() {
        return pokemonImageUrl;
    }

    public void setPokemonImageUrl(String pokemonImageUrl) {
        this.pokemonImageUrl = pokemonImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}