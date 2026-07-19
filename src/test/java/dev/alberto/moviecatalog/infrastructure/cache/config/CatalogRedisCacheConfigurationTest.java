package dev.alberto.moviecatalog.infrastructure.cache.config;

import dev.alberto.moviecatalog.application.cache.CatalogCacheNames;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.testdata.domain.MovieDetailMother;
import dev.alberto.moviecatalog.testdata.domain.MovieSummaryMother;
import org.junit.jupiter.api.Test;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CatalogRedisCacheConfigurationTest {

    private final CatalogRedisCacheConfiguration configuration =
            new CatalogRedisCacheConfiguration();

    @Test
    void shouldConfigureRedisKeysAndCachePrefix() {
        RedisCacheConfiguration redisConfiguration =
                configuration.redisCacheConfiguration();

        assertThat(redisConfiguration.getAllowCacheNullValues())
                .isFalse();

        assertThat(redisConfiguration.getKeyPrefixFor(
                CatalogCacheNames.TRENDING_MOVIES
        )).isEqualTo("catalog::trending-movies::");

        ByteBuffer serializedKey =
                redisConfiguration
                        .getKeySerializationPair()
                        .write("day::en-US");

        assertThat(StandardCharsets.UTF_8.decode(serializedKey).toString())
                .isEqualTo("day::en-US");
    }

    @Test
    void shouldConfigureSpecificTtlForEveryCatalogCache() {
        Duration trendingTtl = Duration.ofMinutes(15);
        Duration movieDetailTtl = Duration.ofHours(6);

        CatalogCacheProperties properties =
                new CatalogCacheProperties(
                        trendingTtl,
                        movieDetailTtl
                );

        RedisCacheConfiguration defaultConfiguration =
                configuration.redisCacheConfiguration();

        RedisCacheManagerBuilderCustomizer customizer =
                configuration.movieRedisCacheManagerBuilderCustomizer(
                        defaultConfiguration,
                        properties,
                        objectMapper()
                );

        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(
                        mock(RedisCacheWriter.class)
                );

        customizer.customize(builder);

        assertThat(builder.getConfiguredCaches())
                .containsExactlyInAnyOrder(
                        CatalogCacheNames.TRENDING_MOVIES,
                        CatalogCacheNames.MOVIE_DETAIL
                );

        assertThat(ttlFor(builder, CatalogCacheNames.TRENDING_MOVIES))
                .isEqualTo(trendingTtl);

        assertThat(ttlFor(builder, CatalogCacheNames.MOVIE_DETAIL))
                .isEqualTo(movieDetailTtl);
    }

    @Test
    void shouldRoundTripEveryCatalogCacheValueAsJson() {
        CatalogCacheProperties properties =
                new CatalogCacheProperties(
                        Duration.ofMinutes(15),
                        Duration.ofHours(6)
                );

        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(
                        mock(RedisCacheWriter.class)
                );

        configuration.movieRedisCacheManagerBuilderCustomizer(
                configuration.redisCacheConfiguration(),
                properties,
                objectMapper()
        ).customize(builder);

        List<MovieSummary> trendingMovies =
                MovieSummaryMother.randomList(2);

        MovieDetail movieDetail =
                MovieDetailMother.withId(550L);

        assertThat(roundTrip(
                builder,
                CatalogCacheNames.TRENDING_MOVIES,
                trendingMovies
        )).isEqualTo(trendingMovies);

        assertThat(roundTrip(
                builder,
                CatalogCacheNames.MOVIE_DETAIL,
                movieDetail
        )).isEqualTo(movieDetail);
    }

    @Test
    void shouldUseAResilientCacheErrorHandler() {
        assertThat(configuration.errorHandler())
                .isInstanceOf(LoggingCacheErrorHandler.class);
    }

    private Duration ttlFor(
            RedisCacheManager.RedisCacheManagerBuilder builder,
            String cacheName
    ) {
        RedisCacheConfiguration cacheConfiguration =
                builder.getCacheConfigurationFor(cacheName)
                        .orElseThrow();

        return cacheConfiguration
                .getTtlFunction()
                .getTimeToLive("key", "value");
    }

    private Object roundTrip(
            RedisCacheManager.RedisCacheManagerBuilder builder,
            String cacheName,
            Object value
    ) {
        RedisCacheConfiguration cacheConfiguration =
                builder.getCacheConfigurationFor(cacheName)
                        .orElseThrow();

        ByteBuffer serializedValue = cacheConfiguration
                .getValueSerializationPair()
                .write(value);

        return cacheConfiguration
                .getValueSerializationPair()
                .read(serializedValue);
    }

    private ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
