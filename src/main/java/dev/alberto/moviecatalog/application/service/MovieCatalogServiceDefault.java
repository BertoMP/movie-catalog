package dev.alberto.moviecatalog.application.service;

import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.infrastructure.tmdb.client.TmdbFeignClient;
import dev.alberto.moviecatalog.infrastructure.tmdb.config.TmdbProperties;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.mapper.TmdbMovieMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieCatalogServiceDefault implements MovieCatalogService {

    private final TmdbFeignClient tmdbClient;
    private final TmdbProperties tmdbProperties;
    private final TmdbMovieMapper tmdbMovieMapper;

    @Override
    public List<MovieSummary> getTrendingMovies(TrendingWindow window) {
        TmdbMoviePageResponse response =
                tmdbClient.getTrendingMovies(
                        window.getValue(),
                        tmdbProperties.defaultLanguage()
                );

        return tmdbMovieMapper.toDomainList(response);
    }
}
