package dev.alberto.moviecatalog.domain.model;

import java.util.List;

public record MoviePage(
        Integer page,
        Integer totalPages,
        Integer totalResults,
        List<MovieSummary> results
) {

    public MoviePage {
        results = results == null
                ? List.of()
                : List.copyOf(results);
    }
}