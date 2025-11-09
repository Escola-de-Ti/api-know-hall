package br.com.escoladeti.api_know_hall.historico;

import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.config.SecurityConfig;
import br.com.escoladeti.api_know_hall.controller.HistoricoTransacaoController;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoListResponseDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoRequestDTO;
import br.com.escoladeti.api_know_hall.dto.historico.HistoricoTransacaoResponseDTO;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.service.HistoricoTransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = HistoricoTransacaoController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
  },
  excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
  }
)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do HistoricoTransacaoController")
class HistoricoTransacaoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private HistoricoTransacaoService historicoTransacaoService;

  private Principal mockPrincipal;
  private HistoricoTransacaoResponseDTO transacaoDTO1;
  private HistoricoTransacaoResponseDTO transacaoDTO2;
  private HistoricoTransacaoListResponseDTO listResponseDTO;
  private Timestamp agora;

  @BeforeEach
  void setUp() {
    agora = Timestamp.from(Instant.now());
    mockPrincipal = () -> "joao@email.com";

    transacaoDTO1 = new HistoricoTransacaoResponseDTO(
      BigInteger.ONE,
      200L,
      MotivoTransacao.SUPER_VOTE,
      "Super vote em comentário",
      "Comentário 'Excelente resposta...' (ID: 456) recebeu Super Vote",
      agora
    );

    transacaoDTO2 = new HistoricoTransacaoResponseDTO(
      BigInteger.TWO,
      50L,
      MotivoTransacao.UP_VOTE_COMENTARIO,
      "Up vote em comentário",
      "Comentário atingiu 5 upvotes - 1 marcos de 5 conquistados",
      agora
    );

    listResponseDTO = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoDTO1, transacaoDTO2),
      1500L,
      500L,
      1000L,
      false,
      1,
      2L
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve buscar histórico com sucesso")
  void deveBuscarHistoricoComSucesso() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("page", "0")
        .param("size", "20"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(2)))
      .andExpect(jsonPath("$.transacoes[0].quantidade").value(200))
      .andExpect(jsonPath("$.transacoes[0].motivo").value("SUPER_VOTE"))
      .andExpect(jsonPath("$.totalRecebido").value(1500))
      .andExpect(jsonPath("$.totalGasto").value(500))
      .andExpect(jsonPath("$.saldoAtual").value(1000))
      .andExpect(jsonPath("$.hasMore").value(false))
      .andExpect(jsonPath("$.totalElements").value(2));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve usar valores padrão quando não especificados")
  void deveUsarValoresPadraoQuandoNaoEspecificados() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal))
      .andDo(print())
      .andExpect(status().isOk());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req ->
        req.page() == 0 &&
          req.size() == 20 &&
          req.motivo() == null &&
          req.dataInicio() == null &&
          req.dataFim() == null
      )
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve retornar lista vazia quando não há transações")
  void deveRetornarListaVaziaQuandoNaoHaTransacoes() throws Exception {
    HistoricoTransacaoListResponseDTO responseVazio = new HistoricoTransacaoListResponseDTO(
      List.of(),
      0L,
      0L,
      1000L,
      false,
      0,
      0L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseVazio);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes").isEmpty())
      .andExpect(jsonPath("$.totalRecebido").value(0))
      .andExpect(jsonPath("$.totalGasto").value(0))
      .andExpect(jsonPath("$.hasMore").value(false));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por motivo SUPER_VOTE")
  void deveFiltrarPorMotivoSuperVote() throws Exception {
    HistoricoTransacaoListResponseDTO responseSuperVote = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoDTO1),
      200L,
      0L,
      1000L,
      false,
      1,
      1L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseSuperVote);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "SUPER_VOTE"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(1)))
      .andExpect(jsonPath("$.transacoes[0].motivo").value("SUPER_VOTE"));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> req.motivo() == MotivoTransacao.SUPER_VOTE)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por motivo UP_VOTE_COMENTARIO")
  void deveFiltrarPorMotivoUpVoteComentario() throws Exception {
    HistoricoTransacaoListResponseDTO responseUpVote = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoDTO2),
      50L,
      0L,
      1000L,
      false,
      1,
      1L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseUpVote);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "UP_VOTE_COMENTARIO"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(1)))
      .andExpect(jsonPath("$.transacoes[0].motivo").value("UP_VOTE_COMENTARIO"));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> req.motivo() == MotivoTransacao.UP_VOTE_COMENTARIO)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por período")
  void deveFiltrarPorPeriodo() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("dataInicio", "2025-01-01T00:00:00")
        .param("dataFim", "2025-01-31T23:59:59"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(2)));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req ->
        req.dataInicio() != null &&
          req.dataFim() != null
      )
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por motivo E período")
  void deveFiltrarPorMotivoEPeriodo() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "RESPOSTA_DESTAQUE")
        .param("dataInicio", "2025-01-01T00:00:00")
        .param("dataFim", "2025-01-31T23:59:59"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req ->
        req.motivo() == MotivoTransacao.RESPOSTA_DESTAQUE &&
          req.dataInicio() != null &&
          req.dataFim() != null
      )
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve paginar resultados")
  void devePaginarResultados() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("page", "1")
        .param("size", "10"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req ->
        req.page() == 1 &&
          req.size() == 10
      )
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve indicar hasMore true")
  void deveIndicarHasMoreTrue() throws Exception {
    HistoricoTransacaoListResponseDTO responseComMais = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoDTO1, transacaoDTO2),
      1500L,
      500L,
      1000L,
      true,
      5,
      100L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseComMais);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("page", "0")
        .param("size", "2"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.hasMore").value(true))
      .andExpect(jsonPath("$.totalPages").value(5))
      .andExpect(jsonPath("$.totalElements").value(100));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve aceitar pageSize personalizado")
  void deveAceitarPageSizePersonalizado() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("size", "50"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> req.size() == 50)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes/resumo - Deve buscar resumo com sucesso")
  void deveBuscarResumoComSucesso() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes/resumo")
        .principal(mockPrincipal))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecebido").value(1500))
      .andExpect(jsonPath("$.totalGasto").value(500))
      .andExpect(jsonPath("$.saldoAtual").value(1000))
      .andExpect(jsonPath("$.totalTransacoes").value(2));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req ->
        req.page() == 0 &&
          req.size() == 1
      )
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes/resumo - Deve retornar zeros quando não há transações")
  void deveRetornarZerosNoResumoQuandoNaoHaTransacoes() throws Exception {
    HistoricoTransacaoListResponseDTO responseVazio = new HistoricoTransacaoListResponseDTO(
      List.of(),
      0L,
      0L,
      500L,
      false,
      0,
      0L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseVazio);

    mockMvc.perform(get("/api/historico-transacoes/resumo")
        .principal(mockPrincipal))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecebido").value(0))
      .andExpect(jsonPath("$.totalGasto").value(0))
      .andExpect(jsonPath("$.saldoAtual").value(500))
      .andExpect(jsonPath("$.totalTransacoes").value(0));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve retornar 404 quando usuário não encontrado")
  void deveRetornar404QuandoUsuarioNaoEncontrado() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenThrow(new EntityNotFoundException("Usuário não encontrado"));

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve retornar 400 com motivo inválido")
  void deveRetornar400ComMotivoInvalido() throws Exception {
    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "MOTIVO_INVALIDO"))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(historicoTransacaoService, never()).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve retornar 400 com formato de data inválido")
  void deveRetornar400ComFormatoDeDataInvalido() throws Exception {
    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("dataInicio", "invalid-date"))
      .andDo(print())
      .andExpect(status().isBadRequest());

    verify(historicoTransacaoService, never()).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }


  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por RESPOSTA_DESTAQUE")
  void deveFiltrarPorRespostaDestaque() throws Exception {
    HistoricoTransacaoResponseDTO transacaoDestaque = new HistoricoTransacaoResponseDTO(
      BigInteger.valueOf(5),
      100L,
      MotivoTransacao.RESPOSTA_DESTAQUE,
      "Resposta marcada como destaque",
      "Comentário marcado como Resposta Destaque com 20 upvotes",
      agora
    );

    HistoricoTransacaoListResponseDTO responseDestaque = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoDestaque),
      100L,
      0L,
      1000L,
      false,
      1,
      1L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseDestaque);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "RESPOSTA_DESTAQUE"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(1)))
      .andExpect(jsonPath("$.transacoes[0].motivo").value("RESPOSTA_DESTAQUE"))
      .andExpect(jsonPath("$.transacoes[0].quantidade").value(100));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> req.motivo() == MotivoTransacao.RESPOSTA_DESTAQUE)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por INSCRICAO_WORKSHOP_ALUNO (negativo)")
  void deveFiltrarPorInscricaoWorkshopAluno() throws Exception {
    HistoricoTransacaoResponseDTO transacaoWorkshop = new HistoricoTransacaoResponseDTO(
      BigInteger.valueOf(6),
      -500L,
      MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO,
      "Inscrição em workshop como aluno",
      "Inscrição em workshop de React Native",
      agora
    );

    HistoricoTransacaoListResponseDTO responseWorkshop = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoWorkshop),
      0L,
      500L,
      500L,
      false,
      1,
      1L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseWorkshop);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "INSCRICAO_WORKSHOP_ALUNO"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(1)))
      .andExpect(jsonPath("$.transacoes[0].motivo").value("INSCRICAO_WORKSHOP_ALUNO"))
      .andExpect(jsonPath("$.transacoes[0].quantidade").value(-500));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> req.motivo() == MotivoTransacao.INSCRICAO_WORKSHOP_ALUNO)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve filtrar por CONQUISTA")
  void deveFiltrarPorConquista() throws Exception {
    HistoricoTransacaoResponseDTO transacaoConquista = new HistoricoTransacaoResponseDTO(
      BigInteger.valueOf(7),
      150L,
      MotivoTransacao.CONQUISTA,
      "Conquista desbloqueada",
      "Conquistou badge de Contribuidor Ativo",
      agora
    );

    HistoricoTransacaoListResponseDTO responseConquista = new HistoricoTransacaoListResponseDTO(
      List.of(transacaoConquista),
      150L,
      0L,
      1150L,
      false,
      1,
      1L
    );

    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(responseConquista);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("motivo", "CONQUISTA"))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes", hasSize(1)))
      .andExpect(jsonPath("$.transacoes[0].motivo").value("CONQUISTA"));

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> req.motivo() == MotivoTransacao.CONQUISTA)
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve aceitar page e size negativos e usar defaults")
  void deveAceitarPageESizeNegativosEUsarDefaults() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("page", "-1")
        .param("size", "-10"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> {
        return req.page() == 0 && req.size() == 20;
      })
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve limitar size máximo a 100")
  void deveLimitarSizeMaximoA100() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal)
        .param("size", "200"))
      .andDo(print())
      .andExpect(status().isOk());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.argThat(req -> {
        return req.size() == 100;
      })
    );
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/historico-transacoes - Deve retornar estrutura JSON correta")
  void deveRetornarEstruturaJSONCorreta() throws Exception {
    when(historicoTransacaoService.buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    )).thenReturn(listResponseDTO);

    mockMvc.perform(get("/api/historico-transacoes")
        .principal(mockPrincipal))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.transacoes").isArray())
      .andExpect(jsonPath("$.totalRecebido").isNumber())
      .andExpect(jsonPath("$.totalGasto").isNumber())
      .andExpect(jsonPath("$.saldoAtual").isNumber())
      .andExpect(jsonPath("$.hasMore").isBoolean())
      .andExpect(jsonPath("$.totalPages").isNumber())
      .andExpect(jsonPath("$.totalElements").isNumber())
      .andExpect(jsonPath("$.transacoes[0].id").exists())
      .andExpect(jsonPath("$.transacoes[0].quantidade").exists())
      .andExpect(jsonPath("$.transacoes[0].motivo").exists())
      .andExpect(jsonPath("$.transacoes[0].motivoDescricao").exists())
      .andExpect(jsonPath("$.transacoes[0].descricao").exists())
      .andExpect(jsonPath("$.transacoes[0].dataTransacao").exists());

    verify(historicoTransacaoService).buscarHistoricoUsuario(
      ArgumentMatchers.any(Principal.class),
      ArgumentMatchers.any(HistoricoTransacaoRequestDTO.class)
    );
  }
}
