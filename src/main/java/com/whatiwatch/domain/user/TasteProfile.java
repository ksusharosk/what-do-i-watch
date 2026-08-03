package com.whatiwatch.domain.user;

import java.util.List;

import com.whatiwatch.domain.Movie;

/*
- Represents what we know about a user's movie taste
- Derived from their ratings and watchlist history
- Isn't stored by the user, built by TasteProfileService
- Gets fed into the AI prompt to personalise recommendations
*/
public record TasteProfile(
    String userId,
    List<String> likedGenres,
    List<String> dislikedGenres,
    List<Movie.Person> favouriteDirectors,
    List<Movie.Person> favouriteActors,
    List<String> preferredDecades,
    List<String> preferredCountries,
    List<String> alreadyWatchedTitles,
    List<String> highlyRatedTitles,
    List<String> poorlyRatedTitles,
    String aiSummary
) {

    /*
    - Returns an empty taste profile for a new user
    - Recommendations start generic
    */
   public static TasteProfile empty(String userId) {
        return new TasteProfile(
            userId, 
            List.of(), 
            List.of(), 
            List.of(),
            List.of(),
            List.of(), 
            List.of(), 
            List.of(), 
            List.of(), 
            List.of(), 
            null
        );
   }

   /*
   - Returns true if we have enough data to personalise recommendations
   - A new user with no rating gets generic recommendations
   */
  public boolean hasEnoughData() {
    return !highlyRatedTitles.isEmpty() || !poorlyRatedTitles.isEmpty();
  }

  /*
  - Builds a human readable summary of this taste profile to include in AI prompts

        Example output:
        "User loves: psychological thrillers, Korean cinema, 1990s films.
        User dislikes: romantic comedies, CGI blockbusters.
        Already watched: Parasite, Oldboy, Drive.
        Do not recommend these again."
  */
    public String toPromptContext() {
        if (!hasEnoughData()) {
            return "New user - no taste history yet. Recommend popular highly rated films";
        }

        StringBuilder sb = new StringBuilder();

        if (!likedGenres.isEmpty()) {
            sb.append("User enjoys:")
            .append(String.join(", ", likedGenres))
            .append(".\n");
        }

        if (!dislikedGenres.isEmpty()) {
            sb.append("User dislikes: ")
              .append(String.join(", ", dislikedGenres))
              .append(".\n");
        }

        if (!preferredDecades.isEmpty()) {
            sb.append("Preferred decades: ")
            .append(String.join(", ", preferredDecades))
            .append(".\n");
        }
        
        if (!preferredCountries.isEmpty()) {
            sb.append("Preferred countries: ")
            .append(String.join(", ", preferredCountries))
            .append(".\n");
        }    

        if (!favouriteDirectors.isEmpty()) {
            sb.append("Favourite directors: ")
            .append(favouriteDirectors.stream()
                .map(Movie.Person::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse(""))
            .append(".\n");
        }

        if (!favouriteActors.isEmpty()) {
            sb.append("Favourite actors: ")
            .append(favouriteActors.stream()
                .map(Movie.Person::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse(""))
            .append(".\n");
        }    

        if (!highlyRatedTitles.isEmpty()) {
            sb.append("Loved these films: ")
            .append(String.join(", ", highlyRatedTitles))
            .append(".\n");
        }

        if (!poorlyRatedTitles.isEmpty()) {
            sb.append("Disliked these films: ")
            .append(String.join(", ", poorlyRatedTitles))
            .append(".\n");
        }

        if (!alreadyWatchedTitles.isEmpty()) {
            sb.append("Already watched - do not recommend: ")
            .append(String.join(", ", alreadyWatchedTitles))
            .append(".\n");
        }

        if (aiSummary != null) {
            sb.append("Overall taste: ")
            .append(aiSummary);
        }

        return sb.toString();

    }

}
