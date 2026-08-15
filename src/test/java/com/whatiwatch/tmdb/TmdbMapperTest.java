package com.whatiwatch.tmdb;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatiwatch.domain.Movie;

class TmdbMapperTest {

    private TmdbMapper mapper;
    private ObjectMapper json;

    @BeforeEach
    void setUp() throws Exception {

        json   = new ObjectMapper();
        
        JsonNode genreNode = json.readTree("""
            {
                "genres": [
                    { "id": 35, "name": "Comedy" },
                    { "id": 53, "name": "Thriller" },
                    { "id": 18, "name": "Drama" },
                    { "id": 28, "name": "Action" }
                ]
            }
            """);

        GenreCache testCache = new GenreCache(() -> genreNode);
        mapper = new TmdbMapper(testCache);
    }

    @Test
    void mapsBasicMovieFields() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 496243,
                "title": "Parasite",
                "original_title": "기생충",
                "overview": "A poor family schemes to become employed by a wealthy family.",
                "release_date": "2019-05-30",
                "vote_average": 8.5,
                "vote_count": 17000,
                "poster_path": "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
                "original_language": "ko",
                "genre_ids": [35, 53, 18]
            }
            """);

        Movie movie = mapper.toMovie(node);

        assertEquals(496243, movie.id());
        assertEquals("Parasite", movie.title());
        assertEquals("기생충", movie.originalTitle());
        assertEquals(2019, movie.year());
        assertEquals(8.5, movie.rating());
        assertEquals(17000, movie.voteCount());
        assertEquals("ko", movie.language());
        assertEquals(3, movie.genres().size());
        assertTrue(movie.genres().contains("Comedy"));
        assertTrue(movie.genres().contains("Thriller"));
        assertTrue(movie.genres().contains("Drama"));
    }

    @Test
    void parsesYearFromReleaseDate() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 1,
                "title": "Test",
                "original_title": "Test",
                "overview": "",
                "release_date": "1994-09-23",
                "vote_average": 9.3,
                "vote_count": 100,
                "poster_path": "",
                "original_language": "en",
                "genre_ids": []
            }
            """);

        Movie movie = mapper.toMovie(node);
        assertEquals(1994, movie.year());
    }

    @Test
    void handlesEmptyReleaseDate() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 1,
                "title": "Test",
                "original_title": "Test",
                "overview": "",
                "release_date": "",
                "vote_average": 0,
                "vote_count": 0,
                "poster_path": "",
                "original_language": "en",
                "genre_ids": []
            }
            """);

        Movie movie = mapper.toMovie(node);
        assertEquals(0, movie.year());
    }

    @Test
    void mapsEmptyResultsToEmptyList() throws Exception {
        JsonNode emptyArray = json.readTree("[]");
        List<Movie> movies = mapper.toMovies(emptyArray);
        assertTrue(movies.isEmpty());
    }

    @Test
    void mapsMultipleMovies() throws Exception {
        JsonNode results = json.readTree("""
            [
                {
                    "id": 1,
                    "title": "Movie One",
                    "original_title": "Movie One",
                    "overview": "",
                    "release_date": "2020-01-01",
                    "vote_average": 7.0,
                    "vote_count": 100,
                    "poster_path": "",
                    "original_language": "en",
                    "genre_ids": []
                },
                {
                    "id": 2,
                    "title": "Movie Two",
                    "original_title": "Movie Two",
                    "overview": "",
                    "release_date": "2021-01-01",
                    "vote_average": 8.0,
                    "vote_count": 200,
                    "poster_path": "",
                    "original_language": "fr",
                    "genre_ids": [28]
                }
            ]
            """);

        List<Movie> movies = mapper.toMovies(results);
        assertEquals(2, movies.size());
        assertEquals("Movie One", movies.get(0).title());
        assertEquals("Movie Two", movies.get(1).title());
    }

    @Test
    void mapsPersonSearchResults() throws Exception {
        JsonNode results = json.readTree("""
            [
                {"id": 138, "name": "Quentin Tarantino"},
                {"id": 287, "name": "Brad Pitt"}
            ]
            """);

        List<Movie.Person> persons = mapper.toPersons(results);
        assertEquals(2, persons.size());
        assertEquals("Quentin Tarantino", persons.get(0).name());
        assertEquals(138, persons.get(0).id());
    }

    @Test
    void handlesNullResults() {
        List<Movie> movies = mapper.toMovies(null);
        assertTrue(movies.isEmpty());

        List<Movie.Person> persons = mapper.toPersons(null);
        assertTrue(persons.isEmpty());
    }

    @Test
    void toFullMovieMapsGenresFromObjects() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 550,
                "title": "Fight Club",
                "original_title": "Fight Club",
                "overview": "",
                "release_date": "1999-10-15",
                "vote_average": 8.4,
                "vote_count": 27000,
                "poster_path": "",
                "original_language": "en",
                "genres": [ { "id": 18, "name": "Drama" }, { "id": 53, "name": "Thriller" } ],
                "production_countries": [ { "iso_3166_1": "US", "name": "United States" } ],
                "credits": { "cast": [], "crew": [] }
            }
            """);

        Movie movie = mapper.toFullMovie(node);

        assertEquals("Fight Club", movie.title());
        assertEquals(1999, movie.year());
        assertEquals(2, movie.genres().size());
        assertTrue(movie.genres().contains("Drama"));
        assertTrue(movie.genres().contains("Thriller"));
    }

    @Test
    void toFullMovieMapsPrimaryCountry() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 1, "title": "T", "original_title": "T", "overview": "",
                "release_date": "2020-01-01", "vote_average": 7.0, "vote_count": 100,
                "poster_path": "", "original_language": "en",
                "genres": [],
                "production_countries": [
                    { "iso_3166_1": "KR", "name": "South Korea" },
                    { "iso_3166_1": "US", "name": "United States" }
                ],
                "credits": { "cast": [], "crew": [] }
            }
            """);

        Movie movie = mapper.toFullMovie(node);

        assertEquals("KR", movie.countryCode());  // first country wins
    }

    @Test
    void toFullMovieHandlesMissingCountry() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 1, "title": "T", "original_title": "T", "overview": "",
                "release_date": "2020-01-01", "vote_average": 7.0, "vote_count": 100,
                "poster_path": "", "original_language": "en",
                "genres": [],
                "production_countries": [],
                "credits": { "cast": [], "crew": [] }
            }
            """);

        Movie movie = mapper.toFullMovie(node);

        assertNull(movie.countryCode());
    }

    @Test
    void toFullMovieExtractsDirectorsFromCrew() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 1, "title": "T", "original_title": "T", "overview": "",
                "release_date": "2020-01-01", "vote_average": 7.0, "vote_count": 100,
                "poster_path": "", "original_language": "en",
                "genres": [],
                "production_countries": [],
                "credits": {
                    "cast": [],
                    "crew": [
                        { "id": 7467, "name": "David Fincher", "job": "Director" },
                        { "id": 7469, "name": "Jim Uhls", "job": "Screenplay" },
                        { "id": 1234, "name": "Second Director", "job": "Director" }
                    ]
                }
            }
            """);

        Movie movie = mapper.toFullMovie(node);

        // Only crew with job == "Director", writers excluded
        assertEquals(2, movie.directors().size());
        assertEquals("David Fincher", movie.directors().get(0).name());
        assertEquals(7467, movie.directors().get(0).id());
        assertTrue(movie.directors().stream().noneMatch(p -> p.name().equals("Jim Uhls")));
    }

    @Test
    void toFullMovieCapsActorsAtTen() throws Exception {
        // Build a cast of 12 — expect only the first 10 kept
        StringBuilder cast = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0) cast.append(",");
            cast.append("{ \"id\": ").append(i)
                .append(", \"name\": \"Actor ").append(i)
                .append("\", \"order\": ").append(i).append(" }");
        }

        JsonNode node = json.readTree("""
            {
                "id": 1, "title": "T", "original_title": "T", "overview": "",
                "release_date": "2020-01-01", "vote_average": 7.0, "vote_count": 100,
                "poster_path": "", "original_language": "en",
                "genres": [],
                "production_countries": [],
                "credits": {
                    "cast": [ %s ],
                    "crew": []
                }
            }
            """.formatted(cast.toString()));

        Movie movie = mapper.toFullMovie(node);

        assertEquals(10, movie.actors().size());
        // Kept in billing order: first is Actor 0, last is Actor 9
        assertEquals("Actor 0", movie.actors().get(0).name());
        assertEquals("Actor 9", movie.actors().get(9).name());
    }

    @Test
    void toFullMovieHandlesEmptyCredits() throws Exception {
        JsonNode node = json.readTree("""
            {
                "id": 1, "title": "T", "original_title": "T", "overview": "",
                "release_date": "2020-01-01", "vote_average": 7.0, "vote_count": 100,
                "poster_path": "", "original_language": "en",
                "genres": [],
                "production_countries": [],
                "credits": { "cast": [], "crew": [] }
            }
            """);

        Movie movie = mapper.toFullMovie(node);

        assertTrue(movie.directors().isEmpty());
        assertTrue(movie.actors().isEmpty());
    }
}