package dev.alberto.moviecatalog.application.exception;

public class CatalogProviderAccessException
        extends CatalogProviderException {

    public CatalogProviderAccessException(Throwable cause) {
        super(
                "The catalog provider rejected the service credentials",
                cause
        );
    }
}
