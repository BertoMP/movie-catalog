package dev.alberto.moviecatalog.infrastructure.tmdb.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "tmdb")
public record TmdbProperties(

        @NotNull
        URI baseUrl,

        @NotBlank(message = "TMDB access token must be configured")
        String accessToken
) {
}
