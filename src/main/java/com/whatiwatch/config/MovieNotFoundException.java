package com.whatiwatch.config;

// Thrown when no movies are found for the given criteria
public class MovieNotFoundException extends AppException {
        public MovieNotFoundException() {
            super("No movies found for the selected filters. Try broadening your search. ");
        }
}
