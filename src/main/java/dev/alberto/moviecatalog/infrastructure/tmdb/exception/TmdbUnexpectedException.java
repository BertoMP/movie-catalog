package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

public class TmdbUnexpectedException extends TmdbException {

    public TmdbUnexpectedException(
            String message,
            int upstreamStatus,
            Integer upstreamCode,
            String methodKey
    ) {
        super(
                message,
                upstreamStatus,
                upstreamCode,
                methodKey
        );
    }
}