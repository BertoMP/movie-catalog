package dev.alberto.moviecatalog.application.service;

import dev.alberto.moviecatalog.application.exception.*;
import dev.alberto.moviecatalog.domain.exception.MovieNotFoundException;
import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.infrastructure.tmdb.client.TmdbFeignClient;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMovieDetailResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.*;
import dev.alberto.moviecatalog.infrastructure.tmdb.mapper.TmdbMovieMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieCatalogServiceDefault implements MovieCatalogService {

    private final TmdbFeignClient tmdbFeignClient;
    private final TmdbMovieMapper tmdbMovieMapper;

    @Override
    public List<MovieSummary> getTrendingMovies(TrendingWindow window, CatalogLanguage language) {
        try {
            TmdbMoviePageResponse response =
                    tmdbFeignClient.getTrendingMovies(
                            window.getValue(),
                            language.getValue()
                    );

            return tmdbMovieMapper.toDomainList(response);

        } catch (TmdbException exception) {
            throw translateProviderException(exception);
        }
    }

    @Override
    public MovieDetail getMovieDetail(Long movieId, CatalogLanguage language) {
        try {
            TmdbMovieDetailResponse response =
                    tmdbFeignClient.getMovieById(
                            movieId,
                            language.getValue()
                    );

            return tmdbMovieMapper.toDomain(response);
        } catch (TmdbResourceNotFoundException exception) {
            throw new MovieNotFoundException(movieId, exception);
        } catch (TmdbException exception) {
            throw translateProviderException(exception);
        }
    }

    private MovieProviderException translateProviderException(
            TmdbException exception
    ) {
        if (exception instanceof TmdbBadRequestException) {
            return new MovieProviderRequestException(exception);
        }

        if (exception instanceof TmdbAuthenticationException
                || exception instanceof TmdbForbiddenException) {
            return new MovieProviderAccessException(exception);
        }

        if (exception instanceof TmdbRateLimitException rateLimitException) {
            return new MovieProviderRateLimitedException(
                    rateLimitException.getRetryAfterSeconds(),
                    rateLimitException
            );
        }

        return new MovieProviderUnavailableException(exception);
    }
}
