package dev.alberto.moviecatalog.domain.service;

import dev.alberto.moviecatalog.domain.model.MoviePage;
import dev.alberto.moviecatalog.domain.model.TrendingWindow;

public interface MovieCatalogService {
    MoviePage getTrendingMovies(TrendingWindow window);
}
