package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

public class TmdbForbiddenException extends TmdbException {

    public TmdbForbiddenException(
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