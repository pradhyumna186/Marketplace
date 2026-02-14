package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.Admin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    @Query("SELECT a FROM Admin a WHERE a.username = :identifier OR a.email = :identifier")
    Optional<Admin> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByEmail(String email);
}
