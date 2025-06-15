package sks.poketmon.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sks.poketmon.dto.FavoriteResponseDto;
import sks.poketmon.entity.User;
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
        // 로그인 사용자 확인
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인 페이지로 리다이렉트
            return "redirect:/users/login";
        }

        try {
            // 즐겨찾기 목록 조회
            List<FavoriteResponseDto> favorites = favoriteService.getFavoriteList(loginUser.getUserCode());

            // 날짜 포맷 처리
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일");
            favorites.forEach(favorite -> {
                if (favorite.getCreatedAt() != null) {
                    // 날짜 포맷팅을 위한 헬퍼 메서드 (Mustache에서 사용)
                    model.addAttribute("formatDate", formatter);
                }
            });

            // 즐겨찾기 개수 조회
            long favoriteCount = favoriteService.getFavoriteCount(loginUser.getUserCode());

            // 모델에 데이터 추가
            model.addAttribute("favorites", favorites);
            model.addAttribute("favoriteCount", favoriteCount);
            model.addAttribute("hasFavorites", !favorites.isEmpty());
            model.addAttribute("loginUser", loginUser);

        } catch (Exception e) {
            // 오류 발생 시 빈 목록으로 처리
            model.addAttribute("favorites", List.of());
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("hasFavorites", false);
            model.addAttribute("error", "즐겨찾기 목록을 불러오는 중 오류가 발생했습니다.");
        }

        return "favorites";
    }
}