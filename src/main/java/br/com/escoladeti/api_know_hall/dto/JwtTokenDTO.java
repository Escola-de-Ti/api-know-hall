package br.com.escoladeti.api_know_hall.dto;

public record JwtTokenDTO( String access_token, String token_type, Long expires_in ) {
}
