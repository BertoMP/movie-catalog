package dev.alberto.moviecatalog.infrastructure.tmdb.exception;

public class TmdbResourceNotFoundException
        extends TmdbException {

    public TmdbResourceNotFoundException(
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