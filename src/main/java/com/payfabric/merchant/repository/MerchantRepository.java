package com.payfabric.merchant.repository;

import com.payfabric.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByUserId(UUID userId);

    Optional<Merchant> findByMerchantCode(String merchantCode);

    boolean existsByMerchantCode(String merchantCode);
}