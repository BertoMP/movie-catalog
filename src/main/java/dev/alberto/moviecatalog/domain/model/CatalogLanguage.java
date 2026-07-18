package dev.alberto.moviecatalog.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum CatalogLanguage {

    EN_US("en-US"),
    ES_ES("es-ES"),
    FR_FR("fr-FR"),
    DE_DE("de-DE"),
    IT_IT("it-IT");

    private final String value;

    public Locale toLocale() {
        return Locale.forLanguageTag(value);
    }

    public static CatalogLanguage fromLocale(Locale locale) {
        return Arrays.stream(values())
                .filter(language ->
                        language.value.equalsIgnoreCase(
                                locale.toLanguageTag()
                        )
                )
                .findFirst()
                .orElse(EN_US);
    }
}
