package sks.poketmon.dto;

public class FavoriteRequestDto {

    private Integer pokemonId;

    // 기본 생성자
    public FavoriteRequestDto() {}

    // 생성자
    public FavoriteRequestDto(Integer pokemonId) {
        this.pokemonId = pokemonId;
    }

    // Getter/Setter
    public Integer getPokemonId() {
        return pokemonId;
    }

    public void setPokemonId(Integer pokemonId) {
        this.pokemonId = pokemonId;
    }
}