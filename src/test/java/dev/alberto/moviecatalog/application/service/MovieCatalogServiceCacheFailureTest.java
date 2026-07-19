package dev.alberto.moviecatalog.application.service;

import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.infrastructure.tmdb.client.TmdbFeignClient;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.mapper.TmdbMovieMapper;
import dev.alberto.moviecatalog.testdata.domain.MovieSummaryMother;
import dev.alberto.moviecatalog.testdata.tmdb.TmdbMoviePageResponseMother;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cache.type=none",
                "spring.docker.compose.enabled=false",
                "tmdb.access-token=test-token"
        },
        classes = {
                dev.alberto.moviecatalog.MovieCatalogServiceApplication.class,
                MovieCatalogServiceCacheFailureTest.FailingCacheConfiguration.class
        }
)
class MovieCatalogServiceCacheFailureTest {

    @Autowired
    private MovieCatalogService movieCatalogService;

    @MockitoBean
    private TmdbFeignClient tmdbClient;

    @MockitoBean
    private TmdbMovieMapper tmdbMovieMapper;

    @Test
    void shouldReturnProviderResultsWhenCacheReadsAndWritesFail() {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        List<MovieSummary> movies = MovieSummaryMother.randomList(2);

        when(tmdbClient.getTrendingMovies("day", "en-US"))
                .thenReturn(providerResponse);
        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(movies);

        assertThat(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        )).isEqualTo(movies);

        assertThat(movieCatalogService.getTrendingMovies(
                TrendingWindow.DAY,
                CatalogLanguage.EN_US
        )).isEqualTo(movies);

        verify(tmdbClient, times(2))
                .getTrendingMovies("day", "en-US");
        verify(tmdbMovieMapper, times(2))
                .toDomainList(providerResponse);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailingCacheConfiguration {

        @Bean
        @Primary
        CacheManager failingCacheManager() {
            DataAccessResourceFailureException cacheFailure =
                    new DataAccessResourceFailureException(
                            "Redis is unavailable"
                    );

            Cache cache = mock(Cache.class);
            when(cache.get(any())).thenThrow(cacheFailure);
            doThrow(cacheFailure).when(cache).put(any(), any());

            CacheManager cacheManager = mock(CacheManager.class);
            when(cacheManager.getCache(anyString())).thenReturn(cache);

            return cacheManager;
        }
    }
}
