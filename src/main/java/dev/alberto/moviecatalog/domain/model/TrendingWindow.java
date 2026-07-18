package dev.alberto.moviecatalog.domain.model;

public enum TrendingWindow {
    DAY("day"),
    WEEK("week");

    private final String value;

    TrendingWindow(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
