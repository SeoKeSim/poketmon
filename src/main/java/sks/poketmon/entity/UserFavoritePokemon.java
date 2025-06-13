package sks.poketmon.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite_pokemon",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_code", "pokemon_id"}))
public class UserFavoritePokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code", nullable = false)
    private Long userCode;

    @Column(name = "pokemon_id", nullable = false)
    private Integer pokemonId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 기본 생성자
    public UserFavoritePokemon() {}

    // 생성자
    public UserFavoritePokemon(Long userCode, Integer pokemonId) {
        this.userCode = userCode;
        this.pokemonId = pokemonId;
        this.createdAt = LocalDateTime.now();
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserCode() {
        return userCode;
    }

    public void setUserCode(Long userCode) {
        this.userCode = userCode;
    }

    public Integer getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(Integer pokemonId) {
        this.pokemonId = pokemonId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}