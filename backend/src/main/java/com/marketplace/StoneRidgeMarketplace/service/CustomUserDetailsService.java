package com.marketplace.StoneRidgeMarketplace.service;

import com.marketplace.StoneRidgeMarketplace.entity.Admin;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.repository.AdminRepository;
import com.marketplace.StoneRidgeMarketplace.repository.UserRepository;
import com.marketplace.StoneRidgeMarketplace.security.AdminPrincipal;
import com.marketplace.StoneRidgeMarketplace.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null && username.startsWith(AdminPrincipal.ADMIN_PREFIX)) {
            String adminUsername = username.substring(AdminPrincipal.ADMIN_PREFIX.length());
            Admin admin = adminRepository.findByUsername(adminUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + adminUsername));
            return AdminPrincipal.create(admin);
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserPrincipal.create(user);
    }
}
