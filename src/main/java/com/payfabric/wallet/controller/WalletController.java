package com.payfabric.wallet.controller;

import com.payfabric.user.entity.User;
import com.payfabric.wallet.dto.WalletResponse;
import com.payfabric.wallet.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        WalletResponse response = walletService.getWallet(
                authenticatedUser.getId()
        );

        return ResponseEntity.ok(response);
    }
}