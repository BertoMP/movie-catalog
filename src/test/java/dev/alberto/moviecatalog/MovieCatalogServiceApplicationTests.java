package dev.alberto.moviecatalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "tmdb.access-token=test-token")
class MovieCatalogServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
