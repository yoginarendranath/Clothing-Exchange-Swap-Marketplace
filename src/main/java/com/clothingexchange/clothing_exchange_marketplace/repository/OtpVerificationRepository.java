package com.clothingexchange.clothing_exchange_marketplace.repository;

import com.clothingexchange.clothing_exchange_marketplace.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailOrderByIdDesc(String email);

    @Transactional
    void deleteByEmail(String email);
}