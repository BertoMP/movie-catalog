package dev.alberto.moviecatalog.web.controller;

import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.web.mapper.MovieApiMapper;
import dev.alberto.moviecatalog.web.response.MovieDetailResponse;
import dev.alberto.moviecatalog.web.response.MovieSummaryResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/movies")
public class MovieApiController {

    private final MovieCatalogService movieCatalogService;
    private final MovieApiMapper movieApiMapper;

    @GetMapping("/trending")
    public List<MovieSummaryResponse> getTrendingMovies(
            @RequestParam(defaultValue = "DAY") TrendingWindow window,
            Locale locale
    ) {
        long startTime = System.currentTimeMillis();
        log.info("GET /api/v1/movies/trending?window={}", window);

        CatalogLanguage language = CatalogLanguage.fromLocale(locale);
        List<MovieSummary> movies = movieCatalogService.getTrendingMovies(window, language);

        long endTime = System.currentTimeMillis();
        log.info("Finished fetching trending movies in {} ms", endTime - startTime);

        return movieApiMapper.toResponse(movies);
    }

    @GetMapping("/{movieId}")
    public MovieDetailResponse getMovieDetail(
            @PathVariable @Positive Long movieId,
            Locale locale
    ) {
        long startTime = System.currentTimeMillis();
        log.info("GET /api/v1/movies/movie/{}", movieId);

        CatalogLanguage language = CatalogLanguage.fromLocale(locale);
        MovieDetail movieDetail = movieCatalogService.getMovieDetail(movieId, language);

        long endTime = System.currentTimeMillis();
        log.info("Finished fetching movie detail in {} ms", endTime - startTime);

        return movieApiMapper.toResponse(movieDetail);
    }
}
