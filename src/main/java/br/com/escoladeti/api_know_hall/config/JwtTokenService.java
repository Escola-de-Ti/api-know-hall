package br.com.escoladeti.api_know_hall.config;

import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class JwtTokenService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  private static final String ISSUER = "api-know-hall";

  public JwtTokenDTO generateTokenWithExpiration(String user) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      Instant expiresAt = expirationDate();
      String token = JWT.create()
        .withIssuer(ISSUER)
        .withIssuedAt(creationDate())
        .withExpiresAt(expiresAt)
        .withSubject(user)
        .sign(algorithm);
      return new JwtTokenDTO(token, "Bearer", expiresAt.toEpochMilli());
    } catch (JWTCreationException exception) {
      throw new JWTCreationException("Erro ao gerar token.", exception);
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }


  public String getSubjectFromToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      return JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()
        .verify(token)
        .getSubject();
    } catch (JWTVerificationException exception) {
      throw new JWTVerificationException("Token inválido ou expirado.");
    } catch (RuntimeException e) {
      throw new RuntimeException();
    }
  }

  private Instant creationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
  }

  private Instant expirationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusHours(1).toInstant();
  }

}
