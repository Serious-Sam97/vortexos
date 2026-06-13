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

    private static final Long OWNER = 1L;
    private static final Long OTHER = 2L;

    private Game game(Platform platform, String title, boolean backlog, Long ownerId) {
        Game game = new Game();
        game.setTitle(title);
        game.setPlatform(platform);
        game.setBacklog(backlog);
        game.setStartedDate(LocalDate.now());
        game.setOwnerId(ownerId);
        return game;
    }

    @Test
    void gamesList_andBacklogGamesList_partitionByTheBacklogFlag() {
        Platform platform = new Platform();
        platform.setName("Test Platform");
        em.persist(platform);

        em.persist(game(platform, "Playing Now", false, OWNER));
        em.persist(game(platform, "On The Shelf", true, OWNER));
        em.flush();

        assertThat(gameRepository.gamesList(OWNER))
                .extracting(Game::getTitle)
                .containsExactly("Playing Now");

        assertThat(gameRepository.backlogGamesList(OWNER))
                .extracting(Game::getTitle)
                .containsExactly("On The Shelf");
    }

    @Test
    void lists_areScopedToTheOwner() {
        Platform platform = new Platform();
        platform.setName("Test Platform");
        em.persist(platform);

        em.persist(game(platform, "Mine", false, OWNER));
        em.persist(game(platform, "Theirs", false, OTHER));
        em.flush();

        assertThat(gameRepository.gamesList(OWNER))
                .extracting(Game::getTitle)
                .containsExactly("Mine"); // the other user's game is excluded
    }

    @Test
    void lists_areEmptyWhenNoGamesExist() {
        assertThat(gameRepository.gamesList(OWNER)).isEmpty();
        assertThat(gameRepository.backlogGamesList(OWNER)).isEmpty();
    }
}
