package sks.poketmon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService implements ApplicationRunner {

    @Autowired
    private PokemonService pokemonService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 애플리케이션 시작 시 포켓몬 이름 데이터 로딩
        // 1세대~9세대까지 약 1010마리 (필요에 따라 조정)

        // 처음에는 적은 수로 테스트 해보세요
        int maxPokemon = 151; // 1세대만 먼저 로딩

        // 백그라운드에서 실행
        new Thread(() -> {
            try {
                pokemonService.loadInitialPokemonNames(maxPokemon);
            } catch (Exception e) {
                System.err.println("초기 데이터 로딩 중 오류 발생: " + e.getMessage());
            }
        }).start();
    }
}