
package com.payfabric.user.controller;

import com.payfabric.user.dto.UserResponse;
import com.payfabric.user.entity.User;
import com.payfabric.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal User authenticatedUser
    ) {

        UserResponse response = userService.getProfile(
                authenticatedUser.getId()
        );

        return ResponseEntity.ok(response);
    }
}
