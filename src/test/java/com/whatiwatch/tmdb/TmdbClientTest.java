package com.whatiwatch.tmdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.whatiwatch.domain.Movie;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.util.List;

class TmdbClientTest {

    private MockWebServer server;
    private TmdbClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        // Point the client at the mock server instead of real TMDB
        client = new TmdbClient("test-token", server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void searchMovieSendsQueryAndYear() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "results": [
                            { "id": 496243, "title": "Parasite", "original_title": "기생충",
                              "overview": "", "release_date": "2019-05-30",
                              "vote_average": 8.5, "vote_count": 17000, "poster_path": "/p.jpg",
                              "original_language": "ko", "genre_ids": [] }
                        ] }
                        """)
                .addHeader("Content-Type", "application/json"));

        List<Movie> results = client.searchMovie("Parasite", 2019);

        assertEquals(1, results.size());
        assertEquals("Parasite", results.get(0).title());
        assertEquals(2019, results.get(0).year());

        RecordedRequest recorded = server.takeRequest();
        assertTrue(recorded.getPath().contains("/search/movie"));
        assertTrue(recorded.getPath().contains("query=Parasite"));
        assertTrue(recorded.getPath().contains("year=2019"));
    }

    @Test
    void searchMovieOmitsYearWhenZero() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "results": [] }
                        """));

        client.searchMovie("Parasite", 0);

        RecordedRequest recorded = server.takeRequest();
        assertTrue(recorded.getPath().contains("query=Parasite"));
        // No year param when the year is 0
        assertTrue(!recorded.getPath().contains("year="));
    }

    @Test
    void searchMovieSendsAuthHeader() throws Exception {
        server.enqueue(new MockResponse().setBody("{ \"results\": [] }"));

        client.searchMovie("Drive", 2011);

        RecordedRequest recorded = server.takeRequest();
        assertEquals("Bearer test-token", recorded.getHeader("Authorization"));
    }
}