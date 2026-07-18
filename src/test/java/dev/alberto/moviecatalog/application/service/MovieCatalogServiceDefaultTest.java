package dev.alberto.moviecatalog.application.service;

import dev.alberto.moviecatalog.application.exception
        .MovieProviderAccessException;
import dev.alberto.moviecatalog.application.exception
        .MovieProviderRateLimitedException;
import dev.alberto.moviecatalog.application.exception
        .MovieProviderRequestException;
import dev.alberto.moviecatalog.application.exception
        .MovieProviderUnavailableException;
import dev.alberto.moviecatalog.domain.exception.MovieNotFoundException;
import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.infrastructure.tmdb.client
        .TmdbFeignClient;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMovieDetailResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto
        .TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbAuthenticationException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbBadRequestException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbForbiddenException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbRateLimitException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbResourceNotFoundException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbServerException;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception
        .TmdbUnexpectedException;
import dev.alberto.moviecatalog.infrastructure.tmdb.mapper
        .TmdbMovieMapper;
import dev.alberto.moviecatalog.testdata.domain
        .MovieDetailMother;
import dev.alberto.moviecatalog.testdata.domain
        .MovieSummaryMother;
import dev.alberto.moviecatalog.testdata.tmdb
        .TmdbMovieDetailResponseMother;
import dev.alberto.moviecatalog.testdata.tmdb
        .TmdbMoviePageResponseMother;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({
        MockitoExtension.class,
        InstancioExtension.class
})
class MovieCatalogServiceDefaultTest {

    private static final CatalogLanguage LANGUAGE =
            CatalogLanguage.EN_US;

    private static final String METHOD_KEY =
            "TmdbFeignClient#test";

    @Mock
    private TmdbFeignClient tmdbClient;

    @Mock
    private TmdbMovieMapper tmdbMovieMapper;

    @InjectMocks
    private MovieCatalogServiceDefault service;

    @ParameterizedTest
    @EnumSource(TrendingWindow.class)
    void shouldReturnTrendingMovies(
            TrendingWindow window
    ) {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        List<MovieSummary> expectedMovies =
                MovieSummaryMother.randomList(2);

        when(tmdbClient.getTrendingMovies(
                window.getValue(),
                LANGUAGE.getValue()
        )).thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(expectedMovies);

        List<MovieSummary> result =
                service.getTrendingMovies(window, LANGUAGE);

        assertThat(result)
                .isSameAs(expectedMovies);

        verify(tmdbClient).getTrendingMovies(
                window.getValue(),
                LANGUAGE.getValue()
        );

        verify(tmdbMovieMapper)
                .toDomainList(providerResponse);
    }

    @Test
    void shouldReturnMovieDetail() {
        Long movieId = 550L;

        TmdbMovieDetailResponse providerResponse =
                TmdbMovieDetailResponseMother.withId(movieId);

        MovieDetail expectedMovie =
                MovieDetailMother.withId(movieId);

        when(tmdbClient.getMovieById(
                movieId,
                LANGUAGE.getValue()
        )).thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomain(providerResponse))
                .thenReturn(expectedMovie);

        MovieDetail result =
                service.getMovieDetail(movieId, LANGUAGE);

        assertThat(result)
                .isSameAs(expectedMovie);

        verify(tmdbClient).getMovieById(
                movieId,
                LANGUAGE.getValue()
        );

        verify(tmdbMovieMapper)
                .toDomain(providerResponse);
    }

    @Test
    void shouldThrowMovieNotFoundWhenDetailDoesNotExist() {
        Long movieId = 999_999_999L;

        TmdbResourceNotFoundException cause =
                new TmdbResourceNotFoundException(
                        "Movie not found",
                        404,
                        34,
                        METHOD_KEY
                );

        when(tmdbClient.getMovieById(
                movieId,
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getMovieDetail(movieId, LANGUAGE)
        )
                .isInstanceOf(MovieNotFoundException.class)
                .hasCause(cause);
    }

    @Test
    void shouldTranslateBadRequestError() {
        TmdbBadRequestException cause =
                new TmdbBadRequestException(
                        "Invalid request",
                        400,
                        5,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderRequestException.class
                )
                .hasCause(cause);
    }

    @Test
    void shouldTranslateAuthenticationError() {
        TmdbAuthenticationException cause =
                new TmdbAuthenticationException(
                        "Invalid token",
                        401,
                        7,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderAccessException.class
                )
                .hasCause(cause);
    }

    @Test
    void shouldTranslateForbiddenError() {
        TmdbForbiddenException cause =
                new TmdbForbiddenException(
                        "Forbidden",
                        403,
                        10,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderAccessException.class
                )
                .hasCause(cause);
    }

    @Test
    void shouldTranslateRateLimitError() {
        TmdbRateLimitException cause =
                new TmdbRateLimitException(
                        "Rate limit exceeded",
                        429,
                        25,
                        METHOD_KEY,
                        60L
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderRateLimitedException.class
                )
                .hasCause(cause)
                .satisfies(exception -> {
                    MovieProviderRateLimitedException rateLimit =
                            (MovieProviderRateLimitedException) exception;

                    assertThat(rateLimit.getRetryAfterSeconds())
                            .isEqualTo(60L);
                });
    }

    @Test
    void shouldTranslateServerError() {
        TmdbServerException cause =
                new TmdbServerException(
                        "Provider error",
                        503,
                        null,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderUnavailableException.class
                )
                .hasCause(cause);
    }

    @Test
    void shouldTranslateUnexpectedError() {
        TmdbUnexpectedException cause =
                new TmdbUnexpectedException(
                        "Unexpected provider response",
                        418,
                        null,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderUnavailableException.class
                )
                .hasCause(cause);
    }

    @Test
    void shouldTranslateNotFoundFromTrendingAsProviderUnavailable() {
        TmdbResourceNotFoundException cause =
                new TmdbResourceNotFoundException(
                        "Endpoint not found",
                        404,
                        34,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies(
                TrendingWindow.DAY.getValue(),
                LANGUAGE.getValue()
        )).thenThrow(cause);

        assertThatThrownBy(
                () -> service.getTrendingMovies(
                        TrendingWindow.DAY,
                        LANGUAGE
                )
        )
                .isExactlyInstanceOf(
                        MovieProviderUnavailableException.class
                )
                .hasCause(cause);
    }
}
