package dev.alberto.moviecatalog.infrastructure.cache.config;

import dev.alberto.moviecatalog.application.cache.CatalogCacheNames;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CatalogRedisCacheConfiguration
        implements CachingConfigurer {

    private static final String CACHE_PREFIX = "catalog::";

    @Bean
    RedisCacheConfiguration redisCacheConfiguration() {
        RedisSerializationContext.SerializationPair<String> keySerializer =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.string());

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> CACHE_PREFIX + cacheName + "::")
                .serializeKeysWith(keySerializer);
    }

    @Bean
    RedisCacheManagerBuilderCustomizer
    movieRedisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration defaultConfiguration,
            CatalogCacheProperties properties,
            ObjectMapper objectMapper
    ) {
        JavaType trendingMoviesType = objectMapper
                .getTypeFactory()
                .constructCollectionType(
                        List.class,
                        MovieSummary.class
                );

        JacksonJsonRedisSerializer<List<MovieSummary>> trendingMoviesSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        trendingMoviesType
                );

        JacksonJsonRedisSerializer<MovieDetail> movieDetailSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        MovieDetail.class
                );

        return builder -> builder
                .withCacheConfiguration(
                        CatalogCacheNames.TRENDING_MOVIES,
                        defaultConfiguration
                                .entryTtl(properties.trendingTtl())
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair.fromSerializer(
                                                trendingMoviesSerializer
                                        )
                                )
                )
                .withCacheConfiguration(
                        CatalogCacheNames.MOVIE_DETAIL,
                        defaultConfiguration
                                .entryTtl(properties.movieDetailTtl())
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair.fromSerializer(
                                                movieDetailSerializer
                                        )
                                )
                );
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler(true);
    }
}
