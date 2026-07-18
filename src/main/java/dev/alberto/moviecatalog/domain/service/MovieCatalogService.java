package dev.alberto.moviecatalog.domain.service;

import dev.alberto.moviecatalog.domain.model.CatalogLanguage;
import dev.alberto.moviecatalog.domain.model.MovieDetail;
import dev.alberto.moviecatalog.domain.model.MovieSummary;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;

import java.util.List;

public interface MovieCatalogService {
    List<MovieSummary> getTrendingMovies(TrendingWindow window, CatalogLanguage language);
    MovieDetail getMovieDetail(Long movieId, CatalogLanguage language);
}
