package com.bookvault.library.controller;

import com.bookvault.library.model.Series;
import com.bookvault.library.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesRepository seriesRepository;

    @GetMapping
    public List<Series> getAllSeries() {
        return seriesRepository.findAll();
    }
}