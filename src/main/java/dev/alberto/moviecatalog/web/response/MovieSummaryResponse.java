package dev.alberto.moviecatalog.web.response;

import java.time.LocalDate;

public record MovieSummaryResponse(
        Long id,
        String title,
        String overview,
        String posterUrl,
        LocalDate releaseDate,
        Double rating
) {
}