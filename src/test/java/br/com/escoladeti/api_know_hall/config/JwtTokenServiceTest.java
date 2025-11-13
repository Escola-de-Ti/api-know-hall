package br.com.escoladeti.api_know_hall.config;

import br.com.escoladeti.api_know_hall.dto.JwtTokenDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtTokenService")
class JwtTokenServiceTest {

  @InjectMocks
  private JwtTokenService jwtTokenService;

  private static final String JWT_SECRET = "test-secret-key-very-long-for-testing-purposes";
  private static final String TEST_USER = "teste@email.com";
  private static final String ISSUER = "api-know-hall";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtTokenService, "jwtSecret", JWT_SECRET);
  }

  @Test
  @DisplayName("Deve gerar tokens com sucesso")
  void deveGerarTokensComSucesso() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    assertThat(resultado).isNotNull();
    assertThat(resultado.access_token()).isNotBlank();
    assertThat(resultado.refresh_token()).isNotBlank();
    assertThat(resultado.token_type()).isEqualTo("Bearer");
    assertThat(resultado.expires_in()).isGreaterThan(0);
  }

  @Test
  @DisplayName("Deve gerar access token com subject correto")
  void deveGerarAccessTokenComSubjectCorreto() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    String subject = jwtTokenService.getSubjectFromToken(resultado.access_token());
    assertThat(subject).isEqualTo(TEST_USER);
  }

  @Test
  @DisplayName("Deve gerar refresh token com subject correto")
  void deveGerarRefreshTokenComSubjectCorreto() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
    String subject = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.refresh_token())
      .getSubject();

    assertThat(subject).isEqualTo(TEST_USER);
  }

  @Test
  @DisplayName("Deve gerar tokens com claims corretos")
  void deveGerarTokensComClaimsCorretos() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

    String accessTyp = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.access_token())
      .getClaim("typ")
      .asString();

    String refreshTyp = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.refresh_token())
      .getClaim("typ")
      .asString();

    assertThat(accessTyp).isEqualTo("access");
    assertThat(refreshTyp).isEqualTo("refresh");
  }

  @Test
  @DisplayName("Deve gerar tokens com issuer correto")
  void deveGerarTokensComIssuerCorreto() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

    String accessIssuer = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.access_token())
      .getIssuer();

    String refreshIssuer = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.refresh_token())
      .getIssuer();

    assertThat(accessIssuer).isEqualTo(ISSUER);
    assertThat(refreshIssuer).isEqualTo(ISSUER);
  }

  @Test
  @DisplayName("Deve gerar tokens com datas de expiração diferentes")
  void deveGerarTokensComDatasDeExpiracaoDiferentes() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

    Date accessExpiresAt = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.access_token())
      .getExpiresAt();

    Date refreshExpiresAt = JWT.require(algorithm)
      .withIssuer(ISSUER)
      .build()
      .verify(resultado.refresh_token())
      .getExpiresAt();

    assertThat(refreshExpiresAt.getTime()).isGreaterThan(accessExpiresAt.getTime());
  }

  @Test
  @DisplayName("Deve extrair subject do access token com sucesso")
  void deveExtrairSubjectDoAccessTokenComSucesso() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    String subject = jwtTokenService.getSubjectFromToken(resultado.access_token());

    assertThat(subject).isEqualTo(TEST_USER);
  }

  @Test
  @DisplayName("Deve lançar exceção ao extrair subject de token inválido")
  void deveLancarExcecaoAoExtrairSubjectDeTokenInvalido() {
    String tokenInvalido = "token.invalido.aqui";

    assertThatThrownBy(() -> jwtTokenService.getSubjectFromToken(tokenInvalido))
      .isInstanceOf(JWTVerificationException.class)
      .hasMessage("Token inválido ou expirado.");
  }

  @Test
  @DisplayName("Deve lançar exceção ao extrair subject de token malformado")
  void deveLancarExcecaoAoExtrairSubjectDeTokenMalformado() {
    String tokenMalformado = "malformed";

    assertThatThrownBy(() -> jwtTokenService.getSubjectFromToken(tokenMalformado))
      .isInstanceOf(JWTVerificationException.class)
      .hasMessage("Token inválido ou expirado.");
  }

  @Test
  @DisplayName("Deve renovar tokens com sucesso")
  void deveRenovarTokensComSucesso() {
    JwtTokenDTO tokensOriginais = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    JwtTokenDTO tokensRenovados = jwtTokenService.refreshTokens(tokensOriginais.refresh_token());

    assertThat(tokensRenovados).isNotNull();
    assertThat(tokensRenovados.access_token()).isNotBlank();
    assertThat(tokensRenovados.refresh_token()).isNotBlank();
    assertThat(tokensRenovados.token_type()).isEqualTo("Bearer");
    assertThat(tokensRenovados.expires_in()).isGreaterThan(0);
  }

  @Test
  @DisplayName("Deve renovar tokens mantendo o subject")
  void deveRenovarTokensMantendoOSubject() {
    JwtTokenDTO tokensOriginais = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    JwtTokenDTO tokensRenovados = jwtTokenService.refreshTokens(tokensOriginais.refresh_token());

    String subject = jwtTokenService.getSubjectFromToken(tokensRenovados.access_token());
    assertThat(subject).isEqualTo(TEST_USER);
  }

  @Test
  @DisplayName("Deve renovar tokens com access token diferente")
  void deveRenovarTokensComAccessTokenDiferente() throws InterruptedException {
    JwtTokenDTO tokensOriginais = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    Thread.sleep(1100);

    JwtTokenDTO tokensRenovados = jwtTokenService.refreshTokens(tokensOriginais.refresh_token());

    assertThat(tokensRenovados.access_token()).isNotEqualTo(tokensOriginais.access_token());
  }

  @Test
  @DisplayName("Deve lançar exceção ao renovar tokens com refresh token inválido")
  void deveLancarExcecaoAoRenovarTokensComRefreshTokenInvalido() {
    String tokenInvalido = "token.invalido.aqui";

    assertThatThrownBy(() -> jwtTokenService.refreshTokens(tokenInvalido))
      .isInstanceOf(JWTVerificationException.class)
      .hasMessage("Refresh token inválido ou expirado.");
  }

  @Test
  @DisplayName("Deve lançar exceção ao renovar tokens com access token em vez de refresh token")
  void deveLancarExcecaoAoRenovarTokensComAccessTokenEmVezDeRefreshToken() {
    JwtTokenDTO tokens = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    assertThatThrownBy(() -> jwtTokenService.refreshTokens(tokens.access_token()))
      .isInstanceOf(JWTVerificationException.class)
      .hasMessage("Refresh token inválido ou expirado.");
  }

  @Test
  @DisplayName("Deve gerar tokens com usuários diferentes e subjects diferentes")
  void deveGerarTokensComUsuariosDiferentesESubjectsDiferentes() {
    String usuario1 = "usuario1@email.com";
    String usuario2 = "usuario2@email.com";

    JwtTokenDTO tokens1 = jwtTokenService.generateTokenWithExpiration(usuario1);
    JwtTokenDTO tokens2 = jwtTokenService.generateTokenWithExpiration(usuario2);

    String subject1 = jwtTokenService.getSubjectFromToken(tokens1.access_token());
    String subject2 = jwtTokenService.getSubjectFromToken(tokens2.access_token());

    assertThat(subject1).isEqualTo(usuario1);
    assertThat(subject2).isEqualTo(usuario2);
    assertThat(subject1).isNotEqualTo(subject2);
  }

  @Test
  @DisplayName("Deve gerar tokens com expiração válida (access token com 1 hora)")
  void deveGerarTokensComExpiracaoValidaAccessToken() {
    JwtTokenDTO resultado = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    long agora = System.currentTimeMillis();
    long diferenca = resultado.expires_in() - agora;

    // Verifica se a expiração está aproximadamente em 1 hora (60 minutos)
    // Permite uma margem de 5 minutos
    long umHoraEmMilisegundos = 60 * 60 * 1000;
    long margemDeTolerancia = 5 * 60 * 1000;

    assertThat(diferenca).isGreaterThan(umHoraEmMilisegundos - margemDeTolerancia);
    assertThat(diferenca).isLessThan(umHoraEmMilisegundos + margemDeTolerancia);
  }

  @Test
  @DisplayName("Deve validar refresh token renovado com sucesso")
  void deveValidarRefreshTokenRenovadoComSucesso() {
    JwtTokenDTO tokensOriginais = jwtTokenService.generateTokenWithExpiration(TEST_USER);

    JwtTokenDTO tokensRenovados = jwtTokenService.refreshTokens(tokensOriginais.refresh_token());

    // Verifica se consegue extrair o subject do refresh token renovado
    // Para isso, temos que renovar novamente
    JwtTokenDTO tokensRenovados2 = jwtTokenService.refreshTokens(tokensRenovados.refresh_token());

    assertThat(tokensRenovados2).isNotNull();
    assertThat(tokensRenovados2.access_token()).isNotBlank();
  }

}
