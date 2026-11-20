package com.payfabric.transfer.controller;

import com.payfabric.transfer.dto.TransferRequest;
import com.payfabric.transfer.dto.TransferResponse;
import com.payfabric.transfer.service.TransferService;
import com.payfabric.user.entity.User;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody TransferRequest request
    ) {

        TransferResponse response = transferService.transfer(
                authenticatedUser.getId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}