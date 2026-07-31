package com.whatiwatch.domain;

import java.util.List;

/* 
- Represents a movie retrieved from TMDB.
- Immutable by design - a movie's data doesn't change once loaded
*/
public record Movie(
    int id,
    String title,
    String originalTitle,
    String overview,
    int year,
    List<String> genres,
    String countryCode,
    String language,
    double rating,
    int voteCount,
    String posterPath,
    List<Person> directors,
    List<Person> actors
) {
    // Represents a person credited on a movie (director or actor)

    public record Person(
        int id,
        String name
    ) {}

    // Returns true if the movie has enough votes to be cosidered reliable
    public boolean hasReliableRating() {
        return voteCount >= 100;
    }

    /* Return a short display title with the year
        Example: "Parasite (2019)"
    */
   public String displayTitle() {
        return title + " (" + year + ") ";
   }

}