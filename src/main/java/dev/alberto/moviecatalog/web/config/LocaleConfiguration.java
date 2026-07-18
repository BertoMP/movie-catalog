package dev.alberto.moviecatalog.web.config;

import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;

@Configuration
public class LocaleConfiguration {

    @Bean
    LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver =
                new AcceptHeaderLocaleResolver();

        resolver.setSupportedLocales(
                Arrays.stream(CatalogLanguage.values())
                        .map(CatalogLanguage::toLocale)
                        .toList()
        );

        resolver.setDefaultLocale(
                CatalogLanguage.EN_US.toLocale()
        );

        return resolver;
    }
}