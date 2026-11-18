
package com.payfabric.user.service;

import com.payfabric.ledger.entity.AccountRole;
import com.payfabric.ledger.entity.AccountType;
import com.payfabric.ledger.entity.LedgerAccount;
import com.payfabric.ledger.repository.LedgerAccountRepository;
import com.payfabric.shared.entity.Currency;
import com.payfabric.shared.exception.ResourceAlreadyExistsException;
import com.payfabric.user.dto.RegisterRequest;
import com.payfabric.user.dto.UserResponse;
import com.payfabric.user.entity.User;
import com.payfabric.user.repository.UserRepository;
import com.payfabric.wallet.entity.Wallet;
import com.payfabric.wallet.repository.WalletRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            LedgerAccountRepository ledgerAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        // 1. Normalize user input
        String phoneNumber = request.phoneNumber().trim();
        String email = normalizeEmail(request.email());

        // 2. Check for existing user
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ResourceAlreadyExistsException(
                    "Phone number is already registered"
            );
        }

        if (email != null && userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException(
                    "Email address is already registered"
            );
        }

        // 3. Hash password
        String passwordHash = passwordEncoder.encode(request.password());

        // 4. Create User
        User user = new User(
                request.firstName().trim(),
                request.lastName().trim(),
                phoneNumber,
                email,
                passwordHash
        );

        userRepository.save(user);

        // 5. Create Wallet
        Wallet wallet = new Wallet(user);
        user.assignWallet(wallet);

        walletRepository.save(wallet);

        // 6. Create Ledger Account
        String accountCode = generateAccountCode();

        LedgerAccount ledgerAccount = new LedgerAccount(
                wallet,
                accountCode,
                AccountType.LIABILITY,
                AccountRole.USER_WALLET,
                Currency.XOF
        );

        ledgerAccountRepository.save(ledgerAccount);

        // 7. Return safe response
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getStatus().name()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateAccountCode() {
        return "WAL-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase(Locale.ROOT);
    }
}
