package sks.poketmon.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sks.poketmon.dto.ApiResponse;
import sks.poketmon.dto.FavoriteRequestDto;
import sks.poketmon.dto.FavoriteResponseDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.exception.FavoriteException;
import sks.poketmon.service.FavoriteService;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteRestController {

    @Autowired
    private FavoriteService favoriteService;

    // 현재 로그인된 사용자 정보 가져오기 (세션 우선, JWT 보조)
    private LoginResponseDto getCurrentUser(HttpSession session, HttpServletRequest request) {
        // 1. 세션 로그인 확인
        LoginResponseDto loginUser = (LoginResponseDto) session.getAttribute("loginUser");
        if (loginUser != null) {
            return loginUser;
        }

        // 2. JWT 로그인 확인 - 여러 가능한 attribute 이름 시도
        LoginResponseDto jwtUser = (LoginResponseDto) request.getAttribute("jwtUser");
        if (jwtUser != null) {
            return jwtUser;
        }

        // 3. userCode로 저장된 경우 확인 (JwtFavoriteController 방식)
        Long userCode = (Long) request.getAttribute("userCode");
        if (userCode != null) {
            // userCode만 있는 경우 LoginResponseDto 생성
            LoginResponseDto user = new LoginResponseDto();
            user.setUserCode(userCode);
            return user;
        }

        return null;
    }

    // userCode 가져오기 헬퍼 메서드
    private Long getUserCode(HttpSession session, HttpServletRequest request) {
        LoginResponseDto user = getCurrentUser(session, request);
        return user != null ? user.getUserCode() : null;
    }

    // 즐겨찾기 추가
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponseDto>> addFavorite(
            @RequestBody FavoriteRequestDto request,
            HttpSession session,
            HttpServletRequest httpRequest) {

        try {
            // 로그인 사용자 확인 (세션 또는 JWT)
            Long userCode = getUserCode(session, httpRequest);
            if (userCode == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
            }

            // 입력값 검증
            if (request.getPokemonId() == null || request.getPokemonId() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("올바른 포켓몬 ID를 입력해주세요."));
            }

            // 즐겨찾기 추가
            FavoriteResponseDto favorite = favoriteService.addFavorite(userCode, request.getPokemonId());

            return ResponseEntity.ok(ApiResponse.success("즐겨찾기에 추가되었습니다.", favorite));

        } catch (FavoriteException.AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("즐겨찾기 추가 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("즐겨찾기 추가 중 오류가 발생했습니다."));
        }
    }

    // 즐겨찾기 삭제
    @DeleteMapping("/{pokemonId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Integer pokemonId,
            HttpSession session,
            HttpServletRequest httpRequest) {

        try {
            // 로그인 사용자 확인 (세션 또는 JWT)
            Long userCode = getUserCode(session, httpRequest);
            if (userCode == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
            }

            // 즐겨찾기 삭제
            favoriteService.removeFavorite(userCode, pokemonId);

            return ResponseEntity.ok(ApiResponse.success("즐겨찾기에서 삭제되었습니다.", null));

        } catch (FavoriteException.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("즐겨찾기 삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("즐겨찾기 삭제 중 오류가 발생했습니다."));
        }
    }

    // 내 즐겨찾기 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponseDto>>> getFavoriteList(
            HttpSession session,
            HttpServletRequest httpRequest) {

        try {
            // 로그인 사용자 확인 (세션 또는 JWT)
            Long userCode = getUserCode(session, httpRequest);
            if (userCode == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
            }

            // 즐겨찾기 목록 조회
            List<FavoriteResponseDto> favorites = favoriteService.getFavoriteList(userCode);

            return ResponseEntity.ok(ApiResponse.success(favorites));

        } catch (Exception e) {
            System.err.println("즐겨찾기 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("즐겨찾기 목록 조회 중 오류가 발생했습니다."));
        }
    }

    // 특정 포켓몬 즐겨찾기 여부 확인
    @GetMapping("/check/{pokemonId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @PathVariable Integer pokemonId,
            HttpSession session,
            HttpServletRequest httpRequest) {

        try {
            // 로그인 사용자 확인 (세션 또는 JWT)
            Long userCode = getUserCode(session, httpRequest);
            if (userCode == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
            }

            // 즐겨찾기 여부 확인
            boolean isFavorite = favoriteService.isFavorite(userCode, pokemonId);

            return ResponseEntity.ok(ApiResponse.success(isFavorite));

        } catch (Exception e) {
            System.err.println("즐겨찾기 확인 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("즐겨찾기 확인 중 오류가 발생했습니다."));
        }
    }

    // 즐겨찾기 개수 조회
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount(
            HttpSession session,
            HttpServletRequest httpRequest) {

        try {
            // 로그인 사용자 확인 (세션 또는 JWT)
            Long userCode = getUserCode(session, httpRequest);
            if (userCode == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요한 서비스입니다."));
            }

            // 즐겨찾기 개수 조회
            long count = favoriteService.getFavoriteCount(userCode);

            return ResponseEntity.ok(ApiResponse.success(count));

        } catch (Exception e) {
            System.err.println("즐겨찾기 개수 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("즐겨찾기 개수 조회 중 오류가 발생했습니다."));
        }
    }
}