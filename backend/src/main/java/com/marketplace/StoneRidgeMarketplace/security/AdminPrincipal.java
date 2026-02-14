package com.marketplace.StoneRidgeMarketplace.security;

import com.marketplace.StoneRidgeMarketplace.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Principal for admin authentication. Username is stored as "admin:&lt;username&gt;"
 * so the UserDetailsService can route to the admins table.
 */
@AllArgsConstructor
@Getter
public class AdminPrincipal implements UserDetails, PrincipalWithId {

    private Long id;
    private String username;  // full "admin:xxx" for JWT validation
    private String email;
    private String password;
    private boolean enabled;

    public static final String ADMIN_PREFIX = "admin:";

    public static AdminPrincipal create(Admin admin) {
        return new AdminPrincipal(
                admin.getId(),
                ADMIN_PREFIX + admin.getUsername(),
                admin.getEmail(),
                admin.getPassword(),
                admin.isEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
