package com.whatiwatch.recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.user.MovieRating;
import com.whatiwatch.domain.user.TasteProfile;
import com.whatiwatch.domain.user.WatchListEntry;

class TasteProfileServiceTest {

    // ---- helpers ----

    /** A fetcher backed by a map — no HTTP. Returns null for unknown ids. */
    private Function<Integer, Movie> fetcherFor(Map<Integer, Movie> movies) {
        return movies::get;
    }

    /** Builds a Movie with just the fields the service reads. */
    private Movie movie(int id, List<String> genres, String country,
                        int year, List<Movie.Person> directors, List<Movie.Person> actors) {
        return new Movie(
                id, "Movie " + id, "Movie " + id, "", year,
                genres, country, "en", 8.0, 1000, "",
                directors, actors);
    }

    /** A positive rating (8+) for a given movie id. */
    private MovieRating positiveRating(int movieId) {
        return MovieRating.create("u1", movieId, "Movie " + movieId, 9, null);
    }

    /** A negative rating (≤4) for a given movie id. */
    private MovieRating negativeRating(int movieId) {
        return MovieRating.create("u1", movieId, "Movie " + movieId, 2, null);
    }

    private TasteProfileService serviceWith(Map<Integer, Movie> movies) {
        return new TasteProfileService(fetcherFor(movies));
    }

    // ---- validation ----

    @Test
    void nullFetcherRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new TasteProfileService(null));
    }

    @Test
    void nullUserIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> serviceWith(Map.of()).build(null, List.of(), List.of()));
    }

    // ---- empty history ----

    @Test
    void noRatingsReturnsEmptyProfile() {
        TasteProfile profile = serviceWith(Map.of())
                .build("u1", List.of(), List.of());

        assertFalse(profile.hasEnoughData());
        assertTrue(profile.likedGenres().isEmpty());
    }

    @Test
    void noRatingsMakesNoFetchCalls() {
        // A fetcher that fails the test if ever called
        Function<Integer, Movie> exploding = id -> {
            throw new AssertionError("fetcher should not be called with no ratings");
        };
        TasteProfileService service = new TasteProfileService(exploding);

        TasteProfile profile = service.build("u1", List.of(), List.of());

        assertFalse(profile.hasEnoughData());
    }

    // ---- title lists ----

    @Test
    void collectsHighlyAndPoorlyRatedTitles() {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Drama"), "US", 2010, List.of(), List.of()));
        movies.put(2, movie(2, List.of("Horror"), "US", 2010, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), negativeRating(2)),
                List.of());

        assertTrue(profile.highlyRatedTitles().contains("Movie 1"));
        assertTrue(profile.poorlyRatedTitles().contains("Movie 2"));
    }

    @Test
    void collectsWatchedTitles() {
        WatchListEntry watched = WatchListEntry.create("u1", 5, "Watched Movie").markAsWatched();
        WatchListEntry wantToWatch = WatchListEntry.create("u1", 6, "Later Movie");

        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Drama"), "US", 2010, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1)),
                List.of(watched, wantToWatch));

        assertTrue(profile.alreadyWatchedTitles().contains("Watched Movie"));
        assertFalse(profile.alreadyWatchedTitles().contains("Later Movie"));
    }

    // ---- the strict 2+ threshold ----

    @Test
    void genreInTwoFilmsIsLiked() {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Thriller"), "US", 2010, List.of(), List.of()));
        movies.put(2, movie(2, List.of("Thriller"), "US", 2011, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), positiveRating(2)),
                List.of());

        assertTrue(profile.likedGenres().contains("Thriller"));
    }

    @Test
    void genreInOneFilmIsNotLiked() {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Thriller"), "US", 2010, List.of(), List.of()));
        movies.put(2, movie(2, List.of("Comedy"), "US", 2011, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), positiveRating(2)),
                List.of());

        // Each genre appears once → neither meets the 2+ threshold
        assertFalse(profile.likedGenres().contains("Thriller"));
        assertFalse(profile.likedGenres().contains("Comedy"));
    }

    @Test
    void directorInTwoFilmsIsFavourite() {
        Movie.Person fincher = new Movie.Person(1, "David Fincher");
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Drama"), "US", 2010, List.of(fincher), List.of()));
        movies.put(2, movie(2, List.of("Drama"), "US", 2011, List.of(fincher), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), positiveRating(2)),
                List.of());

        assertTrue(profile.favouriteDirectors().contains(fincher));
    }

    @Test
    void directorInOneFilmIsNotFavourite() {
        Movie.Person fincher = new Movie.Person(1, "David Fincher");
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Drama"), "US", 2010, List.of(fincher), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1)),
                List.of());

        assertFalse(profile.favouriteDirectors().contains(fincher));
    }

    // ---- decades & countries ----

    @Test
    void decadeInTwoFilmsIsPreferred() {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Drama"), "US", 1994, List.of(), List.of()));
        movies.put(2, movie(2, List.of("Horror"), "US", 1998, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), positiveRating(2)),
                List.of());

        // Both films are 1990s → "1990s" appears twice
        assertTrue(profile.preferredDecades().contains("1990s"));
    }

    @Test
    void countryInTwoFilmsIsPreferred() {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Drama"), "KR", 2019, List.of(), List.of()));
        movies.put(2, movie(2, List.of("Thriller"), "KR", 2003, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), positiveRating(2)),
                List.of());

        assertTrue(profile.preferredCountries().contains("KR"));
    }

    // ---- fetch failures ----

    @Test
    void skipsMoviesThatFailToFetch() {
        // Movie 2 is missing from the map → fetcher returns null for it
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, movie(1, List.of("Thriller"), "US", 2010, List.of(), List.of()));

        TasteProfile profile = serviceWith(movies).build(
                "u1",
                List.of(positiveRating(1), positiveRating(2)),  // id 2 unfetchable
                List.of());

        // Still builds a profile from the one that fetched; titles come from ratings
        assertTrue(profile.highlyRatedTitles().contains("Movie 1"));
        assertTrue(profile.highlyRatedTitles().contains("Movie 2"));
    }
}