package sks.poketmon.dto.response;

import sks.poketmon.entity.PokemonNameMapping;
import java.util.List;

public class MappingStatsResponse {
    private long totalMappings;
    private long todayMappings;
    private List<PokemonNameMapping> recentMappings;

    public MappingStatsResponse() {}

    public MappingStatsResponse(long totalMappings, long todayMappings, List<PokemonNameMapping> recentMappings) {
        this.totalMappings = totalMappings;
        this.todayMappings = todayMappings;
        this.recentMappings = recentMappings;
    }

    // Getters and Setters
    public long getTotalMappings() { return totalMappings; }
    public void setTotalMappings(long totalMappings) { this.totalMappings = totalMappings; }

    public long getTodayMappings() { return todayMappings; }
    public void setTodayMappings(long todayMappings) { this.todayMappings = todayMappings; }

    public List<PokemonNameMapping> getRecentMappings() { return recentMappings; }
    public void setRecentMappings(List<PokemonNameMapping> recentMappings) { this.recentMappings = recentMappings; }
}