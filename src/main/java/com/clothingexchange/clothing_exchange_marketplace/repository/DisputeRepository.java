package com.clothingexchange.clothing_exchange_marketplace.repository;

import com.clothingexchange.clothing_exchange_marketplace.model.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeRepository
        extends JpaRepository<Dispute, Long> {

    List<Dispute> findByReporterId(Long reporterId);

    List<Dispute> findByStatus(String status);

    long countByStatus(String status);
}