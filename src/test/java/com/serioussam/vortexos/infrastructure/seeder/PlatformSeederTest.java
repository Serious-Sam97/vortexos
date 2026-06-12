package com.serioussam.vortexos.infrastructure.seeder;

import com.serioussam.vortexos.infrastructure.repository.JpaPlatformRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/** The seeder should populate the 24 platforms exactly once and be idempotent. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlatformSeederTest {

    @Autowired private JpaPlatformRepository platformRepository;

    @Test
    void run_seedsTheCatalogueOnceAndIsIdempotent() throws Exception {
        PlatformSeeder seeder = new PlatformSeeder(platformRepository);

        seeder.run();
        assertThat(platformRepository.count()).isEqualTo(24);

        seeder.run(); // second run must detect existing data and skip
        assertThat(platformRepository.count()).isEqualTo(24);
        assertThat(platformRepository.findAll())
                .extracting(p -> p.getName())
                .contains("PC", "Nintendo Switch", "Quest");
    }
}
