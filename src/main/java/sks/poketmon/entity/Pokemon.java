package sks.poketmon.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pokemon", indexes = {
        @Index(name = "idx_pokemon_name", columnList = "pokemon_name"),
        @Index(name = "idx_pokemon_type1", columnList = "pokemon_type1"),
        @Index(name = "idx_pokemon_type2", columnList = "pokemon_type2"),
        @Index(name = "idx_legendary", columnList = "is_legendary"),
        @Index(name = "idx_mythical", columnList = "is_mythical")
})
public class Pokemon {

    @Id
    @Column(name = "pokemon_id")
    private Long pokemonId; // PokeAPI의 ID를 그대로 사용

    @Column(name = "pokemon_name", nullable = false, length = 100, unique = true)
    private String pokemonName;

    @Column(name = "pokemon_name_korean", length = 100)
    private String pokemonNameKorean; // 한국어 이름 추가

    @Enumerated(EnumType.STRING)
    @Column(name = "pokemon_type1", nullable = false, length = 20)
    private PokemonType pokemonType1;

    @Enumerated(EnumType.STRING)
    @Column(name = "pokemon_type2", length = 20)
    private PokemonType pokemonType2;

    @Column(name = "pokemon_image_url", length = 500)
    private String pokemonImageUrl;

    @Column(name = "pokemon_image_shiny_url", length = 500)
    private String pokemonImageShinyUrl; // 색이 다른 포켓몬 이미지

    @Column(name = "is_legendary", nullable = false)
    private Boolean isLegendary = false;

    @Column(name = "is_mythical", nullable = false)
    private Boolean isMythical = false;

    @Column(name = "height")
    private Integer height; // 데시미터 단위

    @Column(name = "weight")
    private Integer weight; // 헥토그램 단위

    @Column(name = "base_experience")
    private Integer baseExperience;

    @Column(name = "generation")
    private Integer generation; // 몇 세대 포켓몬인지

    @Column(name = "species_id")
    private Integer speciesId; // species API 용

    @Column(name = "order_number")
    private Integer orderNumber; // 도감 순서

    // 능력치 추가
    @Column(name = "hp")
    private Integer hp;

    @Column(name = "attack")
    private Integer attack;

    @Column(name = "defense")
    private Integer defense;

    @Column(name = "special_attack")
    private Integer specialAttack;

    @Column(name = "special_defense")
    private Integer specialDefense;

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "total_stats")
    private Integer totalStats; // 능력치 총합

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version; // 낙관적 락킹용

    // 기본 생성자
    public Pokemon() {}

    // 필수 필드 생성자
    public Pokemon(Long pokemonId, String pokemonName, PokemonType pokemonType1) {
        this.pokemonId = pokemonId;
        this.pokemonName = pokemonName;
        this.pokemonType1 = pokemonType1;
    }

    // 편의 메서드들
    public boolean hasSecondType() {
        return pokemonType2 != null;
    }

    public boolean isSpecial() {
        return isLegendary || isMythical;
    }

    public void calculateTotalStats() {
        if (hp != null && attack != null && defense != null &&
                specialAttack != null && specialDefense != null && speed != null) {
            this.totalStats = hp + attack + defense + specialAttack + specialDefense + speed;
        }
    }

    // equals and hashCode (ID 기반)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pokemon pokemon = (Pokemon) o;
        return Objects.equals(pokemonId, pokemon.pokemonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pokemonId);
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "pokemonId=" + pokemonId +
                ", pokemonName='" + pokemonName + '\'' +
                ", pokemonType1=" + pokemonType1 +
                ", pokemonType2=" + pokemonType2 +
                ", isLegendary=" + isLegendary +
                ", isMythical=" + isMythical +
                '}';
    }

    // Getters and Setters
    public Long getPokemonId() { return pokemonId; }
    public void setPokemonId(Long pokemonId) { this.pokemonId = pokemonId; }

    public String getPokemonName() { return pokemonName; }
    public void setPokemonName(String pokemonName) { this.pokemonName = pokemonName; }

    public String getPokemonNameKorean() { return pokemonNameKorean; }
    public void setPokemonNameKorean(String pokemonNameKorean) { this.pokemonNameKorean = pokemonNameKorean; }

    public PokemonType getPokemonType1() { return pokemonType1; }
    public void setPokemonType1(PokemonType pokemonType1) { this.pokemonType1 = pokemonType1; }

    public PokemonType getPokemonType2() { return pokemonType2; }
    public void setPokemonType2(PokemonType pokemonType2) { this.pokemonType2 = pokemonType2; }

    public String getPokemonImageUrl() { return pokemonImageUrl; }
    public void setPokemonImageUrl(String pokemonImageUrl) { this.pokemonImageUrl = pokemonImageUrl; }

    public String getPokemonImageShinyUrl() { return pokemonImageShinyUrl; }
    public void setPokemonImageShinyUrl(String pokemonImageShinyUrl) { this.pokemonImageShinyUrl = pokemonImageShinyUrl; }

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

    public Integer getGeneration() { return generation; }
    public void setGeneration(Integer generation) { this.generation = generation; }

    public Integer getSpeciesId() { return speciesId; }
    public void setSpeciesId(Integer speciesId) { this.speciesId = speciesId; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }

    public Integer getHp() { return hp; }
    public void setHp(Integer hp) {
        this.hp = hp;
        calculateTotalStats();
    }

    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) {
        this.attack = attack;
        calculateTotalStats();
    }

    public Integer getDefense() { return defense; }
    public void setDefense(Integer defense) {
        this.defense = defense;
        calculateTotalStats();
    }

    public Integer getSpecialAttack() { return specialAttack; }
    public void setSpecialAttack(Integer specialAttack) {
        this.specialAttack = specialAttack;
        calculateTotalStats();
    }

    public Integer getSpecialDefense() { return specialDefense; }
    public void setSpecialDefense(Integer specialDefense) {
        this.specialDefense = specialDefense;
        calculateTotalStats();
    }

    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) {
        this.speed = speed;
        calculateTotalStats();
    }

    public Integer getTotalStats() { return totalStats; }
    public void setTotalStats(Integer totalStats) { this.totalStats = totalStats; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

// 포켓몬 타입 Enum
enum PokemonType {
    NORMAL("노말"),
    FIRE("불꽃"),
    WATER("물"),
    ELECTRIC("전기"),
    GRASS("풀"),
    ICE("얼음"),
    FIGHTING("격투"),
    POISON("독"),
    GROUND("땅"),
    FLYING("비행"),
    PSYCHIC("에스퍼"),
    BUG("벌레"),
    ROCK("바위"),
    GHOST("고스트"),
    DRAGON("드래곤"),
    DARK("악"),
    STEEL("강철"),
    FAIRY("페어리");

    private final String koreanName;

    PokemonType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static PokemonType fromString(String type) {
        for (PokemonType pokemonType : PokemonType.values()) {
            if (pokemonType.name().equalsIgnoreCase(type)) {
                return pokemonType;
            }
        }
        throw new IllegalArgumentException("Unknown Pokemon type: " + type);
    }
}