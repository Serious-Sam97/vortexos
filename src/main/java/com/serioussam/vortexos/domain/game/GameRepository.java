package com.serioussam.vortexos.domain.game;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    public List<Game> backlogGamesList(Long ownerId);
    public List<Game> gamesList(Long ownerId);
}
