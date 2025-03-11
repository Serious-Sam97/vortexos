package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.platform.Platform;
import com.serioussam.vortexos.infrastructure.repository.JpaPlatformRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/platforms")
public class PlatformController {
    private final JpaPlatformRepository platformRepository;

    public PlatformController (JpaPlatformRepository platformRepository)
    {
        this.platformRepository = platformRepository;
    }

    @GetMapping
    public List<Platform> getAllPlatforms()
    {
        return this.platformRepository.findAll();
    }
}
