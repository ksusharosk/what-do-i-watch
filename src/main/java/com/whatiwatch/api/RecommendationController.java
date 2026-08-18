package com.whatiwatch.api;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.domain.MovieFilter;
import com.whatiwatch.domain.Recommendation;
import com.whatiwatch.domain.user.TasteProfile;
import com.whatiwatch.recommendation.RecommendationService;

/**
 * REST endpoint for movie recommendations
 * For now uses an empty taste profile
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public List<Recommendation> recommend(@RequestBody RecommendationRequest request) 
            throws AiUnavailableException {
        
        MovieFilter filter = buildFilter(request);
        String backend = (request.backend() != null && !request.backend().isBlank())
                ? request.backend()
                : "ollama"; //deefault backend
        
        // Empty profile for now, will be personalized later
        TasteProfile profile = TasteProfile.empty("anonymous");

        return recommendationService.recommend(profile, filter, backend);
    
    }

    // Translates the request DTO into a domain MovieFilter
    private MovieFilter buildFilter(RecommendationRequest request) {
        MovieFilter filter = new MovieFilter();

        if (request.genreIds() != null) {
            request.genreIds().forEach(filter::withGenre);
        }
        if (request.decade() != null && !request.decade().isBlank()) {
            filter.withDecade(request.decade());
        }
        if (request.country() != null && !request.country().isBlank()) {
            filter.withCountry(request.country());
        }
        if (request.language() != null && !request.language().isBlank()) {
            filter.withLanguage(request.language());
        }
        filter.includeWatched(request.includeWatched());

        return filter;
    }
    
}
