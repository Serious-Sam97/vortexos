package com.serioussam.vortexos.domain.game;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    public List<Game> backlogGamesList();
    public List<Game> gamesList();
}
