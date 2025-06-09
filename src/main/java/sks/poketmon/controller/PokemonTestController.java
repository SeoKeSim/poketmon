package sks.poketmon.controller;

import sks.poketmon.service.PokemonTestService;
import sks.poketmon.dto.PokemonTestDTO;
import sks.poketmon.service.PokemonNameMappingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PokemonTestController {
    @Autowired
    private PokemonTestService pokemonTestService;

    @Autowired
    private PokemonNameMappingService nameMappingService;

    @GetMapping("/")
    public String form() {
        return "testsearch"; // 검색 폼
    }

    @PostMapping("/search")
    public String getPokemon(@RequestParam String name, Model model) {
        try {
            // 입력된 이름이 한국어일 수도 있으니, 영어 이름으로 변환 시도
            String engName = nameMappingService.getEnglishName(name);
            if (engName == null) {
                // 매핑 없으면 입력값 그대로 사용 (영어 이름이나 ID일 수도 있으니까)
                engName = name.toLowerCase();
            }

            PokemonTestDTO pokemon = pokemonTestService.getPokemon(engName);
            model.addAttribute("pokemon", pokemon);
            return "testresult";
        } catch (Exception e) {
            model.addAttribute("error", "포켓몬을 찾을 수 없습니다.");
            return "testsearch";
        }
    }
}
