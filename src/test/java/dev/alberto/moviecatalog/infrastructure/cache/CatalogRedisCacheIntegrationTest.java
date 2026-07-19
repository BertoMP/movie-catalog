package dev.alberto.moviecatalog.infrastructure.cache;

import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import dev.alberto.moviecatalog.domain.service.MovieCatalogService;
import dev.alberto.moviecatalog.infrastructure.cache.config.CatalogCacheProperties;
import dev.alberto.moviecatalog.infrastructure.tmdb.client.TmdbFeignClient;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMovieDetailResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import dev.alberto.moviecatalog.infrastructure.tmdb.mapper.TmdbMovieMapper;
import dev.alberto.moviecatalog.testdata.domain.MovieDetailMother;
import dev.alberto.moviecatalog.testdata.domain.MovieSummaryMother;
import dev.alberto.moviecatalog.testdata.tmdb.TmdbMovieDetailResponseMother;
import dev.alberto.moviecatalog.testdata.tmdb.TmdbMoviePageResponseMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CatalogRedisCacheIntegrationTest {

    private static final String REDIS_USERNAME = "movie_catalog";
    private static final String REDIS_PASSWORD = "test-password";
    private static final String TRENDING_KEY =
            "catalog::trending-movies::day::en-US";
    private static final String MOVIE_DETAIL_KEY =
            "catalog::movie-detail::550::en-US";

    @Container
    private static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:8-alpine")
                    .withExposedPorts(6379)
                    .withCommand(
                            "/bin/sh",
                            "-c",
                            ("printf 'user default off\\nuser %s on >%s "
                                    + "~catalog::* &* +@all\\n' > /tmp/users.acl "
                                    + "&& exec redis-server --aclfile /tmp/users.acl")
                                    .formatted(
                                            REDIS_USERNAME,
                                            REDIS_PASSWORD
                                    )
                    );

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add(
                "spring.data.redis.port",
                () -> REDIS.getMappedPort(6379)
        );
        registry.add("spring.data.redis.username", () -> REDIS_USERNAME);
        registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);
        registry.add("spring.docker.compose.enabled", () -> false);
        registry.add("catalog.cache.trending-ttl", () -> "1m");
        registry.add("catalog.cache.movie-detail-ttl", () -> "2m");
        registry.add("tmdb.access-token", () -> "test-token");
    }

    @Autowired
    private MovieCatalogService movieCatalogService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CatalogCacheProperties cacheProperties;

    @MockitoBean
    private TmdbFeignClient tmdbClient;

    @MockitoBean
    private TmdbMovieMapper tmdbMovieMapper;

    @BeforeEach
    void clearCatalogCache() {
        Set<String> keys = redisTemplate.keys("catalog::*");

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldCacheAndDeserializeTrendingMoviesInRedis() {
        TmdbMoviePageResponse providerResponse =
                TmdbMoviePageResponseMother.random();

        List<MovieSummary> movies = MovieSummaryMother.randomList(2);

        when(tmdbClient.getTrendingMovies("day", "en-US"))
                .thenReturn(providerResponse);
        when(tmdbMovieMapper.toDomainList(providerResponse))
                .thenReturn(movies);

        List<MovieSummary> firstResult =
                movieCatalogService.getTrendingMovies(
                        TrendingWindow.DAY,
                        CatalogLanguage.EN_US
                );

        List<MovieSummary> cachedResult =
                movieCatalogService.getTrendingMovies(
                        TrendingWindow.DAY,
                        CatalogLanguage.EN_US
                );

        assertThat(firstResult).isEqualTo(movies);
        assertThat(cachedResult)
                .isEqualTo(movies)
                .isNotSameAs(firstResult);
        assertTtl(TRENDING_KEY, cacheProperties.trendingTtl());

        verify(tmdbClient).getTrendingMovies("day", "en-US");
        verify(tmdbMovieMapper).toDomainList(providerResponse);
    }

    @Test
    void shouldCacheAndDeserializeMovieDetailInRedis() {
        Long movieId = 550L;

        TmdbMovieDetailResponse providerResponse =
                TmdbMovieDetailResponseMother.withId(movieId);

        MovieDetail movie = MovieDetailMother.withId(movieId);

        when(tmdbClient.getMovieById(movieId, "en-US"))
                .thenReturn(providerResponse);
        when(tmdbMovieMapper.toDomain(providerResponse))
                .thenReturn(movie);

        MovieDetail firstResult = movieCatalogService.getMovieDetail(
                movieId,
                CatalogLanguage.EN_US
        );

        MovieDetail cachedResult = movieCatalogService.getMovieDetail(
                movieId,
                CatalogLanguage.EN_US
        );

        assertThat(firstResult).isEqualTo(movie);
        assertThat(cachedResult)
                .isEqualTo(movie)
                .isNotSameAs(firstResult);
        assertTtl(MOVIE_DETAIL_KEY, cacheProperties.movieDetailTtl());

        verify(tmdbClient).getMovieById(movieId, "en-US");
        verify(tmdbMovieMapper).toDomain(providerResponse);
    }

    private void assertTtl(String key, Duration expectedTtl) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);

        assertThat(redisTemplate.hasKey(key)).isTrue();
        assertThat(ttl)
                .isPositive()
                .isLessThanOrEqualTo(expectedTtl.toMillis())
                .isGreaterThan(expectedTtl.minusSeconds(10).toMillis());
    }
}
