package com.payfabric.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank
        @Pattern(regexp = "^\\+[1-9][0-9]{1,14}$")
        String phoneNumber,

        @NotBlank
        @Size(max = 72)
        String password

) {}