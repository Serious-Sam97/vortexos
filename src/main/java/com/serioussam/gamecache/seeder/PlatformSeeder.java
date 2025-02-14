package com.serioussam.gamecache.seeder;

import com.serioussam.gamecache.model.Platform;
import com.serioussam.gamecache.repository.PlatformRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PlatformSeeder implements CommandLineRunner {
    private final PlatformRepository platformRepository;

    public PlatformSeeder(PlatformRepository platformRepository)
    {
        this.platformRepository = platformRepository;
    }

    @Override
    public void run(String... args) throws Exception
    {
        if (platformRepository.count() != 0) {
            System.out.println("Platforms already added, skipping this step!");
            return;
        }

        String[] platforms = {
                "PC", "NES", "Super Nintendo", "Nintendo 64", "GameCube", "Nintendo Wii", "Nintendo 3DS", "Wii U",
                "Nintendo Switch", "PlayStation", "PlayStation 2", "PlayStation 3", "PlayStation 4", "PlayStation 5", "PlayStation Portable",
                "PlayStation Vita", "Xbox", "Xbox 360", "Xbox One", "Xbox Series X", "Xbox Series S", "Sega Genesis", "Sega Dreamcast", "Quest"
        };

        for (String platform : platforms) {
            Platform plat = new Platform();
            plat.setName(platform);
            platformRepository.save(plat);
        }
    }
}
