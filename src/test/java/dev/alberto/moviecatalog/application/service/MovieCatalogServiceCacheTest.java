package dev.alberto.moviecatalog.application.service;

import dev.alberto.moviecatalog.application.cache.CatalogCacheNames;
import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.infrastructure.cache.config.CatalogCacheProperties;
import dev.alberto.moviecatalog.infrastructure.tmdb.client.TmdbFeignClient;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMovieDetailResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.exception.TmdbServerException;
import dev.alberto.moviecatalog.infrastructure.tmdb.mapper.TmdbMovieMapper;
import dev.alberto.moviecatalog.testdata.domain.MovieDetailMother;
import dev.alberto.moviecatalog.testdata.domain.MovieSummaryMother;
import dev.alberto.moviecatalog.testdata.tmdb.TmdbMovieDetailResponseMother;
import dev.alberto.moviecatalog.testdata.tmdb.TmdbMoviePageResponseMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test-cache")
class MovieCatalogServiceCacheTest {

    private static final String METHOD_KEY =
            "TmdbFeignClient#test";

    @Autowired
    private MovieCatalogService movieCatalogService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CatalogCacheProperties cacheProperties;

    @MockitoBean
    private TmdbFeignClient tmdbClient;

    @MockitoBean
    private TmdbMovieMapper tmdbMovieMapper;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);

            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void shouldUseTheIsolatedInMemoryCacheProfile() {
        assertThat(cacheManager)
                .isInstanceOf(ConcurrentMapCacheManager.class);

        assertThat(cacheProperties.trendingTtl())
                .isEqualTo(Duration.ofMinutes(1));

        assertThat(cacheProperties.movieDetailTtl())
                .isEqualTo(Duration.ofMinutes(2));
    }

    @Test
    void shouldCacheTrendingMoviesByWindowAndLanguage() {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        List<MovieSummary> movies =
                MovieSummaryMother.randomList(2);

        when(tmdbClient.getTrendingMovies("day", "en-US"))
                .thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(movies);

        List<MovieSummary> firstResult =
                movieCatalogService.getTrendingMovies(
                        TrendingWindow.DAY,
                        CatalogLanguage.EN_US
                );

        List<MovieSummary> secondResult =
                movieCatalogService.getTrendingMovies(
                        TrendingWindow.DAY,
                        CatalogLanguage.EN_US
                );

        assertThat(firstResult)
                .isSameAs(movies);

        assertThat(secondResult)
                .isSameAs(movies);

        assertThat(trendingCache().get("day::en-US"))
                .isNotNull()
                .extracting(Cache.ValueWrapper::get)
                .isSameAs(movies);

        verify(tmdbClient).getTrendingMovies("day", "en-US");
        verify(tmdbMovieMapper).toDomainList(providerResponse);
    }

    @Test
    void shouldKeepTrendingCacheEntriesSeparate() {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        List<MovieSummary> movies =
                MovieSummaryMother.randomList(1);

        when(tmdbClient.getTrendingMovies("day", "en-US"))
                .thenReturn(providerResponse);

        when(tmdbClient.getTrendingMovies("week", "en-US"))
                .thenReturn(providerResponse);

        when(tmdbClient.getTrendingMovies("day", "es-ES"))
                .thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(movies);

        movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        );

        movieCatalogService.getTrendingMovies(
                TrendingWindow.WEEK,
                CatalogLanguage.EN_US
        );

        movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.ES_ES
        );

        movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        );

        assertThat(trendingCache().get("day::en-US"))
                .isNotNull();

        assertThat(trendingCache().get("week::en-US"))
                .isNotNull();

        assertThat(trendingCache().get("day::es-ES"))
                .isNotNull();

        verify(tmdbClient).getTrendingMovies("day", "en-US");
        verify(tmdbClient).getTrendingMovies("week", "en-US");
        verify(tmdbClient).getTrendingMovies("day", "es-ES");

        verify(tmdbMovieMapper, times(3))
                .toDomainList(providerResponse);
    }

    @Test
    void shouldNotCacheEmptyTrendingResults() {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        when(tmdbClient.getTrendingMovies("day", "en-US"))
                .thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(List.of());

        movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        );

        movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        );

        assertThat(trendingCache().get("day::en-US"))
                .isNull();

        verify(tmdbClient, times(2))
                .getTrendingMovies("day", "en-US");

        verify(tmdbMovieMapper, times(2))
                .toDomainList(providerResponse);
    }

    @Test
    void shouldNotCacheProviderErrors() {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        List<MovieSummary> movies =
                MovieSummaryMother.randomList(1);

        TmdbServerException providerException =
                new TmdbServerException(
                        "Provider error",
                        503,
                        null,
                        METHOD_KEY
                );

        when(tmdbClient.getTrendingMovies("day", "en-US"))
                .thenThrow(providerException)
                .thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(movies);

        assertThatThrownBy(
                () -> movieCatalogService.getTrendingMovies(
                        TrendingWindow.DAY,
                        CatalogLanguage.EN_US
                )
        ).hasCause(providerException);

        assertThat(trendingCache().get("day::en-US"))
                .isNull();

        assertThat(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        )).isSameAs(movies);

        verify(tmdbClient, times(2))
                .getTrendingMovies("day", "en-US");
    }

    @Test
    void shouldCacheMovieDetailByIdAndLanguage() {
        Long movieId = 550L;

        TmdbMovieDetailResponse providerResponse =
                TmdbMovieDetailResponseMother.withId(movieId);

        MovieDetail movie =
                MovieDetailMother.withId(movieId);

        when(tmdbClient.getMovieById(movieId, "en-US"))
                .thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomain(providerResponse))
                .thenReturn(movie);

        MovieDetail firstResult =
                movieCatalogService.getMovieDetail(
                        movieId,
                        CatalogLanguage.EN_US
                );

        MovieDetail secondResult =
                movieCatalogService.getMovieDetail(
                        movieId,
                        CatalogLanguage.EN_US
                );

        assertThat(firstResult)
                .isSameAs(movie);

        assertThat(secondResult)
                .isSameAs(movie);

        assertThat(movieDetailCache().get("550::en-US"))
                .isNotNull()
                .extracting(Cache.ValueWrapper::get)
                .isSameAs(movie);

        verify(tmdbClient).getMovieById(movieId, "en-US");
        verify(tmdbMovieMapper).toDomain(providerResponse);
    }

    @Test
    void shouldKeepMovieDetailCacheEntriesSeparate() {
        Long firstMovieId = 550L;
        Long secondMovieId = 551L;

        TmdbMovieDetailResponse firstProviderResponse =
                TmdbMovieDetailResponseMother.withId(firstMovieId);

        TmdbMovieDetailResponse secondProviderResponse =
                TmdbMovieDetailResponseMother.withId(secondMovieId);

        when(tmdbClient.getMovieById(firstMovieId, "en-US"))
                .thenReturn(firstProviderResponse);

        when(tmdbClient.getMovieById(firstMovieId, "es-ES"))
                .thenReturn(firstProviderResponse);

        when(tmdbClient.getMovieById(secondMovieId, "en-US"))
                .thenReturn(secondProviderResponse);

        when(tmdbMovieMapper.toDomain(firstProviderResponse))
                .thenReturn(MovieDetailMother.withId(firstMovieId));

        when(tmdbMovieMapper.toDomain(secondProviderResponse))
                .thenReturn(MovieDetailMother.withId(secondMovieId));

        movieCatalogService.getMovieDetail(
                firstMovieId,
                CatalogLanguage.EN_US
        );

        movieCatalogService.getMovieDetail(
                firstMovieId,
                CatalogLanguage.ES_ES
        );

        movieCatalogService.getMovieDetail(
                secondMovieId,
                CatalogLanguage.EN_US
        );

        movieCatalogService.getMovieDetail(
                firstMovieId,
                CatalogLanguage.EN_US
        );

        assertThat(movieDetailCache().get("550::en-US"))
                .isNotNull();

        assertThat(movieDetailCache().get("550::es-ES"))
                .isNotNull();

        assertThat(movieDetailCache().get("551::en-US"))
                .isNotNull();

        verify(tmdbClient).getMovieById(firstMovieId, "en-US");
        verify(tmdbClient).getMovieById(firstMovieId, "es-ES");
        verify(tmdbClient).getMovieById(secondMovieId, "en-US");
    }

    @Test
    void shouldNotCacheNullMovieDetail() {
        Long movieId = 550L;

        TmdbMovieDetailResponse providerResponse =
                TmdbMovieDetailResponseMother.withId(movieId);

        when(tmdbClient.getMovieById(movieId, "en-US"))
                .thenReturn(providerResponse);

        when(tmdbMovieMapper.toDomain(providerResponse))
                .thenReturn(null);

        movieCatalogService.getMovieDetail(
                movieId,
                CatalogLanguage.EN_US
        );

        movieCatalogService.getMovieDetail(
                movieId,
                CatalogLanguage.EN_US
        );

        assertThat(movieDetailCache().get("550::en-US"))
                .isNull();

        verify(tmdbClient, times(2))
                .getMovieById(movieId, "en-US");

        verify(tmdbMovieMapper, times(2))
                .toDomain(providerResponse);
    }

    private Cache trendingCache() {
        return getRequiredCache(CatalogCacheNames.TRENDING_MOVIES);
    }

    private Cache movieDetailCache() {
        return getRequiredCache(CatalogCacheNames.MOVIE_DETAIL);
    }

    private Cache getRequiredCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        assertThat(cache)
                .as("cache %s", cacheName)
                .isNotNull();

        return cache;
    }
}
