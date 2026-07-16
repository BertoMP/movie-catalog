package dev.alberto.moviecatalog.domain.model;

import java.time.LocalDate;

public record MovieSummary(
        Long id,
        String title,
        String overview,
        String posterUrl,
        LocalDate releaseDate,
        Double rating
) {
}