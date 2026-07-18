package dev.alberto.moviecatalog.application.exception;

public class MovieProviderAccessException
        extends MovieProviderException {

    public MovieProviderAccessException(Throwable cause) {
        super(
                "The movie provider rejected the service credentials",
                cause
        );
    }
}