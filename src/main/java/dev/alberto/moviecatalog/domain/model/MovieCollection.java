package dev.alberto.moviecatalog.domain.model;

import lombok.Builder;

@Builder
public record MovieCollection(
        Long id,
        String name,
        String posterUrl,
        String backdropUrl
) {
}
