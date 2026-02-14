package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.User;
import com.marketplace.StoneRidgeMarketplace.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByRole(Role role);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY u.createdAt DESC")
    Page<User> searchByEmailOrUsername(@Param("q") String q, Pageable pageable);

    Optional<User> findByEmailVerificationToken(String token);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email OR u.username = :username")
    boolean existsByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
}
