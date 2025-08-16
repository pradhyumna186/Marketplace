package com.marketplace.StoneRidgeMarketplace.repository;

import com.marketplace.StoneRidgeMarketplace.entity.TrustedDevice;
import com.marketplace.StoneRidgeMarketplace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    @Query("SELECT td FROM TrustedDevice td WHERE td.user = :user AND td.deviceFingerprint = :fingerprint AND td.active = true")
    Optional<TrustedDevice> findByUserAndDeviceFingerprintAndActiveTrue(@Param("user") User user,
            @Param("fingerprint") String fingerprint);

    @Query("SELECT td FROM TrustedDevice td WHERE td.user = :user")
    List<TrustedDevice> findByUser(@Param("user") User user);
}
