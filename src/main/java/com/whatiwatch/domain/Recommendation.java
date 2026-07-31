package com.whatiwatch.domain;

/*
- Represents a movie recommendation - a movie paired with an AI-generated pitch.
- Also tracks user feedback so recommendations can improve over time.
*/
public record Recommendation(
    Movie movie,
    String aiPitch,
    Feedback feedback
) {

    // Represents how the user reacted to this recommendation.
    public enum Feedback {
        NONE, // user hasn't reacted yet
        LIKED, // user added to watchlist or rated highly
        DISLIKED, // user dismissed it
        WATCHED, // user has already seen it
    }

    /*
    - Creates a fresh recommendation with no feedback yet.
    - This is what gets returned when AI first suggests a movie
    */
   public static Recommendation fresh(Movie movie, String aiPitch) {
        return new Recommendation(movie, aiPitch, Feedback.NONE);
   }

   /*
   - Returns a copy of this recommendation with updated feedback
   */
   public Recommendation withFeedback(Feedback feedback) {
        return new Recommendation(movie, aiPitch, feedback);
   }

   /* 
   - Return true if the user has interacted with this recommendation
   */
   public boolean hasInteraction() {
        return feedback != Feedback.NONE;
   }


}
