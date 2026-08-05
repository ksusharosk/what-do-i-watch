package com.whatiwatch.tmdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.whatiwatch.domain.Movie;

/*
- Maps raw TMDB JSON responses into domain objects
- Keeps all JSON parsing logic in one place
*/
public class TmdbMapper {

    // Maps a JSON array of TMDB movie results into a list of Movie objects
    public List<Movie> toMovies(JsonNode results) {
        if (results == null || !results.isArray()) {
            return Collections.emptyList();
        }

        List<Movie> movies = new ArrayList<>();
        for (JsonNode node : results) {
            movies.add(toMovie(node));
        }

        return movies;
    }

    // Maps a single TMDB movie JSON node into a Movie domain object
    public Movie toMovie(JsonNode node) {
        int id = node.get("id").asInt();
        String title = node.get("title").asText("");
        String originalTitle = node.get("original_title").asText("");
        String overview = node.get("overview").asText("");
        String releaseDate = node.get("release_date").asText("");
        double rating = node.get("vote_average").asDouble(0);
        int voteCount = node.get("vote_count").asInt(0);
        String posterPath = node.get("poster_path").asText("");
        String originalLang = node.get("original_language").asText("en");

        int year = parseYear(releaseDate);
        List<String> genres = toGenreNames(node.get("genre_ids"));

        return new Movie(
            id,
            title,
            originalTitle,
            overview,
            year,
            genres,
            null, // countryCode fetched separately
            originalLang,
            rating,
            voteCount,
            posterPath,
            List.of(), // directord fetched separately
            List.of()  // actors fetched separately
        );
    }

    // Maps a JSON array of TMDB person results into a list of Person objects
    public List<Movie.Person> toPersons(JsonNode results) {
        if (results == null || !results.isArray()) {
            return Collections.emptyList();
        }

        List<Movie.Person> persons = new ArrayList<>();
        for (JsonNode node : results) {
            int id = node.get("id").asInt();
            String name = node.get("name").asText("");
            persons.add(new Movie.Person(id, name));
        }
        return persons;
    }

    // Extracts the year from a TMDB release date string (format: "1999-03-31"), returns 0 if the date is missing/unparseable
    private int parseYear(String releaseDate) {
        if(releaseDate == null | releaseDate.length() < 4) {
            return 0;
        }

        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Converts a JSON array of genre IDs into the genre name strings
    private List<String> toGenreNames(JsonNode genreIds) {
        if (genreIds == null || !genreIds.isArray()) {
            return Collections.emptyList();
        }    

        List<String> genres = new ArrayList<>();
        for (JsonNode id : genreIds) {
            genres.add(String.valueOf(id.asInt()));
        }
        
        return genres;
    }

}
