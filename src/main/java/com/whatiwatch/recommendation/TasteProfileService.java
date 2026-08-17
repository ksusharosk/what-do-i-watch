package com.whatiwatch.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.user.MovieRating;
import com.whatiwatch.domain.user.TasteProfile;
import com.whatiwatch.domain.user.WatchListEntry;

/*
 - Builds a {@link TasteProfile} from a user's rating and watch history.
 - Fetches the full Movie for each rated film (via an injected fetch function)
 and aggregates patterns across the movies the user rated highly vs poorly.
 - A pattern must appear in at least MIN_OCCURRENCES highly-rated films to count
 - The movie fetcher is passed in (e.g. tmdbClient::getMovie)
 */
public final class TasteProfileService {
    
    private static final int MIN_OCCURRENCES = 2;
    private static final int MAX_GENRES = 10;
    private static final int MAX_DIRECTORS = 5;
    private static final int MAX_ACTORS = 5;
    private final static int MAX_DECADES = 3;
    private final static int MAX_COUNTRIES = 3;

    private final Function<Integer, Movie> movieFetcher;

    /**
     * @param movieFetcher fetches a full movie by its TMDB id 
     * (e.g {@code tmdbClient::getMovie})
     */
    public TasteProfileService(Function<Integer, Movie> movieFetcher) {
        if (movieFetcher == null) {
            throw new IllegalArgumentException("movieFetcher cannot be null");
        }
        this.movieFetcher = movieFetcher;
    }

    public TasteProfile build(String userId, List<MovieRating> ratings, List<WatchListEntry> watchlist) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userID cannot be null or blank");
        }
        if (ratings == null) {
            throw new IllegalArgumentException("ratings cannot be null");
        }
        if (watchlist == null) {
            throw new IllegalArgumentException("watchlist cannot be null");
        }

        List<String> highlyRatedTitles = titlesOf(ratings, MovieRating::isPositive);
        List<String> poorlyRatedTitles = titlesOf(ratings, MovieRating::isNegative);
        List<String> alreadyWatchedTitles = watchlist.stream()
                .filter(WatchListEntry::isWatched)
                .map(WatchListEntry::movieTitle)
                .distinct()
                .toList();
        
        // No rating history -> generic recommendation
        if (highlyRatedTitles.isEmpty() && poorlyRatedTitles.isEmpty()) {
            return TasteProfile.empty(userId);
        }

        // Fetch full movie data for positively and negatively rated films
        List<Movie> lovedMovies = fetchMovies(ratings, MovieRating::isPositive);
        List<Movie> dislikedMovies = fetchMovies(ratings, MovieRating::isNegative);

        List<String> likedGenres = topGenres(lovedMovies);
        List<String> dislikedGenres = topGenres(dislikedMovies);
        List<Movie.Person> favouriteDirectors = topPeople(lovedMovies, Movie::directors, MAX_DIRECTORS);
        List<Movie.Person> favouriteActors = topPeople(lovedMovies, Movie::actors, MAX_ACTORS);
        List<String> preferredDecades = topDecades(lovedMovies);
        List<String> preferredCountires = topCountries(lovedMovies);

        return new TasteProfile(
                userId,
                likedGenres,
                dislikedGenres,
                favouriteDirectors,
                favouriteActors,
                preferredDecades,
                preferredCountires,
                alreadyWatchedTitles,
                highlyRatedTitles,
                poorlyRatedTitles,
                null // aiSummary - placeholder
        );

    }

    // Distinct titles of rating matching the predicate
    private List<String> titlesOf(List<MovieRating> ratings, 
                                java.util.function.Predicate<MovieRating> predicate) {
        return ratings.stream()
                .filter(predicate)
                .map(MovieRating::movieTitle)
                .distinct()
                .toList();
    }

    // Fetches full Movie data for rating matching the predicate
    private List<Movie> fetchMovies(List<MovieRating> ratings,
                                    java.util.function.Predicate<MovieRating> predicate) {
        List<Movie> movies = new ArrayList<>();
        for (MovieRating rating : ratings) {
            if (!predicate.test(rating)) {
                continue;
            }
            Movie movie = movieFetcher.apply(rating.movieId());
            if (movie != null) {
                movies.add(movie);
            }
        }
        return movies;
    }

    private List<String> topGenres(List<Movie> movies) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Movie movie : movies) {
            for (String genre : movie.genres()) {
                counts.merge(genre, 1, Integer::sum);
            }
        }
        return topByCount(counts, MAX_GENRES);
    }

    private List<Movie.Person> topPeople(List<Movie> movies,
                                        Function<Movie, List<Movie.Person>> peopleOf,
                                        int limit) {
        Map<Movie.Person, Integer> counts = new LinkedHashMap<>();
        for (Movie movie : movies) {
            for (Movie.Person person : peopleOf.apply(movie)) {
                counts.merge(person, 1, Integer::sum);
            }
        }
        return topByCount(counts, limit);
    }

    private List<String> topDecades(List<Movie> movies) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Movie movie : movies) {
            String decade = toDecade(movie.year());
            if (decade != null) {
                counts.merge(decade, 1, Integer::sum);
            }
        }
        return topByCount(counts, MAX_DECADES);
    }

    private List<String> topCountries(List<Movie> movies) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Movie movie : movies) {
            String country = movie.countryCode();
            if (country != null && !country.isBlank()) {
                counts.merge(country, 1, Integer::sum);
            }
        }
        return topByCount(counts, MAX_COUNTRIES);
    }

    // Keeps entries occuring at least MIN_OCCURENCES times ordered by frequency, capped at limit
    private <T> List<T> topByCount(Map<T, Integer> counts, int limit) {
        return counts.entrySet().stream()
            .filter(e -> e.getValue() >= MIN_OCCURRENCES)
            .sorted(Comparator.comparingInt(Map.Entry<T, Integer>::getValue).reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();
    }

    // Turns a year into a decade label, e.g 1994 -> 1990s
    private String toDecade(int year) {
        if (year <= 0) {
            return null;
        }
        int start = (year/10) * 10;
        return start + "s";
    }

}
