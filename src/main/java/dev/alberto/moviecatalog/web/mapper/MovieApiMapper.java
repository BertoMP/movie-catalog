package dev.alberto.moviecatalog.web.mapper;

import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.web.response.MovieSummaryResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieApiMapper {

    public List<MovieSummaryResponse> toResponse(List<MovieSummary> source) {
        return source.stream()
                .map(this::toResponse)
                .toList();
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
