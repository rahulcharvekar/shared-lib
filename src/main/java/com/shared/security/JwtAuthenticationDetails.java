package com.shared.security;

import com.shared.security.client.TokenIntrospectionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class JwtAuthenticationDetails extends WebAuthenticationDetails {

    private final TokenIntrospectionResponse tokenDetails;

    public JwtAuthenticationDetails(HttpServletRequest request, TokenIntrospectionResponse tokenDetails) {
        super(request);
        this.tokenDetails = tokenDetails;
    }

    public TokenIntrospectionResponse getTokenDetails() {
        return tokenDetails;
    }

    public Long getUserId() {
        return tokenDetails.getUserId();
    }

    public Integer getPermissionVersion() {
        return tokenDetails.getPermissionVersion();
    }

    public String getTokenId() {
        return tokenDetails.getTokenId();
    }
}
