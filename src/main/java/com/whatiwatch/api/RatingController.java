package com.whatiwatch.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whatiwatch.domain.user.MovieRating;
import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.RatingService;
import com.whatiwatch.domain.user.UserService;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    public RatingController(RatingService ratingService, UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    /** List the logged-in user's ratings. */
    @GetMapping
    public List<MovieRating> myRatings(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        return ratingService.getRatings(user.id());
    }

    /** Rate a movie (creates or updates). */
    @PostMapping
    public MovieRating rate(@RequestBody RatingRequest request,
                            @AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        return ratingService.rate(user.id(), request.movieId(), request.movieTitle(),
                request.rating(), request.review());
    }

    /** Remove a rating. */
    @DeleteMapping("/{movieId}")
    public void deleteRating(@PathVariable int movieId,
                             @AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        ratingService.deleteRating(user.id(), movieId);
    }
}