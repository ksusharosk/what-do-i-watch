package com.whatiwatch.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.whatiwatch.domain.Movie;
import com.whatiwatch.tmdb.TmdbClient;

/**
 * Movie lookup endpoints — searching TMDB so users can find films to rate or
 * add to their watchlist.
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final TmdbClient tmdbClient;

    public MovieController(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    /**
     * Searches for movies by title (optionally narrowed by year).
     * Public — guests can search and browse; saving requires login.
     *
     * Example: GET /api/movies/search?title=parasite&year=2019
     */
    @GetMapping("/search")
    public List<Movie> search(@RequestParam String title,
                              @RequestParam(required = false, defaultValue = "0") int year) {
        return tmdbClient.searchMovie(title, year);
    }
}