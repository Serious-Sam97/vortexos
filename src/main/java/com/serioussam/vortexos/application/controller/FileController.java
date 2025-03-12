package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.domain.file.FileRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private JpaFileRepository fileRepository;

    public FileController(JpaFileRepository fileRepository)
    {
        this.fileRepository = fileRepository;
    }

    @GetMapping
    public List<File> index()
    {
        return this.fileRepository.findAll();
    }
}
