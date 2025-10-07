package br.com.escoladeti.api_know_hall.config;

import br.com.escoladeti.api_know_hall.dto.UsuarioLoginDTO;
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
  @Value("${PRIVATE_KEY}")
    private static String secretKey;

  private static final String SECRET_KEY = secretKey;

  private static final String ISSUER = "api-know-hall";

  public String generateToken(String user) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
      return JWT.create()
        .withIssuer(ISSUER)
        .withIssuedAt(creationDate())
        .withExpiresAt(expirationDate())
        .withSubject(user)
        .sign(algorithm);
    } catch (JWTCreationException exception){
      throw new JWTCreationException("Erro ao gerar token.", exception);
    }
  }

  public String getSubjectFromToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
      return JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()
        .verify(token)
        .getSubject();
    } catch (JWTVerificationException exception){
      throw new JWTVerificationException("Token inválido ou expirado.");
    }
  }

  private Instant creationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
  }

  private Instant expirationDate() {
    return ZonedDateTime.now(ZoneId.of("America/Sao_Paul")).plusHours(1).toInstant();
  }

}
