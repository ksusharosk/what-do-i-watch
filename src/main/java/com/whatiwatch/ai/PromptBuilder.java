package com.whatiwatch.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.MovieFilter;
import com.whatiwatch.domain.user.TasteProfile;

/*
 - Builds a personalized prompt for the AI from a user's taste profile
  and the filters they've selected for this request.
*/
public final class PromptBuilder {
    /**
     - Builds the full prompt to send to an AI backend.
        @param profile the user's derived taste profile (loved/disliked/watched)
        @param filter  the filters chosen for this request (genre, decade, etc.)
        @return a complete prompt string
     */
    public String build(TasteProfile profile, MovieFilter filter) {
        if (profile == null) {
            throw new IllegalArgumentException("TasteProfile cannot be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("MovieFilter cannot be null");
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a film recommendation expert. ")
              .append("Recommend movies the user is likely to love, ")
              .append("based on their taste and the filters below. \n\n");
        
        prompt.append("== User taste ==\n")
              .append(profile.toPromptContext())
              .append("\n\n");

        prompt.append("== Filters for this request ==\n")
              .append(describeFilter(filter))
              .append("\n");
        
        prompt.append("\nRecommend up to 5 films. For each, give the title, ")
              .append("the release year, and one sentence explaining why this ")
              .append("user in particular would enjoy it. ");
        
        if(filter.isIncludeWatched()) {
            prompt.append("You may include films the user has already watched ")
                  .append("if they are a strong match.");
        } else {
            prompt.append("Do not recommend any film the user has already watched.");
        }

        return prompt.toString();

    }

    /*
     - Renders a MovieFilter as human-readable text for the AI.
    */
    private String describeFilter(MovieFilter filter) {
        List<String> parts = new ArrayList<>();

        if (!filter.getGenreIds().isEmpty()) {
            String genres = filter.getGenreIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            parts.add("Genre IDs: " + genres);
        }
        if (filter.getDecade() != null) {
            parts.add("Country: " + filter.getDecade());
        }
        if (filter.getCountryCode() != null) {
            parts.add("Country: " + filter.getCountryCode());
        }
        if (!filter.getActors().isEmpty()) {
            parts.add("Actors: " + joinNames(filter.getActors()));
        }
        if(!filter.getDirectors().isEmpty()) {
            parts.add("Directors: " + joinNames(filter.getDirectors()));
        }
        parts.add("Language: " + filter.getLanguage());

        return String.join("\n", parts);

    }

    private String joinNames(List<Movie.Person> people) {
        return people.stream()
            .map(Movie.Person::name)
            .collect(Collectors.joining(", "));
    }

}
