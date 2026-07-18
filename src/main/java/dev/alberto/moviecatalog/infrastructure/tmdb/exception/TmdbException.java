package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

import lombok.Getter;

@Getter
public abstract class TmdbException extends RuntimeException {

    private final int upstreamStatus;
    private final Integer upstreamCode;
    private final String methodKey;

    protected TmdbException(
            String message,
            int upstreamStatus,
            Integer upstreamCode,
            String methodKey
    ) {
        super(message);
        this.upstreamStatus = upstreamStatus;
        this.upstreamCode = upstreamCode;
        this.methodKey = methodKey;
    }
}