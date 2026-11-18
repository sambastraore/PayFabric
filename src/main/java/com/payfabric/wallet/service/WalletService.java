package com.payfabric.wallet.service;

import com.payfabric.ledger.entity.AccountRole;
import com.payfabric.ledger.entity.EntryType;
import com.payfabric.ledger.entity.LedgerAccount;
import com.payfabric.ledger.entity.LedgerTransactionStatus;
import com.payfabric.ledger.repository.LedgerAccountRepository;
import com.payfabric.ledger.repository.LedgerEntryRepository;
import com.payfabric.shared.exception.ResourceNotFoundException;
import com.payfabric.wallet.dto.WalletResponse;
import com.payfabric.wallet.entity.Wallet;
import com.payfabric.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(
            WalletRepository walletRepository,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository
    ) {
        this.walletRepository = walletRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        LedgerAccount account = ledgerAccountRepository
                .findByWalletId(wallet.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ledger account not found for wallet"));

        if (account.getAccountRole() != AccountRole.USER_WALLET) {
            throw new IllegalStateException(
                    "Invalid ledger account associated with wallet");
        }

        Long credits = ledgerEntryRepository
                .calculateTotalByAccountAndEntryType(
                        account.getId(),
                        EntryType.CREDIT,
                        LedgerTransactionStatus.POSTED
                );

        Long debits = ledgerEntryRepository
                .calculateTotalByAccountAndEntryType(
                        account.getId(),
                        EntryType.DEBIT,
                        LedgerTransactionStatus.POSTED
                );

        Long balance = credits - debits;

        return new WalletResponse(
                wallet.getId(),
                wallet.getCurrency().name(),
                wallet.getStatus().name(),
                balance
        );
    }
}