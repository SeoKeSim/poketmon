package sks.poketmon.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sks.poketmon.service.FavoriteService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt/favorites")
public class JwtFavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 즐겨찾기 추가
     * POST /api/jwt/favorites
     */
    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody AddFavoriteRequest request,
                                         HttpServletRequest httpRequest) {
        try {
            Long userCode = (Long) httpRequest.getAttribute("userCode");

            boolean success = favoriteService.addFavoriteJwt(userCode, request.getPokemonId());

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "즐겨찾기에 추가되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "이미 즐겨찾기에 등록된 포켓몬입니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기 추가 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 즐겨찾기 삭제
     * DELETE /api/jwt/favorites/{pokemonId}
     */
    @DeleteMapping("/{pokemonId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Integer pokemonId,
                                            HttpServletRequest httpRequest) {
        try {
            Long userCode = (Long) httpRequest.getAttribute("userCode");

            boolean success = favoriteService.removeFavoriteJwt(userCode, pokemonId);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "즐겨찾기에서 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "즐겨찾기에서 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 즐겨찾기 목록 조회
     * GET /api/jwt/favorites
     */
    @GetMapping
    public ResponseEntity<?> getFavorites(HttpServletRequest httpRequest) {
        try {
            Long userCode = (Long) httpRequest.getAttribute("userCode");

            List<Integer> favoriteIds = favoriteService.getFavoriteIds(userCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", favoriteIds);
            response.put("count", favoriteIds.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 즐겨찾기 상태 확인
     * GET /api/jwt/favorites/check/{pokemonId}
     */
    @GetMapping("/check/{pokemonId}")
    public ResponseEntity<?> checkFavorite(@PathVariable Integer pokemonId,
                                           HttpServletRequest httpRequest) {
        try {
            Long userCode = (Long) httpRequest.getAttribute("userCode");

            boolean isFavorite = favoriteService.isFavorite(userCode, pokemonId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isFavorite", isFavorite);
            response.put("pokemonId", pokemonId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기 상태 확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 요청 DTO
    public static class AddFavoriteRequest {
        private Integer pokemonId;

        public Integer getPokemonId() {
            return pokemonId;
        }

        public void setPokemonId(Integer pokemonId) {
            this.pokemonId = pokemonId;
        }
    }
}