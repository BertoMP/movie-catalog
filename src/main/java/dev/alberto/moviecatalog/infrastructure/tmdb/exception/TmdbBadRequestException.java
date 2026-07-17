package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

public class TmdbBadRequestException extends TmdbException {

    public TmdbBadRequestException(
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