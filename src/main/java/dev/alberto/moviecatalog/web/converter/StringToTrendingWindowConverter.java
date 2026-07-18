package dev.alberto.moviecatalog.web.converter;

import dev.alberto.moviecatalog.domain.model.TrendingWindow;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToTrendingWindowConverter implements Converter<String, TrendingWindow> {

    @Override
    public TrendingWindow convert(String source) {
        return TrendingWindow.valueOf(
                source.trim().toUpperCase(Locale.ROOT)
        );
    }
}