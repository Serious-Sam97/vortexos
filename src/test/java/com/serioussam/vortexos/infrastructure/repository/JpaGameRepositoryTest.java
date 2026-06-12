package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.game.Game;
import com.serioussam.vortexos.domain.platform.Platform;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistence-slice tests for the custom @Query methods on JpaGameRepository,
 * exercising the real JPQL against the configured SQLite database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaGameRepositoryTest {

    @Autowired private JpaGameRepository gameRepository;
    @Autowired private TestEntityManager em;

    private Game game(Platform platform, String title, boolean backlog) {
        Game game = new Game();
        game.setTitle(title);
        game.setPlatform(platform);
        game.setBacklog(backlog);
        game.setStartedDate(LocalDate.now());
        return game;
    }

    @Test
    void gamesList_andBacklogGamesList_partitionByTheBacklogFlag() {
        Platform platform = new Platform();
        platform.setName("Test Platform");
        em.persist(platform);

        em.persist(game(platform, "Playing Now", false));
        em.persist(game(platform, "On The Shelf", true));
        em.flush();

        assertThat(gameRepository.gamesList())
                .extracting(Game::getTitle)
                .containsExactly("Playing Now");

        assertThat(gameRepository.backlogGamesList())
                .extracting(Game::getTitle)
                .containsExactly("On The Shelf");
    }

    @Test
    void lists_areEmptyWhenNoGamesExist() {
        assertThat(gameRepository.gamesList()).isEmpty();
        assertThat(gameRepository.backlogGamesList()).isEmpty();
    }
}
