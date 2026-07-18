package dev.alberto.moviecatalog.domain.model;

import lombok.Builder;

@Builder
public record MovieGenre(
        Long id,
        String name
) {
}
