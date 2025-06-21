package sks.poketmon.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import sks.poketmon.config.JwtTokenProvider;
import sks.poketmon.dto.ApiResponse;
import sks.poketmon.dto.FavoriteResponseDto;
import sks.poketmon.dto.FavoriteRequestDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.service.FavoriteService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class JwtFavoriteController {

    private final FavoriteService favoriteService;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 페이지 렌더링: 즐겨찾기 화면
    @GetMapping("/jwt/favorites")
    public String jwtFavoritesPage(HttpServletRequest request, Model model) {
        log.debug("즐겨찾기 페이지 접근 시도");

        Long userCode = getUserCodeFromRequest(request);
        log.debug("추출된 userCode: {}", userCode);

        if (userCode == null) {
            log.warn("userCode를 찾을 수 없어 로그인 페이지로 리다이렉트");
            return "redirect:/users/login";
        }

        try {
            // 즐겨찾기 목록 조회
            List<FavoriteResponseDto> favorites = favoriteService.getFavoriteList(userCode);
            model.addAttribute("favorites", favorites);
            model.addAttribute("favoriteCount", favorites.size());
            model.addAttribute("hasFavorites", !favorites.isEmpty());

            log.debug("즐겨찾기 {}개 조회 완료", favorites.size());
            return "favorites";
        } catch (Exception e) {
            log.error("즐겨찾기 목록 조회 중 오류 발생", e);
            return "redirect:/users/login";
        }
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

    // ✅ 요청에서 userCode를 추출하는 통합 메서드
    private Long getUserCodeFromRequest(HttpServletRequest request) {
        // 1. JwtWebInterceptor에서 설정한 userCode를 먼저 확인
        Long userCode = (Long) request.getAttribute("userCode");
        log.debug("Attribute에서 userCode: {}", userCode);

        if (userCode != null) {
            return userCode;
        }

        // 2. jwtUser에서 가져오기
        LoginResponseDto jwtUser = (LoginResponseDto) request.getAttribute("jwtUser");
        if (jwtUser != null) {
            log.debug("jwtUser에서 userCode: {}", jwtUser.getUserCode());
            return jwtUser.getUserCode();
        }

        // 3. 세션의 tempJwtUser에서 확인 (JwtWebInterceptor에서 설정)
        LoginResponseDto tempJwtUser = (LoginResponseDto) request.getSession().getAttribute("tempJwtUser");
        if (tempJwtUser != null) {
            log.debug("tempJwtUser에서 userCode: {}", tempJwtUser.getUserCode());
            return tempJwtUser.getUserCode();
        }

        // 4. 헤더에서 토큰 직접 확인 (fallback)
        String token = getTokenFromRequest(request);
        log.debug("헤더에서 추출한 토큰: {}", token != null ? "존재" : "없음");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            userCode = jwtTokenProvider.getUserCode(token);
            log.debug("토큰에서 추출한 userCode: {}", userCode);
            return userCode;
        }

        // 5. 쿠키에서도 확인해보기 (추가)
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    String cookieToken = cookie.getValue();
                    if (cookieToken != null && jwtTokenProvider.validateToken(cookieToken)) {
                        userCode = jwtTokenProvider.getUserCode(cookieToken);
                        log.debug("쿠키에서 추출한 userCode: {}", userCode);
                        return userCode;
                    }
                }
            }
        }

        log.warn("모든 방법으로 userCode 추출 실패");
        return null;
    }

    // ✅ 헤더에서 토큰 꺼내는 유틸
    private String getTokenFromRequest(HttpServletRequest request) {
        // Authorization 헤더 확인
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // X-Auth-Token 헤더도 확인
        String authToken = request.getHeader("X-Auth-Token");
        if (authToken != null) {
            return authToken;
        }

        return null;
    }
}