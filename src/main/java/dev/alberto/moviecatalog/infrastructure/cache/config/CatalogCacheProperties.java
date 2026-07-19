package dev.alberto.moviecatalog.infrastructure.cache.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "catalog.cache")
public record CatalogCacheProperties(
        @NotNull
        Duration trendingTtl,

        @NotNull
        Duration movieDetailTtl
) {
}
