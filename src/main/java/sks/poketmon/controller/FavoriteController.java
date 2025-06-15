package sks.poketmon.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sks.poketmon.dto.FavoriteResponseDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.service.FavoriteService;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public String favoritesPage(Model model, HttpSession session) {
        // 로그인 사용자 확인 - LoginResponseDto로 변경
        LoginResponseDto loginUser = (LoginResponseDto) session.getAttribute("loginUser");
        if (loginUser == null || !loginUser.isSuccess()) {
            return "redirect:/users/login";
        }

        try {
            // 즐겨찾기 목록 조회 - userCode 사용
            List<FavoriteResponseDto> favorites = favoriteService.getFavoriteList(loginUser.getUserCode());

            // 날짜 포맷 헬퍼 전달
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일");
            model.addAttribute("formatDate", formatter);

            // 즐겨찾기 개수
            long favoriteCount = (favorites != null) ? favorites.size() : 0;

            // 모델에 추가
            model.addAttribute("favorites", favorites != null ? favorites : List.of());
            model.addAttribute("favoriteCount", favoriteCount);
            model.addAttribute("hasFavorites", favorites != null && !favorites.isEmpty());
            model.addAttribute("loginUser", loginUser); // LoginResponseDto 객체 전달

        } catch (Exception e) {
            // 예외 시에도 빈 값들로 초기화해서 템플릿이 안 깨지도록
            model.addAttribute("favorites", List.of());
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("hasFavorites", false);
            model.addAttribute("error", "즐겨찾기 목록을 불러오는 중 오류가 발생했습니다.");
        }

        return "favorites";
    }
}