package com.serioussam.gamecache.application.controller;

import com.serioussam.gamecache.domain.platform.Platform;
import com.serioussam.gamecache.infrastructure.repository.JpaPlatformRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/platforms")
public class PlatformController {
    private final JpaPlatformRepository jpaPlatformRepository;

    public PlatformController (JpaPlatformRepository jpaPlatformRepository)
    {
        this.jpaPlatformRepository = jpaPlatformRepository;
    }

    @GetMapping
    public List<Platform> getAllPlatforms()
    {
        return this.jpaPlatformRepository.findAll();
    }
}
