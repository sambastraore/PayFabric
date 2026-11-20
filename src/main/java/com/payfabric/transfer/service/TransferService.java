package com.payfabric.transfer.service;

import com.payfabric.ledger.entity.*;
import com.payfabric.ledger.repository.*;
import com.payfabric.shared.entity.Currency;
import com.payfabric.shared.exception.ResourceNotFoundException;
import com.payfabric.transfer.dto.TransferRequest;
import com.payfabric.transfer.dto.TransferResponse;
import com.payfabric.transfer.entity.Transfer;
import com.payfabric.transfer.repository.TransferRepository;
import com.payfabric.wallet.entity.Wallet;
import com.payfabric.wallet.entity.WalletStatus;
import com.payfabric.wallet.repository.WalletRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final TransferRepository transferRepository;

    public TransferService(
            WalletRepository walletRepository,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            LedgerTransactionRepository ledgerTransactionRepository,
            TransferRepository transferRepository
    ) {
        this.walletRepository = walletRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse transfer(
            UUID userId,
            TransferRequest request
    ) {

        // 1. Validation du montant
        if (request.amount() == null || request.amount() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Transfer amount must be positive"
            );
        }

        // 2. Récupération des portefeuilles
        Wallet sourceWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Source wallet not found"));

        Wallet destinationWallet = walletRepository.findById(
                request.destinationWalletId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Destination wallet not found"));

        // 3. Vérification des portefeuilles
        if (sourceWallet.getId().equals(destinationWallet.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Source and destination wallets must be different"
            );
        }

        if (sourceWallet.getStatus() != WalletStatus.ACTIVE
                || destinationWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Both wallets must be active"
            );
        }

        if (sourceWallet.getCurrency() != destinationWallet.getCurrency()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Wallet currencies do not match"
            );
        }

        // 4. Verrouillage des comptes dans un ordre déterministe
        LedgerAccount sourceAccount;
        LedgerAccount destinationAccount;

        if (sourceWallet.getId().compareTo(destinationWallet.getId()) < 0) {

            sourceAccount = ledgerAccountRepository
                    .findByWalletIdForUpdate(sourceWallet.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Source ledger account not found"));

            destinationAccount = ledgerAccountRepository
                    .findByWalletIdForUpdate(destinationWallet.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Destination ledger account not found"));

        } else {

            destinationAccount = ledgerAccountRepository
                    .findByWalletIdForUpdate(destinationWallet.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Destination ledger account not found"));

            sourceAccount = ledgerAccountRepository
                    .findByWalletIdForUpdate(sourceWallet.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Source ledger account not found"));
        }

        // 5. Vérification des comptes comptables
        validateAccount(sourceAccount, sourceWallet.getCurrency());
        validateAccount(destinationAccount, destinationWallet.getCurrency());

        // 6. Calcul du solde disponible
        Long credits = ledgerEntryRepository
                .calculateTotalByAccountAndEntryType(
                        sourceAccount.getId(),
                        EntryType.CREDIT,
                        LedgerTransactionStatus.POSTED
                );

        Long debits = ledgerEntryRepository
                .calculateTotalByAccountAndEntryType(
                        sourceAccount.getId(),
                        EntryType.DEBIT,
                        LedgerTransactionStatus.POSTED
                );

        long balance = Math.subtractExact(credits, debits);

        if (balance < request.amount()) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Insufficient funds"
            );
        }

        // 7. Création du transfert
        String transferReference = "TRF-" + UUID.randomUUID();

        Transfer transfer = new Transfer(
                transferReference,
                sourceWallet,
                destinationWallet,
                request.amount(),
                sourceWallet.getCurrency()
        );

        transfer.markAsProcessing();

        // 8. Création de la transaction comptable
        String ledgerReference = "LED-" + UUID.randomUUID();

        LedgerTransaction ledgerTransaction = new LedgerTransaction(
                ledgerReference,
                LedgerTransactionType.TRANSFER,
                sourceWallet.getCurrency(),
                "Transfer between wallets: " + transferReference
        );

        // 9. Création des deux écritures
        LedgerEntry debitEntry = new LedgerEntry(
                sourceAccount,
                EntryType.DEBIT,
                request.amount()
        );

        LedgerEntry creditEntry = new LedgerEntry(
                destinationAccount,
                EntryType.CREDIT,
                request.amount()
        );

        ledgerTransaction.addEntry(debitEntry);
        ledgerTransaction.addEntry(creditEntry);

        // 10. Vérification de l'équilibre comptable
        validateLedgerTransaction(ledgerTransaction);

        // 11. Comptabilisation
        ledgerTransaction.post();

        ledgerTransactionRepository.save(ledgerTransaction);

        // 12. Finalisation du transfert
        transfer.complete(ledgerTransaction);

        Transfer savedTransfer = transferRepository.save(transfer);

        return toResponse(savedTransfer);
    }

    private void validateAccount(
            LedgerAccount account,
            Currency walletCurrency
    ) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ledger account is not active"
            );
        }

        if (account.getAccountRole() != AccountRole.USER_WALLET) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Invalid ledger account role"
            );
        }

        if (account.getCurrency() != walletCurrency) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ledger account currency does not match wallet currency"
            );
        }
    }

    private void validateLedgerTransaction(
            LedgerTransaction transaction
    ) {
        long totalDebits = 0;
        long totalCredits = 0;

        if (transaction.getEntries().size() < 2) {
            throw new IllegalStateException(
                    "A ledger transaction must contain at least two entries"
            );
        }

        for (LedgerEntry entry : transaction.getEntries()) {

            if (entry.getAmount() == null || entry.getAmount() <= 0) {
                throw new IllegalStateException(
                        "Ledger entry amount must be positive"
                );
            }

            if (entry.getLedgerAccount().getCurrency()
                    != transaction.getCurrency()) {
                throw new IllegalStateException(
                        "Ledger entry currency does not match transaction currency"
                );
            }

            if (entry.getEntryType() == EntryType.DEBIT) {
                totalDebits = Math.addExact(totalDebits, entry.getAmount());
            } else if (entry.getEntryType() == EntryType.CREDIT) {
                totalCredits = Math.addExact(totalCredits, entry.getAmount());
            }
        }

        if (totalDebits != totalCredits) {
            throw new IllegalStateException(
                    "Ledger transaction is not balanced"
            );
        }
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getReference(),
                transfer.getSourceWallet().getId(),
                transfer.getDestinationWallet().getId(),
                transfer.getAmount(),
                transfer.getCurrency().name(),
                transfer.getStatus().name(),
                transfer.getCreatedAt(),
                transfer.getCompletedAt()
        );
    }
}