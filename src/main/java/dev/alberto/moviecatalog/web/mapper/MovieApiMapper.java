package dev.alberto.moviecatalog.web.mapper;

import dev.alberto.moviecatalog.domain.model.MoviePage;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.web.response.MoviePageResponse;
import dev.alberto.moviecatalog.web.response.MovieSummaryResponse;

import org.springframework.stereotype.Component;

@Component
public class MovieApiMapper {

    public MoviePageResponse toResponse(MoviePage source) {
        return new MoviePageResponse(
                source.page(),
                source.totalPages(),
                source.totalResults(),
                source.results()
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private MovieSummaryResponse toResponse(MovieSummary source) {
        return new MovieSummaryResponse(
                source.id(),
                source.title(),
                source.overview(),
                source.posterUrl(),
                source.releaseDate(),
                source.rating()
        );
    }
}