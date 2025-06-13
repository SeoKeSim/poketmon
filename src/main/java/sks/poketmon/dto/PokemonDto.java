package sks.poketmon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokemonDto {

    private int id;
    private String name;
    private String koreanName;
    private int height;
    private int weight;
    private List<TypeDto> types;
    private List<StatDto> stats;
    private SpriteDto sprites;

    // 기본 생성자
    public PokemonDto() {}

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

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<TypeDto> getTypes() {
        return types;
    }

    public void setTypes(List<TypeDto> types) {
        this.types = types;
    }

    public List<StatDto> getStats() {
        return stats;
    }

    public void setStats(List<StatDto> stats) {
        this.stats = stats;
    }

    public SpriteDto getSprites() {
        return sprites;
    }

    public void setSprites(SpriteDto sprites) {
        this.sprites = sprites;
    }

    // 내부 클래스들
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeDto {
        private int slot;
        private TypeInfoDto type;

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public TypeInfoDto getType() {
            return type;
        }

        public void setType(TypeInfoDto type) {
            this.type = type;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TypeInfoDto {
            private String name;
            private String url;
            private String koreanName; // 추가된 필드

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

            public String getKoreanName() {
                return koreanName;
            }

            public void setKoreanName(String koreanName) {
                this.koreanName = koreanName;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatDto {
        @JsonProperty("base_stat")
        private int baseStat;
        private int effort;
        private StatInfoDto stat;

        public int getBaseStat() {
            return baseStat;
        }

        public void setBaseStat(int baseStat) {
            this.baseStat = baseStat;
        }

        public int getEffort() {
            return effort;
        }

        public void setEffort(int effort) {
            this.effort = effort;
        }

        public StatInfoDto getStat() {
            return stat;
        }

        public void setStat(StatInfoDto stat) {
            this.stat = stat;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatInfoDto {
            private String name;
            private String url;
            private String koreanName; // 추가된 필드

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

            public String getKoreanName() {
                return koreanName;
            }

            public void setKoreanName(String koreanName) {
                this.koreanName = koreanName;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpriteDto {
        @JsonProperty("front_default")
        private String frontDefault;
        @JsonProperty("front_shiny")
        private String frontShiny;
        private OtherDto other;

        public String getFrontDefault() {
            return frontDefault;
        }

        public void setFrontDefault(String frontDefault) {
            this.frontDefault = frontDefault;
        }

        public String getFrontShiny() {
            return frontShiny;
        }

        public void setFrontShiny(String frontShiny) {
            this.frontShiny = frontShiny;
        }

        public OtherDto getOther() {
            return other;
        }

        public void setOther(OtherDto other) {
            this.other = other;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class OtherDto {
            @JsonProperty("official-artwork")
            private OfficialArtworkDto officialArtwork;

            public OfficialArtworkDto getOfficialArtwork() {
                return officialArtwork;
            }

            public void setOfficialArtwork(OfficialArtworkDto officialArtwork) {
                this.officialArtwork = officialArtwork;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class OfficialArtworkDto {
                @JsonProperty("front_default")
                private String frontDefault;

                public String getFrontDefault() {
                    return frontDefault;
                }

                public void setFrontDefault(String frontDefault) {
                    this.frontDefault = frontDefault;
                }
            }
        }
    }
}