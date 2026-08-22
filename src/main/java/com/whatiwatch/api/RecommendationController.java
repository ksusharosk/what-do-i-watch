package com.whatiwatch.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.domain.MovieFilter;
import com.whatiwatch.domain.Recommendation;
import com.whatiwatch.domain.user.MovieRating;
import com.whatiwatch.domain.user.MovieRatingEntity;
import com.whatiwatch.domain.user.MovieRatingRepository;
import com.whatiwatch.domain.user.TasteProfile;
import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.WatchListEntry;
import com.whatiwatch.domain.user.WatchListEntryEntity;
import com.whatiwatch.domain.user.WatchListEntryRepository;
import com.whatiwatch.recommendation.AiBackendResolver;
import com.whatiwatch.recommendation.RecommendationService;
import com.whatiwatch.recommendation.TasteProfileService;
import com.whatiwatch.domain.user.UserService;
import com.whatiwatch.ai.AiBackend;

/**
 * REST endpoint for movie recommendations
 * For now uses an empty taste profile
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final TasteProfileService tasteProfileService;
    private final UserService userService;
    private final MovieRatingRepository ratingRepository;
    private final WatchListEntryRepository watchlistRepository;
    private final AiBackendResolver backendResolver;

    public RecommendationController(RecommendationService recommendationService,
                                    TasteProfileService tasteProfileService,
                                    UserService userService,
                                    MovieRatingRepository ratingRepository,
                                    WatchListEntryRepository watchlistRepository,
                                    AiBackendResolver backendResolver) {
        this.recommendationService = recommendationService;
        this.tasteProfileService = tasteProfileService;
        this.userService = userService;
        this.ratingRepository = ratingRepository;
        this.watchlistRepository = watchlistRepository;
        this.backendResolver = backendResolver;
    }

    @PostMapping
    public List<Recommendation> recommend(@RequestBody RecommendationRequest request,
                                        @AuthenticationPrincipal OidcUser oidcUser) 
            throws AiUnavailableException {
        
        MovieFilter filter = buildFilter(request);
        String backendName = (request.backend() != null && !request.backend().isBlank())
                ? request.backend()
                : "groq"; //deefault backend
    
        TasteProfile profile = buildProfileFor(oidcUser);
        Set<Integer> watchedIds = watchedMovieIdsFor(oidcUser);

        User currentUser = (oidcUser != null)
                ? userService.findByGoogleId(oidcUser.getSubject()).orElse(null)
                : null;
        AiBackend backend = backendResolver.resolve(currentUser, backendName);

        return recommendationService.recommend(profile, filter, backend, watchedIds);
    
    }

    @PostMapping("/nostalgia")
    public List<Recommendation> nostalgia(@RequestBody NostalgiaRequest request,
                                          @AuthenticationPrincipal OidcUser oidcUser)
            throws AiUnavailableException {

        // Login required — a guest has no watch history to draw from.
        User user = userService.requireUser(oidcUser);

        // Build the film pools from the user's history.
        List<MovieRating> ratings = ratingRepository.findByUserId(user.id()).stream()
                .map(MovieRatingEntity::toDomain)
                .toList();
        List<WatchListEntry> watchlist = watchlistRepository.findByUserId(user.id()).stream()
                .map(WatchListEntryEntity::toDomain)
                .toList();

        List<String> lovedFilms = ratings.stream()
                .filter(MovieRating::isPositive)
                .map(MovieRating::movieTitle)
                .distinct()
                .limit(30)
                .toList();

        List<String> watchedFilms = watchlist.stream()
                .filter(WatchListEntry::isWatched)
                .map(WatchListEntry::movieTitle)
                .distinct()
                .limit(30)
                .toList();

        MovieFilter filter = buildNostalgiaFilter(request);
        String backendName = (request.backend() != null && !request.backend().isBlank())
                ? request.backend()
                : "groq";
        
        AiBackend backend = backendResolver.resolve(user, backendName);

        return recommendationService.recommendNostalgia(
                lovedFilms, watchedFilms, request.mood(), filter, backend);
    }

    private MovieFilter buildNostalgiaFilter(NostalgiaRequest request) {
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
        return filter;
    }

    // Builds a real profile for a logged-in user, or an empty one for a guest
    private TasteProfile buildProfileFor(OidcUser oidcUser) {
        if(oidcUser == null) {
            return TasteProfile.empty("guest");
        }
        User user = userService.findByGoogleId(oidcUser.getSubject()).orElse(null);
        if (user == null) {
            return TasteProfile.empty("guest");
        }

        List<MovieRating> ratings = ratingRepository.findByUserId(user.id()).stream()
            .map(MovieRatingEntity::toDomain)
            .toList();
        List<WatchListEntry> watchlist = watchlistRepository.findByUserId(user.id()).stream()
            .map(WatchListEntryEntity::toDomain)
            .toList();
        
        return tasteProfileService.build(user.id(), ratings, watchlist);
    }

    /** TMDB ids of films this user has watched (empty for guests) */
    private Set<Integer> watchedMovieIdsFor(OidcUser oidcUser) {
        if (oidcUser == null) {
            return Set.of();
        }

        User user = userService.findByGoogleId(oidcUser.getSubject()).orElse(null);
        if (user == null) {
            return Set.of();
        }
        return watchlistRepository.findByUserId(user.id()).stream()
            .filter(e -> e.getStatus() == WatchListEntry.Status.WATCHED)
            .map(WatchListEntryEntity::getMovieId)
            .collect(Collectors.toSet());
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
