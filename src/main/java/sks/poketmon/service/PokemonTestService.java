package sks.poketmon.service;

import sks.poketmon.dto.PokemonTestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PokemonTestService {
    public PokemonTestDTO getPokemon(String name) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase();
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, PokemonTestDTO.class);
    }
}
