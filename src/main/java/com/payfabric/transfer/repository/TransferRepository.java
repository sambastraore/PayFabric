package com.payfabric.transfer.repository;

import com.payfabric.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository
        extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByReference(String reference);

    boolean existsByReference(String reference);
}