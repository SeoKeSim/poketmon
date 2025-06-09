package sks.poketmon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pokemon_info")  // MySQL 테이블명과 맞춤
public class Test {
    @Id
    private int id;

    private String name;

    private String koreanName;

    private String imageUrl;

    // 기본 생성자
    public PokemonTestDTO() {}

    // getter, setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKoreanName() { return koreanName; }
    public void setKoreanName(String koreanName) { this.koreanName = koreanName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
