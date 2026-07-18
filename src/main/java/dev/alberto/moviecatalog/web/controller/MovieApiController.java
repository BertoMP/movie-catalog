package dev.alberto.moviecatalog.web.controller;

import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.web.mapper.MovieApiMapper;
import dev.alberto.moviecatalog.web.response.MovieDetailResponse;
import dev.alberto.moviecatalog.web.response.MovieSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/movies")
public class MovieApiController {

    private final MovieCatalogService movieCatalogService;
    private final MovieApiMapper movieApiMapper;

    @GetMapping("/trending")
    public List<MovieSummaryResponse> getTrendingMovies(@RequestParam(defaultValue = "DAY") TrendingWindow window) {
        long startTime = System.currentTimeMillis();
        log.info("GET /api/v1/movies/trending?window={}", window);

        List<MovieSummary> movies =
                movieCatalogService.getTrendingMovies(window);

        long endTime = System.currentTimeMillis();
        log.info("Finished fetching trending movies in {} ms", endTime - startTime);

        return movieApiMapper.toResponse(movies);
    }

    @GetMapping("/movie/{id}")
    public MovieDetailResponse getMovieDetail(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        log.info("GET /api/v1/movies/movie/{}", id);

        MovieDetail movieDetail =
                movieCatalogService.getMovieDetail(id);

        long endTime = System.currentTimeMillis();
        log.info("Finished fetching movie detail in {} ms", endTime - startTime);

        return movieApiMapper.toResponse(movieDetail);
    }
}
