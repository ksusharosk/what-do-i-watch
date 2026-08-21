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
    public String build(TasteProfile profile, MovieFilter filter, int count, List<String> excludeTitles) {
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

        if(excludeTitles != null && !excludeTitles.isEmpty()) {
            prompt.append("\n== Do NOT recommend these (already suggested or seen) ==\n")
                  .append(String.join(", ", excludeTitles))
                  .append("\n");
        }
        
        prompt.append("\nRecommend up to ").append(count).append(" films. ");

        prompt.append("\nRespond with ONLY a JSON array and nothing else - ")
              .append("no explanation, no markdown, no code fences. ")
              .append("Each element must have exactly these fields:\n")
              .append("[\n")
              .append(" {\"title\": \"Movie Title\", \"year\": 2019, ")
              .append("\"pitch\": \"one sentence on why this user would enjoy it\"}\n")
              .append("]");

        return prompt.toString();

    }

    // Convenience overload: default count of 5, no exclusions
    public String build(TasteProfile profile, MovieFilter filter) {
        return build(profile, filter, 5, List.of());
    }

    /**
     * Builds a prompt for nostalgia/rewatch mode: the AI picks films the user
     * has already watched (preferrung ones they rated highly) that match the given mood and optional filters, for a comfort rewatch
     * 
     * @param lovedFilms films user rated highly
     * @param watchedFilms other films user has watched
     * @param mood free-text mood, e.g "cozy", "something intense"
     * @param filter optional genre/decade/etc. filters
     * @param count how many to recommend
     */
    public String buildNostalgia(List<String> lovedFilms,
                                 List<String> watchedFilms,
                                 String mood,
                                 MovieFilter filter,
                                 int count) {
        if (filter == null) {
            throw new IllegalArgumentException("MovieFilter cannot be null");
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a film curator helping someone pick a comforting rewatch. ")
              .append("Recommend films the user has ALREADY SEEN that fit their mood — ")
              .append("this is for revisiting old favourites, not discovering new films.\n\n");

        if (mood != null && !mood.isBlank()) {
            prompt.append("== Mood ==\n").append(mood).append("\n\n");
        }

        if (lovedFilms != null && !lovedFilms.isEmpty()) {
            prompt.append("== Films the user loved (prefer these) ==\n")
                  .append(String.join(", ", lovedFilms))
                  .append("\n\n");
        }
        if (watchedFilms != null && !watchedFilms.isEmpty()) {
            prompt.append("== Other films the user has watched ==\n")
                  .append(String.join(", ", watchedFilms))
                  .append("\n\n");
        }

        prompt.append("== Filters for this request ==\n")
              .append(describeFilter(filter))
              .append("\n");

        prompt.append("\nRecommend up to ").append(count).append(" films for a rewatch. ")
              .append("Choose ONLY from the films listed above (ones the user has seen). ")
              .append("Prefer the loved films, and match the mood.\n");

        prompt.append("\nRespond with ONLY a JSON array and nothing else — ")
              .append("no explanation, no markdown, no code fences. ")
              .append("Each element must have exactly these fields:\n")
              .append("[\n")
              .append("  {\"title\": \"Movie Title\", \"year\": 2019, ")
              .append("\"pitch\": \"one sentence on why to rewatch this now\"}\n")
              .append("]");

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
