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

import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.WatchListEntry;
import com.whatiwatch.domain.user.UserService;
import com.whatiwatch.domain.user.WatchListService;

@RestController
@RequestMapping("/api/watchlist")
public class WatchListController {

    private final WatchListService watchListService;
    private final UserService userService;

    public WatchListController(WatchListService watchListService, UserService userService) {
        this.watchListService = watchListService;
        this.userService = userService;
    }

    /** List the logged-in user's watchlist. */
    @GetMapping
    public List<WatchListEntry> myWatchList(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        return watchListService.getWatchList(user.id());
    }

    /**
     * Add a movie to the watchlist or update its status.
     * Passing status "WATCHED" flags it as watched — this is what powers
     * "mark watched" straight from a recommendation.
     */
    @PostMapping
    public WatchListEntry addOrUpdate(@RequestBody WatchListRequest request,
                                      @AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        return watchListService.addOrUpdate(user.id(), request.movieId(),
                request.movieTitle(), request.status());
    }

    /** Remove a movie from the watchlist. */
    @DeleteMapping("/{movieId}")
    public void remove(@PathVariable int movieId,
                       @AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        watchListService.remove(user.id(), movieId);
    }
}