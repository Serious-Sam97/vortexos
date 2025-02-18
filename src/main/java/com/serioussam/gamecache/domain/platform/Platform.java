package com.serioussam.gamecache.domain.platform;

import com.serioussam.gamecache.domain.game.Game;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "platform")
public class Platform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games = new ArrayList<>();

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
