package dev.alberto.moviecatalog.web.response;

import java.util.List;

public record MoviePageResponse(
        Integer page,
        Integer totalPages,
        Integer totalResults,
        List<MovieSummaryResponse> results
) {
}