package com.whatiwatch.domain;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.whatiwatch.config.InvalidFilterException;

class MovieFilterTest {
    
    @Test
    void emptyFilterHasDefaultParams() {
        Map<String, String> params = new MovieFilter().toQueryParams();

        assertEquals("en", params.get("language"));
        assertEquals("vote_average.desc", params.get("sort_by"));
        assertEquals("100", params.get("vote_count.gte"));
    }

    @Test
    void decadeConvertsToDateRange() {
        Map<String, String> params = new MovieFilter() 
            .withDecade("1990s")
            .toQueryParams();

        assertEquals("1990-01-01", params.get("primary_release_date.gte"));
        assertEquals("1999-12-31", params.get("primary_release_date.lte"));
    }

    @Test
    void multipleGenresJoinedWithComma() {
        Map<String, String> params = new MovieFilter()
            .withGenre(28)
            .withGenre(35)
            .toQueryParams();

        String genres = params.get("with_genres");
        assertTrue(genres.contains("28"));
        assertTrue(genres.contains("35"));
        assertTrue(genres.contains(","));
    }

    @Test
    void duplicateGenreOnlyAddedOnce() {
        Map<String, String> params = new MovieFilter()
            .withGenre(28)
            .withGenre(28)
            .toQueryParams();

        assertEquals("28", params.get("with_genres"));
    }

    @Test
    void countryCodeAddedToParams() {
        Map<String, String> params = new MovieFilter()
            .withCountry("JP")
            .toQueryParams();
        
        assertEquals("JP", params.get("with_origin_country"));
    }

    @Test
    void invalidDecadeThrowsException() {
        assertThrows(InvalidFilterException.class, () -> 
            new MovieFilter().withDecade("90s")
        );
    }

    @Test
    void invalidCountryCodeThrowsException() {
        assertThrows(InvalidFilterException.class, () ->
            new MovieFilter().withCountry("japan") 
        );
    }

    @Test
    void languageDefaultsToEnglish() {
        MovieFilter filter = new MovieFilter();
        assertEquals("en", filter.getLanguage());
    }

    @Test
    void languageCanBeChanged() {
        Map<String, String> params = new MovieFilter()
            .withLanguage("pl")
            .toQueryParams();

        assertEquals("pl", params.get("language"));
    }

}
