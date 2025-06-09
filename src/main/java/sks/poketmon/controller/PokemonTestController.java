package sks.poketmon.controller;

import sks.poketmon.service.PokemonTestService;
import sks.poketmon.dto.PokemonTestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PokemonTestController {
    @Autowired
    private PokemonTestService pokemonTestService;

    @GetMapping("/")
    public String form() {
        return "testsearch"; // 검색 폼
    }

    @PostMapping("/search")
    public String getPokemon(@RequestParam String name, Model model) {
        try {
            PokemonTestDTO pokemon = pokemonTestService.getPokemon(name);
            model.addAttribute("pokemon", pokemon);
            return "testresult"; // 결과 보여주는 페이지
        } catch (Exception e) {
            model.addAttribute("error", "포켓몬을 찾을 수 없습니다.");
            return "testsearch";
        }
    }
}
