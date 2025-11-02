package br.com.escoladeti.api_know_hall.config;

import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class JwtTokenService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  private static final String ISSUER = "api-know-hall";

  public JwtTokenDTO generateTokenWithExpiration(String user) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

      Instant accessExpiresAt = accessExpirationDate();
      Instant refreshExpiresAt = refreshExpirationDate();
      Date issuedAt = Date.from(creationDate());

      String accessToken = JWT.create()
        .withIssuer(ISSUER)
        .withIssuedAt(issuedAt)
        .withExpiresAt(Date.from(accessExpiresAt))
        .withSubject(user)
        .withClaim("typ", "access")
        .sign(algorithm);

      String refreshToken = JWT.create()
        .withIssuer(ISSUER)
        .withIssuedAt(issuedAt)
        .withExpiresAt(Date.from(refreshExpiresAt))
        .withSubject(user)
        .withClaim("typ", "refresh")
        .sign(algorithm);

      return new JwtTokenDTO(accessToken, "Bearer", accessExpiresAt.toEpochMilli(), refreshToken);
    } catch (JWTCreationException exception) {
      throw new JWTCreationException("Erro ao gerar token.", exception);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public JwtTokenDTO refreshTokens(String refreshToken) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      JWTVerifier verifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build();

      DecodedJWT decoded = verifier.verify(refreshToken);

      String typ = decoded.getClaim("typ").asString();
      if (typ == null || !typ.equals("refresh")) {
        throw new JWTVerificationException("Token fornecido não é um refresh token.");
      }

      String subject = decoded.getSubject();

      Instant accessExpiresAt = accessExpirationDate();
      Instant refreshExpiresAt = refreshExpirationDate();
      Date issuedAt = Date.from(creationDate());

      String newAccessToken = JWT.create()
        .withIssuer(ISSUER)
        .withIssuedAt(issuedAt)
        .withExpiresAt(Date.from(accessExpiresAt))
        .withSubject(subject)
        .withClaim("typ", "access")
        .sign(algorithm);

      String newRefreshToken = JWT.create()
        .withIssuer(ISSUER)
        .withIssuedAt(issuedAt)
        .withExpiresAt(Date.from(refreshExpiresAt))
        .withSubject(subject)
        .withClaim("typ", "refresh")
        .sign(algorithm);

      return new JwtTokenDTO(newAccessToken, "Bearer", accessExpiresAt.toEpochMilli(), newRefreshToken);
    } catch (JWTVerificationException exception) {
      throw new JWTVerificationException("Refresh token inválido ou expirado.");
    } catch (Exception e) {
      throw new RuntimeException(e);
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
      throw new RuntimeException(e);
    }
  }

  private Instant creationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
  }

  private Instant accessExpirationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusHours(1).toInstant();
  }

  private Instant refreshExpirationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusDays(7).toInstant();
  }

}
