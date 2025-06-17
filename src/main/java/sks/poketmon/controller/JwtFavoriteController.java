package sks.poketmon.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sks.poketmon.config.JwtTokenProvider;
import sks.poketmon.dto.ApiResponse;
import sks.poketmon.dto.FavoriteResponseDto;
import sks.poketmon.dto.FavoriteRequestDto;
import sks.poketmon.service.FavoriteService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class JwtFavoriteController {

    private final FavoriteService favoriteService;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 페이지 렌더링: 즐겨찾기 화면
    @GetMapping("/jwt/favorites")
    public String jwtFavoritesPage(HttpServletRequest request, Model model) {
        String token = getTokenFromRequest(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return "redirect:/users/login";
        }

        Long userCode = jwtTokenProvider.getUserCode(token);
        List<FavoriteResponseDto> favorites = favoriteService.getFavoriteList(userCode);
        model.addAttribute("favorites", favorites);
        model.addAttribute("favoriteCount", favorites.size());

        return "favorites";
    }


    // ✅ 즐겨찾기 추가 (API)
    @PostMapping("/api/jwt/favorites")
    @ResponseBody
    public ApiResponse<?> addFavorite(@RequestAttribute("userCode") Long userCode,
                                      @RequestBody FavoriteRequestDto request) {
        boolean success = favoriteService.addFavoriteJwt(userCode, request.getPokemonId());
        return success
                ? ApiResponse.success("즐겨찾기에 추가되었습니다.")
                : ApiResponse.error("이미 즐겨찾기된 포켓몬입니다.");
    }

    // ✅ 즐겨찾기 삭제 (API)
    @DeleteMapping("/api/jwt/favorites/{pokemonId}")
    @ResponseBody
    public ApiResponse<?> removeFavorite(@RequestAttribute("userCode") Long userCode,
                                         @PathVariable Integer pokemonId) {
        boolean success = favoriteService.removeFavoriteJwt(userCode, pokemonId);
        return success
                ? ApiResponse.success("즐겨찾기에서 삭제되었습니다.")
                : ApiResponse.error("즐겨찾기 목록에 없습니다.");
    }

    // ✅ 즐겨찾기 목록 조회 (API)
    @GetMapping("/api/jwt/favorites")
    @ResponseBody
    public ApiResponse<List<Integer>> getFavorites(@RequestAttribute("userCode") Long userCode) {
        List<Integer> favoriteIds = favoriteService.getFavoriteIds(userCode);
        return ApiResponse.success(favoriteIds);
    }

    // ✅ 즐겨찾기 여부 확인 (API)
    @GetMapping("/api/jwt/favorites/check/{pokemonId}")
    @ResponseBody
    public ApiResponse<Boolean> checkFavorite(@RequestAttribute("userCode") Long userCode,
                                              @PathVariable Integer pokemonId) {
        boolean exists = favoriteService.isFavorite(userCode, pokemonId);
        return ApiResponse.success(exists);
    }

    // ✅ 즐겨찾기 개수 확인 (API)
    @GetMapping("/api/jwt/favorites/count")
    @ResponseBody
    public ApiResponse<Integer> getFavoriteCount(@RequestAttribute("userCode") Long userCode) {
        List<Integer> favoriteIds = favoriteService.getFavoriteIds(userCode);
        return ApiResponse.success(favoriteIds.size());
    }

    // ✅ 헤더에서 토큰 꺼내는 유틸
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
