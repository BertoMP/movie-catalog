package dev.alberto.moviecatalog.infrastructure.tmdb.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class TmdbFeignConfiguration {

    @Bean
    RequestInterceptor tmdbRequestInterceptor(TmdbProperties properties) {
        return requestTemplate -> {
            requestTemplate.header(
                    "Authorization",
                    "Bearer " + properties.accessToken()
            );

            requestTemplate.header(
                    "Accept",
                    "application/json"
            );
        };
    }
}
