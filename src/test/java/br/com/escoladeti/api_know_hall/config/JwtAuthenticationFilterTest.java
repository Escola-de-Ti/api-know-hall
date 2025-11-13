package br.com.escoladeti.api_know_hall.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

  @Mock
  private JwtTokenService jwtTokenService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private static final String JWT_SECRET = "test-secret-key-very-long-for-testing-purposes";
  private static final String TEST_USER = "teste@email.com";
  private static final String ISSUER = "api-know-hall";
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
  }

  @Test
  @DisplayName("Deve processar requisição sem Authorization header")
  void deveProcessarRequisicaoSemAuthorizationHeader() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtTokenService, never()).getSubjectFromToken(any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("Deve processar requisição com Authorization header vazio")
  void deveProcessarRequisicaoComAuthorizationHeaderVazio() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtTokenService, never()).getSubjectFromToken(any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("Deve processar requisição com header que não começa com Bearer")
  void deveProcessarRequisicaoComHeaderQuenaoComecaComBearer() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtTokenService, never()).getSubjectFromToken(any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("Deve autenticar com token válido")
  void deveAutenticarComTokenValido() throws ServletException, IOException {
    String token = criarTokenValido(TEST_USER);
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(token)).thenReturn(TEST_USER);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtTokenService, times(1)).getSubjectFromToken(token);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(TEST_USER);
  }

  @Test
  @DisplayName("Deve extrair token corretamente do Authorization header")
  void deveExtrairTokenCorretamentedoAuthorizationHeader() throws ServletException, IOException {
    String token = criarTokenValido(TEST_USER);
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(token)).thenReturn(TEST_USER);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtTokenService, times(1)).getSubjectFromToken(token);
  }

  @Test
  @DisplayName("Deve retornar 401 com token inválido")
  void deveRetornar401ComTokenInvalido() throws ServletException, IOException {
    String tokenInvalido = "token.invalido.aqui";
    String authHeader = "Bearer " + tokenInvalido;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(tokenInvalido))
      .thenThrow(new JWTVerificationException("Token inválido ou expirado."));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, times(1)).setContentType("application/json");
    verify(filterChain, never()).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("Deve retornar resposta de erro em JSON com token inválido")
  void deveRetornarRespostaDeErroEmJsonComTokenInvalido() throws ServletException, IOException {
    String tokenInvalido = "token.invalido.aqui";
    String authHeader = "Bearer " + tokenInvalido;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(tokenInvalido))
      .thenThrow(new JWTVerificationException("Token inválido ou expirado."));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    String responseContent = stringWriter.toString();
    assertThat(responseContent).contains("error");
    assertThat(responseContent).contains("Unauthorized");
  }

  @Test
  @DisplayName("Deve não substituir autenticação existente")
  void deveNaoSubstituirAutenticacaoExistente() throws ServletException, IOException {
    String token = criarTokenValido(TEST_USER);
    String authHeader = "Bearer " + token;

    // Simula uma autenticação já existente
    org.springframework.security.authentication.UsernamePasswordAuthenticationToken authExistente =
      new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
        "outro@email.com", null, java.util.List.of()
      );
    SecurityContextHolder.getContext().setAuthentication(authExistente);

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(token)).thenReturn(TEST_USER);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Verifica que a autenticação anterior foi mantida
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
      .isEqualTo("outro@email.com");
    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve processar múltiplos tokens sequencialmente")
  void deveProcessarMultiplosTokensSequencialmente() throws ServletException, IOException {
    String usuario1 = "usuario1@email.com";
    String usuario2 = "usuario2@email.com";

    String token1 = criarTokenValido(usuario1);
    String token2 = criarTokenValido(usuario2);

    // Primeira requisição
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token1);
    when(jwtTokenService.getSubjectFromToken(token1)).thenReturn(usuario1);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
      .isEqualTo(usuario1);

    // Limpar contexto
    SecurityContextHolder.clearContext();

    // Segunda requisição
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token2);
    when(jwtTokenService.getSubjectFromToken(token2)).thenReturn(usuario2);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
      .isEqualTo(usuario2);
  }

  @Test
  @DisplayName("Deve lançar exceção genérica ao processar token")
  void deveLancarExcecaoGenericaAoProcessarToken() throws ServletException, IOException {
    String tokenInvalido = "token.invalido.aqui";
    String authHeader = "Bearer " + tokenInvalido;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(tokenInvalido))
      .thenThrow(new RuntimeException("Erro inesperado"));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve não autenticar quando subject é nulo")
  void deveNaoAutenticarQuandoSubjectEhNulo() throws ServletException, IOException {
    String token = criarTokenValido(TEST_USER);
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(token)).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("Deve setar as authorities como lista vazia")
  void deveSetarAsAuthoritiesComoListaVazia() throws ServletException, IOException {
    String token = criarTokenValido(TEST_USER);
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(token)).thenReturn(TEST_USER);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
  }

  @Test
  @DisplayName("Deve continuar filtro chain mesmo após autenticação")
  void deveContinuarFiltroChainMesmoAposAutenticacao() throws ServletException, IOException {
    String token = criarTokenValido(TEST_USER);
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(token)).thenReturn(TEST_USER);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve retornar status 401 na resposta de erro")
  void deveRetornarStatus401NaRespostaDeErro() throws ServletException, IOException {
    String tokenInvalido = "token.invalido.aqui";
    String authHeader = "Bearer " + tokenInvalido;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(tokenInvalido))
      .thenThrow(new JWTVerificationException("Token inválido ou expirado."));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(401);
  }

  @Test
  @DisplayName("Deve setar content type como application/json")
  void deveSetarContentTypeComoApplicationJson() throws ServletException, IOException {
    String tokenInvalido = "token.invalido.aqui";
    String authHeader = "Bearer " + tokenInvalido;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtTokenService.getSubjectFromToken(tokenInvalido))
      .thenThrow(new JWTVerificationException("Token inválido ou expirado."));
    when(response.getWriter()).thenReturn(printWriter);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(response).setContentType("application/json");
  }

  // Método auxiliar para criar um token válido
  private String criarTokenValido(String subject) {
    Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
    Date issuedAt = Date.from(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant());
    Date expiresAt = Date.from(
      ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusHours(1).toInstant()
    );

    return JWT.create()
      .withIssuer(ISSUER)
      .withIssuedAt(issuedAt)
      .withExpiresAt(expiresAt)
      .withSubject(subject)
      .withClaim("typ", "access")
      .sign(algorithm);
  }

}
