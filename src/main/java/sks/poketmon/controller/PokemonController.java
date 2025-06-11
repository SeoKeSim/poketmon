package sks.poketmon.controller;

import sks.poketmon.service.PokemonService;
import sks.poketmon.entity.Pokemon;
import sks.poketmon.dto.PokemonTestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/")
    public String form() {
        return "testsearch"; // 검색 폼
    }

    /**
     * 포켓몬 검색 (한국어/영어 지원)
     */
    @PostMapping("/search")
    public String getPokemon(@RequestParam String name, Model model) {
        try {
            PokemonTestDTO pokemon = pokemonService.getPokemonByKoreanName(name);
            model.addAttribute("pokemon", pokemon);
            return "testresult";
        } catch (Exception e) {
            model.addAttribute("error", "포켓몬을 찾을 수 없습니다: " + name);
            return "testsearch";
        }
    }

    /**
     * 모든 포켓몬 목록 조회
     */
    @GetMapping("/pokemon/list")
    public String getAllPokemon(Model model) {
        List<Pokemon> pokemonList = pokemonService.getAllPokemon();
        model.addAttribute("pokemonList", pokemonList);
        return "pokemon-list";
    }

    /**
     * 타입별 포켓몬 검색
     */
    @GetMapping("/pokemon/type/{type}")
    public String getPokemonByType(@PathVariable String type, Model model) {
        List<Pokemon> pokemonList = pokemonService.getPokemonByType(type);
        model.addAttribute("pokemonList", pokemonList);
        model.addAttribute("searchType", type);
        return "pokemon-list";
    }

    /**
     * 전설의 포켓몬들
     */
    @GetMapping("/pokemon/legendary")
    public String getLegendaryPokemon(Model model) {
        List<Pokemon> pokemonList = pokemonService.getLegendaryPokemon();
        model.addAttribute("pokemonList", pokemonList);
        model.addAttribute("category", "전설의 포켓몬");
        return "pokemon-list";
    }

    /**
     * 환상의 포켓몬들
     */
    @GetMapping("/pokemon/mythical")
    public String getMythicalPokemon(Model model) {
        List<Pokemon> pokemonList = pokemonService.getMythicalPokemon();
        model.addAttribute("pokemonList", pokemonList);
        model.addAttribute("category", "환상의 포켓몬");
        return "pokemon-list";
    }

    /**
     * 포켓몬 이름으로 부분 검색
     */
    @GetMapping("/pokemon/search")
    public String searchPokemon(@RequestParam String query, Model model) {
        List<Pokemon> pokemonList = pokemonService.searchPokemonByName(query);
        model.addAttribute("pokemonList", pokemonList);
        model.addAttribute("searchQuery", query);
        return "pokemon-list";
    }

    /**
     * REST API - JSON 응답
     */
    @GetMapping("/api/pokemon/{name}")
    @ResponseBody
    public PokemonTestDTO getPokemonAPI(@PathVariable String name) {
        return pokemonService.getPokemonByKoreanName(name);
    }

    /**
     * REST API - 모든 포켓몬 JSON 응답
     */
    @GetMapping("/api/pokemon")
    @ResponseBody
    public List<Pokemon> getAllPokemonAPI() {
        return pokemonService.getAllPokemon();
    }
}