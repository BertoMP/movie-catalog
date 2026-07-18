package dev.alberto.moviecatalog.web.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MovieSummaryResponse(
        Long id,
        String title,
        String overview,
        String posterUrl,
        LocalDate releaseDate,
        Double rating
) {
}
