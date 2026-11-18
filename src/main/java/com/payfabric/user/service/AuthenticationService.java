
package com.payfabric.user.service;

import com.payfabric.config.JwtService;
import com.payfabric.user.dto.AuthResponse;
import com.payfabric.user.dto.LoginRequest;
import com.payfabric.user.entity.User;
import com.payfabric.user.entity.UserStatus;
import com.payfabric.user.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String phoneNumber = request.phoneNumber().trim();

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid credentials")
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("User account is not active");
        }

        String token = jwtService.generateToken(user.getId());

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpiration() / 1000
        );
    }
}
