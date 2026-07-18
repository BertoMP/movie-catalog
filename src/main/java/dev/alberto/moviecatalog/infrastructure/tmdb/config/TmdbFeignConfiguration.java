package dev.alberto.moviecatalog.infrastructure.tmdb.config;

import dev.alberto.moviecatalog.infrastructure.tmdb.error.TmbdErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.ObjectMapper;

public class TmdbFeignConfiguration {

    @Bean
    RequestInterceptor tmdbRequestInterceptor(TmdbProperties properties) {
        return requestTemplate -> {
            requestTemplate.header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + properties.accessToken()
            );

            requestTemplate.header(
                    "Accept",
                    "application/json"
            );
        };
    }

    @Bean
    ErrorDecoder tmdbErrorDecoder(ObjectMapper objectMapper) {
        return new TmbdErrorDecoder(objectMapper);
    }
}
