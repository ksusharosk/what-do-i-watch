package com.whatiwatch.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.whatiwatch.config.InvalidFilterException;

/* 
- Represents the filters a user can apply when searching for movies.
- Uses a fluent API - filters can be chained together like: 
    new MovieFilter().withGenre(28).withDecade("1990s").withLanguage("pl")
*/

public class MovieFilter {
    
    private final Set<Integer> genreIds = new HashSet<>();
    private final List<Movie.Person> actors = new ArrayList<>();
    private final List<Movie.Person> directors = new ArrayList<>();
    private String decade;
    private String countryCode;
    private String language = "en";
    private boolean includeWatched = false;

    // -------Fluent Setters--------
    public MovieFilter withGenre(int genreId) {
        genreIds.add(genreId);
        return this;
    }

    public MovieFilter withDecade(String decade) {
        if (!decade.matches("\\d{4}s")) {
            throw new InvalidFilterException("Decade must be in format '1990s', got: " + decade);
        }
        this.decade = decade;
        return this;
    }

    public MovieFilter withCountry(String countryCode) {
        if (!countryCode.matches("[A-Z]{2}")) {
            throw new InvalidFilterException("Country code must be 2 uppercase letters (e.g 'JP'), got:  " + countryCode);
        }
        this.countryCode = countryCode;
        return this;
    }

    public MovieFilter withLanguage(String language) {
        this.language = language;
        return this;
    }

    public MovieFilter withActor(Movie.Person actor) {
        actors.add(actor);
        return this;
    }

    public MovieFilter withDirector(Movie.Person director) {
        directors.add(director);
        return this;
    }

    public MovieFilter includeWatched(boolean includeWatched) {
        this.includeWatched = includeWatched;
        return this;
    }

    // ----- Getters ------

    public Set<Integer> getGenreIds() {return genreIds;}
    public List<Movie.Person> getActors() {return actors;}
    public List<Movie.Person> getDirectors() {return directors;}
    public String getDecade() {return decade;}
    public String getCountryCode() {return countryCode;}
    public String getLanguage() {return language;}
    public boolean isIncludeWatched() {return includeWatched;}

    /* 
    - Convert this filter into TMDB API query parameters.
        Example output: {with_genres=28, primary_release_date.gte=1990-01-01}
    */
    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();

        if (!genreIds.isEmpty()) {
            params.put ("with_genres", genreIds.stream()
                .map(String::valueOf)
                .reduce((a,b) -> a + "," + b)
                .orElse(""));
        }

        if (decade != null) {
            String startYear = decade.replace("s", "");
            params.put("primary_release_date.gte", startYear + "-01-01");
            params.put("primary_release_date.lte", 
                (Integer.parseInt(startYear) + 9) + "-12-31");
        }

        if (countryCode != null) {
            params.put("with_origin_country", countryCode);
        }

        if (!actors.isEmpty() || !directors.isEmpty()) {
            List<Movie.Person> allPeople = new ArrayList<>();
            allPeople.addAll(actors);
            allPeople.addAll(directors);
            params.put("with_people", allPeople.stream()
                .map(p -> String.valueOf(p.id()))
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
        }

        params.put("language", language);
        params.put("sort_by", "vote_average.desc");
        params.put("vote_count.gte", "100");

        return params;

    }

}
