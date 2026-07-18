package dev.alberto.moviecatalog.infrastructure.tmdb.client;

import dev.alberto.moviecatalog.infrastructure.tmdb.config.TmdbFeignConfiguration;
import dev.alberto.moviecatalog.infrastructure.tmdb.dto.TmdbMoviePageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "tmdbClient",
        url = "${tmdb.base-url}",
        configuration = TmdbFeignConfiguration.class
)
public interface TmdbFeignClient {

    @GetMapping("/trending/movie/{timeWindow}")
    TmdbMoviePageResponse getTrendingMovies(
            @PathVariable("timeWindow") String timeWindow,
            @RequestParam("language") String language
    );
}
