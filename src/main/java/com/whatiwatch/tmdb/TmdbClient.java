package com.whatiwatch.tmdb;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatiwatch.config.AppException;
import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.MovieFilter;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
- Client for the TMDB (The Movie Database) API
- Handles all HTTP communication with TMDB
- Converts raw JSON responses into the domain objects via TmdbMapper
*/
public class TmdbClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    private final String apiToken;
    private final OkHttpClient http;
    private final ObjectMapper json;
    private final TmdbMapper mapper;

    public TmdbClient(String apiToken) {
        this.apiToken = apiToken;
        this.http = new OkHttpClient();
        this.json = new ObjectMapper();

        GenreCache genreCache = new GenreCache(this::fetchGenres);
        this.mapper = new TmdbMapper(genreCache);
    }

    /*
    - Discovers movies based on the given filter
    - Maps to TMDB's /discover/movie endpoint
    */
   public List<Movie> discover(MovieFilter filter) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/discover/movie")
            .newBuilder();
        
        for (Map.Entry<String, String> param : filter.toQueryParams().entrySet()) {
            urlBuilder.addQueryParameter(param.getKey(), param.getValue());
        }

        JsonNode response = get(urlBuilder.build().toString());
        return mapper.toMovies(response.get("results"));
   }

   /*
   - Searches for a person (actor/director) by name
   - Maps to TMDB's endpoint /search/person
   */
   public List<Movie.Person> searchPerson(String query) {
        String url = HttpUrl.parse(BASE_URL + "/search/person")
            .newBuilder()
            .addQueryParameter("query", query)
            .build()
            .toString();
       
        JsonNode response = get(url);
        return mapper.toPersons(response.get("results"));
   }

   /*
   - Fetcheds the full list of movie genres from TMDB
   - Should be cached
   */
   public JsonNode fetchGenres() {
        return get(BASE_URL + "/genre/movie/list");
   }

   /*
    - Makes a GET request to the given URL
    - Adds the Authorization header authomatically
    - Throws AppException on any HTTP or network error 
   */
   private JsonNode get(String url) {
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + apiToken)
            .header("Accept", "application/json")
            .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AppException(
                  "TMDB API error: " + response.code() + " " + response.message()  
                );
            }
            return json.readTree(response.body().string());
        } catch (IOException e) {
            throw new AppException("Failed to connect to TMDB: " + e.getMessage(), e);
        }
   }

   /*
    - Fetches a single movie's full details by ID, incl credits
    - Maps to TMDB's /movie/{id} endpoint with append_to_response=credits
   */
   public Movie getMovie(int id) {
        String url = HttpUrl.parse(BASE_URL + "/movie/" + id)
            .newBuilder()
            .addQueryParameter("append_to_response", "credits")
            .build()
            .toString();
        
        JsonNode response = get(url);
        return mapper.toFullMovie(response);
   }

}
