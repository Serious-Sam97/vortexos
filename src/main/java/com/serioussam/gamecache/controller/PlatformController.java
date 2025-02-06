package com.serioussam.gamecache.controller;

import com.serioussam.gamecache.model.Platform;
import com.serioussam.gamecache.repository.PlatformRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/platforms")
public class PlatformController {
    private final PlatformRepository platformRepository;

    public PlatformController (PlatformRepository platformRepository)
    {
        this.platformRepository = platformRepository;
    }

    @GetMapping
    public List<Platform> getAllPlatforms()
    {
        return this.platformRepository.findAll();
    }
}
