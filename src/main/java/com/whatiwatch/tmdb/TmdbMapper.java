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

    private final GenreCache genreCache;

    public TmdbMapper(GenreCache genreCache) {
        if (genreCache == null) {
            throw new IllegalArgumentException("genreCache cannot be null");
        }
        this.genreCache = genreCache;
    }

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

    /*
     - Maps a TMDB movie detailed response (from /movie/{id}?append_to_response=credits)
     into a filly-populated Movie object.

     - The detail endpoint differs from the discover list: 
      - genres come as {id, name} objects
      - country is in production_countries
      - cast/crew are nested under credits
    */
    public Movie toFullMovie(JsonNode node) {
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
        List<String> genres = toGenreNamesFromObjects(node.get("genres"));
        String countryCode = toPrimaryCountry(node.get("production_countries"));

        JsonNode credits = node.get("credits");
        List<Movie.Person> directors = toDirectors(credits);
        List<Movie.Person> actors = toTopActors(credits, 10);

        return new Movie(
            id, title, originalTitle, overview, year,
            genres, countryCode, originalLang, rating,
            voteCount, posterPath, directors, actors
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

    // Maps a JSON array of TMDB person results into a list of directors
    private List<Movie.Person> toDirectors(JsonNode credits) {
        if (credits == null) {
            return Collections.emptyList();
        }
        JsonNode crew = credits.path("crew");
        if (!crew.isArray()) {
            return Collections.emptyList();
        }
        List<Movie.Person> directors = new ArrayList<>();
        for (JsonNode member : crew) {
            if ("Director".equals(member.path("job").asText(""))) {
                directors.add(new Movie.Person(
                    member.path("id").asInt(),
                    member.path("name").asText("")));
            }
        }
        return directors;
    }

    // Maps a JSON array of TMDB person results into a list of actors with a top <limit>.
    private List<Movie.Person> toTopActors(JsonNode credits, int limit) {
        if (credits == null) {
            return Collections.emptyList();
        }
        JsonNode cast = credits.path("cast");
        if (!cast.isArray()) {
            return Collections.emptyList();
        }
        List<Movie.Person> actors = new ArrayList<>();
        for (JsonNode member : cast) {
            if (actors.size() >= limit) {
                break;
            }
            actors.add(new Movie.Person(
            member.path("id").asInt(), 
            member.path("name").asText("")
        ));
        }
        return actors;
        
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
            genres.add(genreCache.nameFor(id.asInt()));
        }
        
        return genres;
    }

    // Detail-endpoint genres: [{ "id": 18, "name": "Drama" }] -> ["Drama"]
    private List<String> toGenreNamesFromObjects(JsonNode genres) {
        if (genres == null || !genres.isArray()) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (JsonNode g : genres) {
            String name = g.path("name").asText();
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    // Returns first producation country's ISO code, e.g "US", null if none
    private String toPrimaryCountry(JsonNode countries) {
        if (countries == null || !countries.isArray() || countries.isEmpty()) {
            return  null;
        }
        return countries.get(0).path("iso_3166_1").asText(null);
    }

}
