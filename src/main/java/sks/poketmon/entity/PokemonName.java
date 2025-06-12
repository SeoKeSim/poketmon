package sks.poketmon.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_names")
public class PokemonName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pokemon_id")
    private Integer pokemonId;

    @Column(name = "english_name")
    private String englishName;

    @Column(name = "korean_name")
    private String koreanName;

    @Column(name = "language_code")
    private String languageCode;

    // 기본 생성자
    public PokemonName() {}

    // 생성자
    public PokemonName(Integer pokemonId, String englishName, String koreanName, String languageCode) {
        this.pokemonId = pokemonId;
        this.englishName = englishName;
        this.koreanName = koreanName;
        this.languageCode = languageCode;
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

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}