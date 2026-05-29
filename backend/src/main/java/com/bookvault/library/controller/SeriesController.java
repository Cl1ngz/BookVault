package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Series;
import com.bookvault.library.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public ResponseEntity<List<Series>> getAllSeries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String authorLastName
    ) {
        return ResponseEntity.ok(seriesService.getAllSeries(name, authorLastName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Series> getSeriesById(@PathVariable Integer id) {
        return seriesService.getSeriesById(id);
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksBySeries(@PathVariable Integer id) {
        return seriesService.getBooksBySeries(id);
    }
}