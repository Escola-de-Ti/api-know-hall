package br.com.escoladeti.api_know_hall.inscricao;

import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.controller.InscricaoController;
import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoCreateDTO;
import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoResponseDTO;
import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoUpdateDTO;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import br.com.escoladeti.api_know_hall.service.InscricaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = InscricaoController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class
  },
  excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = JwtAuthenticationFilter.class
  )
)
@ActiveProfiles("test")
@DisplayName("Testes Unitários - InscricaoController")
class InscricaoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private InscricaoService inscricaoService;

  private InscricaoResponseDTO inscricaoResponse;
  private InscricaoCreateDTO inscricaoCreateDTO;
  private InscricaoUpdateDTO inscricaoUpdateDTO;

  @BeforeEach
  void setUp() {
    inscricaoResponse = InscricaoResponseDTO.builder()
      .id(BigInteger.valueOf(100))
      .usuarioId(BigInteger.valueOf(2))
      .usuarioNome("Maria Aluna")
      .workshopId(BigInteger.valueOf(10))
      .workshopTitulo("Spring Boot Avançado")
      .status(StatusInscricao.INSCRITO)
      .dataInscricao(LocalDateTime.now())
      .build();

    inscricaoCreateDTO = new InscricaoCreateDTO();
    inscricaoCreateDTO.setWorkshopId(BigInteger.valueOf(10));

    inscricaoUpdateDTO = new InscricaoUpdateDTO();
    inscricaoUpdateDTO.setStatus(StatusInscricao.CANCELADO);
  }

  @Nested
  @DisplayName("Testes POST /api/inscricoes - Inscrever em Workshop")
  class InscreverTests {

    @Test
    @DisplayName("Deve inscrever usuário em workshop com sucesso")
    void deveInscreverUsuarioComSucesso() throws Exception {
      // Arrange
      when(inscricaoService.inscrever(eq("maria@email.com"), eq(BigInteger.valueOf(10))))
        .thenReturn(inscricaoResponse);

      // Act & Assert
      mockMvc.perform(post("/api/inscricoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(inscricaoCreateDTO))
          .principal(() -> "maria@email.com"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.usuarioId", is(2)))
        .andExpect(jsonPath("$.usuarioNome", is("Maria Aluna")))
        .andExpect(jsonPath("$.workshopId", is(10)))
        .andExpect(jsonPath("$.workshopTitulo", is("Spring Boot Avançado")))
        .andExpect(jsonPath("$.status", is("INSCRITO")));

      verify(inscricaoService).inscrever(eq("maria@email.com"), eq(BigInteger.valueOf(10)));
    }

    @Test
    @DisplayName("Deve retornar 400 quando workshopId é nulo")
    void deveRetornar400QuandoWorkshopIdNulo() throws Exception {
      // Arrange
      inscricaoCreateDTO.setWorkshopId(null);

      // Act & Assert
      mockMvc.perform(post("/api/inscricoes")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(inscricaoCreateDTO))
          .principal(() -> "maria@email.com"))
        .andExpect(status().isBadRequest());

      verify(inscricaoService, never()).inscrever(any(), any());
    }
  }

  @Nested
  @DisplayName("Testes DELETE /api/inscricoes/workshops/{workshopId} - Cancelar Inscrição")
  class CancelarInscricaoTests {

    @Test
    @DisplayName("Deve cancelar inscrição com sucesso")
    void deveCancelarInscricaoComSucesso() throws Exception {
      // Arrange
      doNothing().when(inscricaoService).cancelarInscricao("maria@email.com", BigInteger.valueOf(10));

      // Act & Assert
      mockMvc.perform(delete("/api/inscricoes/workshops/10")
          .principal(() -> "maria@email.com"))
        .andExpect(status().isNoContent());

      verify(inscricaoService).cancelarInscricao("maria@email.com", BigInteger.valueOf(10));
    }
  }

  @Nested
  @DisplayName("Testes GET /api/inscricoes/workshops/{workshopId} - Buscar Inscrição")
  class BuscarInscricaoTests {

    @Test
    @DisplayName("Deve buscar inscrição com sucesso")
    void deveBuscarInscricaoComSucesso() throws Exception {
      // Arrange
      when(inscricaoService.buscarInscricao("maria@email.com", BigInteger.valueOf(10)))
        .thenReturn(inscricaoResponse);

      // Act & Assert
      mockMvc.perform(get("/api/inscricoes/workshops/10")
          .principal(() -> "maria@email.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.usuarioId", is(2)))
        .andExpect(jsonPath("$.workshopId", is(10)))
        .andExpect(jsonPath("$.status", is("INSCRITO")));

      verify(inscricaoService).buscarInscricao("maria@email.com", BigInteger.valueOf(10));
    }
  }

  @Nested
  @DisplayName("Testes GET /api/inscricoes/minhas - Listar Minhas Inscrições")
  class ListarMinhasInscricoesTests {

    @Test
    @DisplayName("Deve listar inscrições do usuário com sucesso")
    void deveListarInscricoesDoUsuarioComSucesso() throws Exception {
      // Arrange
      InscricaoResponseDTO inscricao2 = InscricaoResponseDTO.builder()
        .id(BigInteger.valueOf(101))
        .usuarioId(BigInteger.valueOf(2))
        .usuarioNome("Maria Aluna")
        .workshopId(BigInteger.valueOf(11))
        .workshopTitulo("React Native")
        .status(StatusInscricao.INSCRITO)
        .dataInscricao(LocalDateTime.now())
        .build();

      List<InscricaoResponseDTO> inscricoes = Arrays.asList(inscricaoResponse, inscricao2);

      when(inscricaoService.listarInscricoesPorUsuario("maria@email.com"))
        .thenReturn(inscricoes);

      // Act & Assert
      mockMvc.perform(get("/api/inscricoes/minhas")
          .principal(() -> "maria@email.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(100)))
        .andExpect(jsonPath("$[0].workshopTitulo", is("Spring Boot Avançado")))
        .andExpect(jsonPath("$[1].id", is(101)))
        .andExpect(jsonPath("$[1].workshopTitulo", is("React Native")));

      verify(inscricaoService).listarInscricoesPorUsuario("maria@email.com");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem inscrições")
    void deveRetornarListaVaziaQuandoUsuarioSemInscricoes() throws Exception {
      // Arrange
      when(inscricaoService.listarInscricoesPorUsuario("maria@email.com"))
        .thenReturn(Arrays.asList());

      // Act & Assert
      mockMvc.perform(get("/api/inscricoes/minhas")
          .principal(() -> "maria@email.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

      verify(inscricaoService).listarInscricoesPorUsuario("maria@email.com");
    }
  }

  @Nested
  @DisplayName("Testes GET /api/inscricoes/workshops/{workshopId}/participantes - Listar Participantes")
  class ListarParticipantesTests {

    @Test
    @DisplayName("Deve listar participantes do workshop com sucesso")
    void deveListarParticipantesDoWorkshopComSucesso() throws Exception {
      // Arrange
      InscricaoResponseDTO inscricao2 = InscricaoResponseDTO.builder()
        .id(BigInteger.valueOf(101))
        .usuarioId(BigInteger.valueOf(3))
        .usuarioNome("Pedro Silva")
        .workshopId(BigInteger.valueOf(10))
        .workshopTitulo("Spring Boot Avançado")
        .status(StatusInscricao.INSCRITO)
        .dataInscricao(LocalDateTime.now())
        .build();

      List<InscricaoResponseDTO> participantes = Arrays.asList(inscricaoResponse, inscricao2);

      when(inscricaoService.listarInscricoesPorWorkshop(BigInteger.valueOf(10)))
        .thenReturn(participantes);

      // Act & Assert
      mockMvc.perform(get("/api/inscricoes/workshops/10/participantes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].usuarioNome", is("Maria Aluna")))
        .andExpect(jsonPath("$[1].usuarioNome", is("Pedro Silva")))
        .andExpect(jsonPath("$[0].workshopId", is(10)))
        .andExpect(jsonPath("$[1].workshopId", is(10)));

      verify(inscricaoService).listarInscricoesPorWorkshop(BigInteger.valueOf(10));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando workshop não tem participantes")
    void deveRetornarListaVaziaQuandoWorkshopSemParticipantes() throws Exception {
      // Arrange
      when(inscricaoService.listarInscricoesPorWorkshop(BigInteger.valueOf(10)))
        .thenReturn(Arrays.asList());

      // Act & Assert
      mockMvc.perform(get("/api/inscricoes/workshops/10/participantes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

      verify(inscricaoService).listarInscricoesPorWorkshop(BigInteger.valueOf(10));
    }
  }

  @Nested
  @DisplayName("Testes PATCH /api/inscricoes/{inscricaoId} - Atualizar Status")
  class AtualizarStatusTests {

    @Test
    @DisplayName("Deve atualizar status da inscrição com sucesso")
    void deveAtualizarStatusComSucesso() throws Exception {
      // Arrange
      InscricaoResponseDTO inscricaoCancelada = InscricaoResponseDTO.builder()
        .id(BigInteger.valueOf(100))
        .usuarioId(BigInteger.valueOf(2))
        .usuarioNome("Maria Aluna")
        .workshopId(BigInteger.valueOf(10))
        .workshopTitulo("Spring Boot Avançado")
        .status(StatusInscricao.CANCELADO)
        .dataInscricao(LocalDateTime.now())
        .build();

      when(inscricaoService.atualizarStatusInscricao(BigInteger.valueOf(100), StatusInscricao.CANCELADO))
        .thenReturn(inscricaoCancelada);

      // Act & Assert
      mockMvc.perform(patch("/api/inscricoes/100")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(inscricaoUpdateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(100)))
        .andExpect(jsonPath("$.status", is("CANCELADO")));

      verify(inscricaoService).atualizarStatusInscricao(BigInteger.valueOf(100), StatusInscricao.CANCELADO);
    }

    @Test
    @DisplayName("Deve retornar 400 quando status é nulo")
    void deveRetornar400QuandoStatusNulo() throws Exception {
      // Arrange
      inscricaoUpdateDTO.setStatus(null);

      // Act & Assert
      mockMvc.perform(patch("/api/inscricoes/100")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(inscricaoUpdateDTO)))
        .andExpect(status().isBadRequest());

      verify(inscricaoService, never()).atualizarStatusInscricao(any(), any());
    }
  }
}
