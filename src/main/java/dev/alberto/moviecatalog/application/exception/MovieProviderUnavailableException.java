package dev.alberto.moviecatalog.application.exception;

public class MovieProviderUnavailableException
        extends MovieProviderException {

    public MovieProviderUnavailableException(Throwable cause) {
        super(
                "The movie provider is currently unavailable",
                cause
        );
    }
}