package com.whatiwatch.config;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import com.whatiwatch.domain.user.UserService;

/**
 * Runs during Google (OIDC) login: after Google returns the profile, ensures a
 * corresponding user exists in our database (find-or-create).
 *
 * Because the openid scope is requested, Google login is OIDC, so this
 * implements the OIDC user service.
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String googleId = oidcUser.getAttribute("sub");
        String email = oidcUser.getAttribute("email");
        String displayName = oidcUser.getAttribute("name");

        userService.findOrCreate(googleId, email, displayName);

        return oidcUser;
    }
}
