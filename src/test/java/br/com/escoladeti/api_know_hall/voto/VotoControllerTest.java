package br.com.escoladeti.api_know_hall.voto;

import br.com.escoladeti.api_know_hall.controller.VotoController;
import br.com.escoladeti.api_know_hall.dto.voto.VotoResponseDTO;
import br.com.escoladeti.api_know_hall.service.VotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = VotoController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
  },
  excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.escoladeti.api_know_hall.config.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.escoladeti.api_know_hall.security.*")
  }
)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do VotoController")
class VotoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private VotoService votoService;

  private VotoResponseDTO votoAdicionado;
  private VotoResponseDTO votoRemovido;
  private VotoResponseDTO superVotoAdicionado;
  private VotoResponseDTO superVotoRemovido;
  private Principal mockPrincipal;

  @BeforeEach
  void setUp() {
    votoAdicionado = new VotoResponseDTO(true, 1L);
    votoRemovido = new VotoResponseDTO(false, 0L);
    superVotoAdicionado = new VotoResponseDTO(true, 1L);
    superVotoRemovido = new VotoResponseDTO(false, 0L);
    mockPrincipal = () -> "joao@email.com";
  }

  // ==================== TESTES DE UP_VOTE EM POST ====================

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/post/{postId} - Deve adicionar voto em post")
  void deveAdicionarVotoEmPost() throws Exception {
    when(votoService.votarEmPost(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(votoAdicionado);

    mockMvc.perform(post("/api/votos/post/1")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(true))
      .andExpect(jsonPath("$.totalUpVotes").value(1));

    verify(votoService).votarEmPost(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/post/{postId} - Deve remover voto em post")
  void deveRemoverVotoEmPost() throws Exception {
    when(votoService.votarEmPost(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(votoRemovido);

    mockMvc.perform(post("/api/votos/post/1")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(false))
      .andExpect(jsonPath("$.totalUpVotes").value(0));

    verify(votoService).votarEmPost(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/post/{postId} - Deve retornar 404 para post inexistente")
  void deveRetornar404ParaPostInexistente() throws Exception {
    when(votoService.votarEmPost(eq(BigInteger.valueOf(999)), any(Principal.class)))
      .thenThrow(new EntityNotFoundException("Post não encontrado"));

    mockMvc.perform(post("/api/votos/post/999")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Post não encontrado"));

    verify(votoService).votarEmPost(eq(BigInteger.valueOf(999)), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/post/{postId} - Deve retornar 400 ao votar no próprio post")
  void deveRetornar400AoVotarNoProprioPost() throws Exception {
    when(votoService.votarEmPost(eq(BigInteger.ONE), any(Principal.class)))
      .thenThrow(new IllegalArgumentException("Você não pode votar no próprio post"));

    mockMvc.perform(post("/api/votos/post/1")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Você não pode votar no próprio post"));

    verify(votoService).votarEmPost(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/post/{postId} - Deve validar ID inválido")
  void deveValidarIdInvalidoParaPost() throws Exception {
    mockMvc.perform(post("/api/votos/post/abc")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(votoService, never()).votarEmPost(any(), any());
  }

  // ==================== TESTES DE UP_VOTE EM COMENTÁRIO ====================

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId} - Deve adicionar voto em comentário")
  void deveAdicionarVotoEmComentario() throws Exception {
    when(votoService.votarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(votoAdicionado);

    mockMvc.perform(post("/api/votos/comentario/1")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(true))
      .andExpect(jsonPath("$.totalUpVotes").value(1));

    verify(votoService).votarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId} - Deve remover voto em comentário")
  void deveRemoverVotoEmComentario() throws Exception {
    when(votoService.votarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(votoRemovido);

    mockMvc.perform(post("/api/votos/comentario/1")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(false))
      .andExpect(jsonPath("$.totalUpVotes").value(0));

    verify(votoService).votarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId} - Deve retornar 404 para comentário inexistente")
  void deveRetornar404ParaComentarioInexistente() throws Exception {
    when(votoService.votarEmComentario(eq(BigInteger.valueOf(999)), any(Principal.class)))
      .thenThrow(new EntityNotFoundException("Comentário não encontrado"));

    mockMvc.perform(post("/api/votos/comentario/999")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Comentário não encontrado"));

    verify(votoService).votarEmComentario(eq(BigInteger.valueOf(999)), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId} - Deve retornar 400 ao votar no próprio comentário")
  void deveRetornar400AoVotarNoProprioComentario() throws Exception {
    when(votoService.votarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenThrow(new IllegalArgumentException("Você não pode votar no próprio comentário"));

    mockMvc.perform(post("/api/votos/comentario/1")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Você não pode votar no próprio comentário"));

    verify(votoService).votarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId} - Deve validar ID inválido")
  void deveValidarIdInvalidoParaComentario() throws Exception {
    mockMvc.perform(post("/api/votos/comentario/abc")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(votoService, never()).votarEmComentario(any(), any());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId}/super - Deve adicionar super voto em comentário")
  void deveAdicionarSuperVotoEmComentario() throws Exception {
    when(votoService.superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(superVotoAdicionado);

    mockMvc.perform(post("/api/votos/comentario/1/super")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(true))
      .andExpect(jsonPath("$.totalUpVotes").value(1));

    verify(votoService).superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId}/super - Deve remover super voto em comentário")
  void deveRemoverSuperVotoEmComentario() throws Exception {
    when(votoService.superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(superVotoRemovido);

    mockMvc.perform(post("/api/votos/comentario/1/super")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(false))
      .andExpect(jsonPath("$.totalUpVotes").value(0));

    verify(votoService).superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId}/super - Deve retornar 404 para comentário inexistente")
  void deveRetornar404ParaSuperVotoEmComentarioInexistente() throws Exception {
    when(votoService.superVotarEmComentario(eq(BigInteger.valueOf(999)), any(Principal.class)))
      .thenThrow(new EntityNotFoundException("Comentário não encontrado"));

    mockMvc.perform(post("/api/votos/comentario/999/super")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Comentário não encontrado"));

    verify(votoService).superVotarEmComentario(eq(BigInteger.valueOf(999)), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId}/super - Deve retornar 400 ao super votar no próprio comentário")
  void deveRetornar400AoSuperVotarNoProprioComentario() throws Exception {
    when(votoService.superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenThrow(new IllegalArgumentException("Você não pode votar no próprio comentário"));

    mockMvc.perform(post("/api/votos/comentario/1/super")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").value("Você não pode votar no próprio comentário"));

    verify(votoService).superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId}/super - Deve validar ID inválido")
  void deveValidarIdInvalidoParaSuperVoto() throws Exception {
    mockMvc.perform(post("/api/votos/comentario/abc/super")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(votoService, never()).superVotarEmComentario(any(), any());
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/votos/comentario/{comentarioId}/super - Deve adicionar super voto com múltiplos votos")
  void deveAdicionarSuperVotoComMultiplosVotos() throws Exception {
    VotoResponseDTO multiplosSuperVotos = new VotoResponseDTO(true, 5L);

    when(votoService.superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class)))
      .thenReturn(multiplosSuperVotos);

    mockMvc.perform(post("/api/votos/comentario/1/super")
        .principal(mockPrincipal)
        .with(csrf()))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.votado").value(true))
      .andExpect(jsonPath("$.totalUpVotes").value(5));

    verify(votoService).superVotarEmComentario(eq(BigInteger.ONE), any(Principal.class));
  }
}
