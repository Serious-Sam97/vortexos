package com.serioussam.gamecache.seeder;

import com.serioussam.gamecache.repository.GameRepository;
import com.serioussam.gamecache.repository.PlatformRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;

    public DataSeeder(PlatformRepository platformRepository, GameRepository gameRepository)
    {
        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public void run(String... args) throws Exception
    {
        
    }
}
