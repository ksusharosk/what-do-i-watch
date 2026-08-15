package com.whatiwatch.tmdb;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}