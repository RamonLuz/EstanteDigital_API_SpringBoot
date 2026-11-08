package com.estantedigital.dto;

public record TokenResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
