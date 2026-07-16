package dev.alberto.moviecatalog.web.controller;

import dev.alberto.moviecatalog.domain.model.MoviePage;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.web.mapper.MovieApiMapper;
import dev.alberto.moviecatalog.web.response.MoviePageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/movies")
public class MovieApiController {

    private final MovieCatalogService movieCatalogService;
    private final MovieApiMapper movieApiMapper;

    @GetMapping("/trending")
    public MoviePageResponse getTrendingMovies(@RequestParam(defaultValue = "DAY") TrendingWindow window) {
        Long startTime = System.currentTimeMillis();
        log.info("GET /api/v1/movies/trending?window={}", window);

        MoviePage movies =
                movieCatalogService.getTrendingMovies(window);

        Long endTime = System.currentTimeMillis();
        log.info("Finished fetching trending movies in {} ms", endTime - startTime);

        return movieApiMapper.toResponse(movies);
    }
}