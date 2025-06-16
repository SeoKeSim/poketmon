package sks.poketmon.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sks.poketmon.dto.PokemonDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.service.PokemonService;

@Controller
public class PokemonController {

    private static final Logger logger = LoggerFactory.getLogger(PokemonController.class);

    @Autowired
    private PokemonService pokemonService;

    // 메인 페이지 (검색 폼)
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        // 세션에서 로그인 정보 가져와서 모델에 추가
        LoginResponseDto loginUser = (LoginResponseDto) session.getAttribute("loginUser");
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("userName", loginUser.getUserName());
        }
        return "index";
    }

    // 포켓몬 검색 처리
    @PostMapping("/search")
    public String searchPokemon(@RequestParam("query") String query, Model model, HttpSession session) {

        logger.info("포켓몬 검색 요청: {}", query);

        LoginResponseDto loginUser = (LoginResponseDto) session.getAttribute("loginUser");
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("userName", loginUser.getUserName());
        }

        if (query == null || query.trim().isEmpty()) {
            logger.warn("빈 검색어로 요청됨");
            model.addAttribute("error", "검색어를 입력해주세요.");
            return "index";
        }

        try {
            PokemonDto pokemon = pokemonService.searchPokemon(query);

            if (pokemon == null) {
                logger.info("포켓몬을 찾을 수 없음: {}", query);
                model.addAttribute("error", "'" + query + "'에 해당하는 포켓몬을 찾을 수 없습니다.");
                return "index";
            }

            logger.info("포켓몬 검색 성공: {} (ID: {})", pokemon.getName(), pokemon.getId());
            model.addAttribute("pokemon", pokemon);
            model.addAttribute("pokemonService", pokemonService); // 한국어 변환을 위해 추가
            return "result";

        } catch (RuntimeException e) {
            logger.error("포켓몬 검색 중 런타임 오류 발생", e);
            model.addAttribute("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
            return "index";
        } catch (Exception e) {
            logger.error("포켓몬 검색 중 예상치 못한 오류 발생", e);
            model.addAttribute("error", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "index";
        }
    }

    // GET 방식으로 검색 페이지 접근 시 메인으로 리다이렉트
    @GetMapping("/search")
    public String searchGet() {
        return "redirect:/";
    }
}