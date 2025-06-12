package sks.poketmon.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pokemon_name_mapping",
        indexes = {
                @Index(name = "idx_korean_name", columnList = "korean_name"),
                @Index(name = "idx_english_name", columnList = "english_name"),
                @Index(name = "idx_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"korean_name", "english_name"})
        })
public class PokemonNameMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korean_name", nullable = false, length = 100)
    private String koreanName;

    @Column(name = "english_name", nullable = false, length = 100)
    private String englishName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version; // 낙관적 락킹용

    // 기본 생성자
    public PokemonNameMapping() {}

    // 편의 생성자
    public PokemonNameMapping(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }

    // equals and hashCode (비즈니스 키 기반)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PokemonNameMapping that = (PokemonNameMapping) o;
        return Objects.equals(koreanName, that.koreanName) &&
                Objects.equals(englishName, that.englishName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(koreanName, englishName);
    }

    @Override
    public String toString() {
        return "PokemonNameMapping{" +
                "id=" + id +
                ", koreanName='" + koreanName + '\'' +
                ", englishName='" + englishName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}