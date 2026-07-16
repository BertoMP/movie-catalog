package dev.alberto.moviecatalog.infraestructure.tmdb.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "tmdb")
public record TmdbProperties(

        @NotNull
        URI baseUrl,

        @NotBlank(message = "TMDB access token must be configured")
        String accessToken,

        @NotBlank
        @Pattern(
                regexp = "^[a-z]{2}-[A-Z]{2}$",
                message = "TMDB default language must use the format en-US"
        )
        String defaultLanguage,

        @NotBlank
        @Pattern(
                regexp = "^[A-Z]{2}$",
                message = "TMDB default region must use the format US"
        )
        String defaultRegion
) {
}
