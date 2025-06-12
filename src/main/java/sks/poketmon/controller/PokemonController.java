package sks.poketmon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sks.poketmon.dto.PokemonDto;
import sks.poketmon.service.PokemonService;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    // 메인 페이지 (검색 폼)
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 포켓몬 검색 처리
    @PostMapping("/search")
    public String searchPokemon(@RequestParam("query") String query, Model model) {

        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("error", "검색어를 입력해주세요.");
            return "index";
        }

        try {
            PokemonDto pokemon = pokemonService.searchPokemon(query);

            if (pokemon == null) {
                model.addAttribute("error", "'" + query + "'에 해당하는 포켓몬을 찾을 수 없습니다.");
                return "index";
            }

            model.addAttribute("pokemon", pokemon);
            model.addAttribute("pokemonService", pokemonService); // 한국어 변환을 위해 추가
            return "result";

        } catch (Exception e) {
            model.addAttribute("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
            return "index";
        }
    }

    // GET 방식으로 검색 페이지 접근 시 메인으로 리다이렉트
    @GetMapping("/search")
    public String searchGet() {
        return "redirect:/";
    }
}