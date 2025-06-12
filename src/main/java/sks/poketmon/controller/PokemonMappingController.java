package sks.poketmon.controller;

import sks.poketmon.entity.PokemonNameMapping;
import sks.poketmon.service.DatabasePokemonMappingService;
import sks.poketmon.dto.response.MappingStatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
@Validated
public class PokemonMappingController {

    @Autowired
    private DatabasePokemonMappingService mappingService;

    /**
     * 한국어 -> 영어 이름 변환
     */
    @GetMapping("/korean-to-english")
    public ResponseEntity<String> getEnglishName(@RequestParam @NotBlank String koreanName) {
        String englishName = mappingService.getEnglishName(koreanName);
        if (englishName != null) {
            return ResponseEntity.ok(englishName);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 영어 -> 한국어 이름 변환
     */
    @GetMapping("/english-to-korean")
    public ResponseEntity<String> getKoreanName(@RequestParam @NotBlank String englishName) {
        String koreanName = mappingService.getKoreanName(englishName);
        if (koreanName != null) {
            return ResponseEntity.ok(koreanName);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 한국어 이름으로 부분 검색
     */
    @GetMapping("/search/korean")
    public ResponseEntity<List<PokemonNameMapping>> searchByKoreanName(@RequestParam @NotBlank String name) {
        List<PokemonNameMapping> results = mappingService.searchByKoreanName(name);
        return ResponseEntity.ok(results);
    }

    /**
     * 영어 이름으로 부분 검색
     */
    @GetMapping("/search/english")
    public ResponseEntity<List<PokemonNameMapping>> searchByEnglishName(@RequestParam @NotBlank String name) {
        List<PokemonNameMapping> results = mappingService.searchByEnglishName(name);
        return ResponseEntity.ok(results);
    }

    /**
     * 매핑 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<MappingStatsResponse> getMappingStats() {
        MappingStatsResponse stats = mappingService.getMappingStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 수동 매핑 추가
     */
    @PostMapping("/mapping")
    public ResponseEntity<PokemonNameMapping> addMapping(
            @RequestParam @NotBlank String koreanName,
            @RequestParam @NotBlank String englishName) {
        try {
            PokemonNameMapping mapping = mappingService.addManualMapping(koreanName, englishName);
            return ResponseEntity.ok(mapping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 대량 영어 이름 조회
     */
    @PostMapping("/mappings/english")
    public ResponseEntity<List<PokemonNameMapping>> getMappingsByEnglishNames(@RequestBody List<String> englishNames) {
        List<PokemonNameMapping> mappings = mappingService.findMappingsByEnglishNames(englishNames);
        return ResponseEntity.ok(mappings);
    }

    /**
     * 대량 한국어 이름 조회
     */
    @PostMapping("/mappings/korean")
    public ResponseEntity<List<PokemonNameMapping>> getMappingsByKoreanNames(@RequestBody List<String> koreanNames) {
        List<PokemonNameMapping> mappings = mappingService.findMappingsByKoreanNames(koreanNames);
        return ResponseEntity.ok(mappings);
    }

    /**
     * 수동 데이터 업데이트 트리거 (관리자용)
     */
    @PostMapping("/admin/update-mappings")
    public ResponseEntity<String> triggerMappingUpdate() {
        mappingService.updatePokemonMappings();
        return ResponseEntity.ok("매핑 업데이트가 시작되었습니다.");
    }

    /**
     * 중복 데이터 정리 (관리자용)
     */
    @PostMapping("/admin/cleanup-duplicates")
    public ResponseEntity<String> cleanupDuplicates() {
        mappingService.cleanupDuplicateData();
        return ResponseEntity.ok("중복 데이터 정리가 완료되었습니다.");
    }
}