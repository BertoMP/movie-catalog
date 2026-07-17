package dev.alberto.moviecatalog.application.exception;

public class MovieProviderRequestException
        extends MovieProviderException {

    public MovieProviderRequestException(Throwable cause) {
        super(
                "The movie provider rejected the request",
                cause
        );
    }
}