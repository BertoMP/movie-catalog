package dev.alberto.moviecatalog.infraestructure.tmdb.config;

import feign.RequestInterceptor;

public class TmdbFeignConfiguration {

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
