package com.marketplace.StoneRidgeMarketplace.security;

/**
 * Common interface for principals that have an id (UserPrincipal and AdminPrincipal).
 * Allows admin endpoints to accept either without casting.
 */
public interface PrincipalWithId {
    Long getId();
}
