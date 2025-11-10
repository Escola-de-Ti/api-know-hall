package br.com.escoladeti.api_know_hall.workshops;

import br.com.escoladeti.api_know_hall.config.JwtAuthenticationFilter;
import br.com.escoladeti.api_know_hall.controller.WorkshopController;
import br.com.escoladeti.api_know_hall.dto.workshop.DescricaoWorkshopDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopCreateDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.service.WorkshopService;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
  controllers = WorkshopController.class,
  excludeAutoConfiguration = {
    SecurityAutoConfiguration.class
  },
  excludeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = JwtAuthenticationFilter.class
  )
)
@ActiveProfiles("test")
@DisplayName("Testes Unitários - WorkshopController")
class WorkshopControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WorkshopService workshopService;

  private Usuario instrutor;
  private Usuario usuarioComum;
  private Workshop workshopAberto;
  private Workshop workshopEmAndamento;
  private Workshop workshopConcluido;

  @BeforeEach
  void setUp() {
    instrutor = new Usuario();
    instrutor.setId(BigInteger.valueOf(1));
    instrutor.setNome("João");
    instrutor.setEmail("joao.instrutor@email.com");
    instrutor.setCpf("12345678901");
    instrutor.setTipoUsuario(TipoUsuario.INSTRUTOR);
    instrutor.setStatusUsuario(StatusUsuario.ATIVO);

    usuarioComum = new Usuario();
    usuarioComum.setId(BigInteger.valueOf(2));
    usuarioComum.setNome("Maria");
    usuarioComum.setEmail("maria.comum@email.com");
    usuarioComum.setCpf("98765432109");
    usuarioComum.setTipoUsuario(TipoUsuario.ALUNO);
    usuarioComum.setStatusUsuario(StatusUsuario.ATIVO);

    workshopAberto = new Workshop();
    workshopAberto.setId(BigInteger.valueOf(1));
    workshopAberto.setTitulo("Spring Boot Avançado");
    workshopAberto.setLinkMeet("https://meet.google.com/abc-defg");
    workshopAberto.setStatus(StatusWorkshop.ABERTO);
    workshopAberto.setInstrutor(instrutor);
    workshopAberto.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
    workshopAberto.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));

    workshopEmAndamento = new Workshop();
    workshopEmAndamento.setId(BigInteger.valueOf(2));
    workshopEmAndamento.setTitulo("Workshop Imediato");
    workshopEmAndamento.setLinkMeet("https://meet.google.com/now");
    workshopEmAndamento.setStatus(StatusWorkshop.EM_ANDAMENTO);
    workshopEmAndamento.setInstrutor(instrutor);
    workshopEmAndamento.setDataInicio(Timestamp.from(Instant.now()));
    workshopEmAndamento.setDataTermino(Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS)));

    workshopConcluido = new Workshop();
    workshopConcluido.setId(BigInteger.valueOf(3));
    workshopConcluido.setTitulo("Workshop Concluído");
    workshopConcluido.setLinkMeet("https://meet.google.com/past");
    workshopConcluido.setStatus(StatusWorkshop.CONCLUIDO);
    workshopConcluido.setInstrutor(instrutor);
    workshopConcluido.setDataInicio(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
    workshopConcluido.setDataTermino(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS)));
  }

  @Nested
  @DisplayName("POST /api/workshops - Criar Workshop")
  class CriarWorkshopTests {

    @Test
    @DisplayName("Deve criar workshop com sucesso e status ABERTO")
    void deveCriarWorkshopComSucesso() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      DescricaoWorkshopDTO descricaoDTO = new DescricaoWorkshopDTO();
      descricaoDTO.setTema("Backend Java");
      descricaoDTO.setDescricao("Workshop sobre Spring Boot");

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Spring Boot Avançado");
      createDTO.setLinkMeet("https://meet.google.com/abc-defg");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);
      createDTO.setDescricao(descricaoDTO);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopAberto);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.titulo").value("Spring Boot Avançado"))
        .andExpect(jsonPath("$.linkMeet").value("https://meet.google.com/abc-defg"))
        .andExpect(jsonPath("$.status").value("ABERTO"));

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve criar workshop com status EM_ANDAMENTO quando data é hoje")
    void deveCriarWorkshopEmAndamento() throws Exception {
      Timestamp hoje = Timestamp.from(Instant.now());
      Timestamp fim = Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Imediato");
      createDTO.setLinkMeet("https://meet.google.com/now");
      createDTO.setDataInicio(hoje);
      createDTO.setDataTermino(fim);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopEmAndamento);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titulo").value("Workshop Imediato"))
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve criar workshop com status EM_ANDAMENTO quando data no passado")
    void deveCriarWorkshopComDataPassada() throws Exception {
      Timestamp passado = Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS));
      Timestamp fim = Timestamp.from(Instant.now().plus(3, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Iniciado");
      createDTO.setLinkMeet("https://meet.google.com/started");
      createDTO.setDataInicio(passado);
      createDTO.setDataTermino(fim);

      Workshop workshopIniciado = new Workshop();
      workshopIniciado.setId(BigInteger.valueOf(3));
      workshopIniciado.setTitulo("Workshop Iniciado");
      workshopIniciado.setStatus(StatusWorkshop.EM_ANDAMENTO);
      workshopIniciado.setInstrutor(instrutor);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopIniciado);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve criar workshop sem descrição")
    void deveCriarWorkshopSemDescricao() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Sem Descrição");
      createDTO.setLinkMeet("https://meet.google.com/no-desc");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      Workshop workshopSemDesc = new Workshop();
      workshopSemDesc.setId(BigInteger.valueOf(4));
      workshopSemDesc.setTitulo("Workshop Sem Descrição");
      workshopSemDesc.setStatus(StatusWorkshop.ABERTO);
      workshopSemDesc.setInstrutor(instrutor);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopSemDesc);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titulo").value("Workshop Sem Descrição"));

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 quando usuário não é instrutor")
    void deveRetornar400QuandoUsuarioNaoEhInstrutor() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Inválido");
      createDTO.setLinkMeet("https://meet.google.com/invalid");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenThrow(new IllegalArgumentException("Apenas usuários do tipo INSTRUTOR podem criar workshops"));

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 quando data término antes de data início")
    void deveRetornar400QuandoDataInvalida() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(6, ChronoUnit.DAYS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Inválido");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenThrow(new IllegalArgumentException("Data de término deve ser maior que data de início"));

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 quando título está vazio")
    void deveRetornar400QuandoTituloVazio() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, never()).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 quando data de início não fornecida")
    void deveRetornar400QuandoDataInicioNaoFornecida() throws Exception {
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Sem Data Início");
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, never()).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 quando data de término não fornecida")
    void deveRetornar400QuandoDataTerminoNaoFornecida() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Sem Data Término");
      createDTO.setDataInicio(dataInicio);

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, never()).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 404 quando instrutor não existe")
    void deveRetornar404QuandoInstrutorNaoExiste() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setTitulo("Workshop Instrutor Inexistente");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      when(workshopService.criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail())))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Usuário com ID 99999 não encontrado"));

      mockMvc.perform(post("/api/workshops")
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isNotFound());

      verify(workshopService, times(1)).criarWorkshop(any(WorkshopCreateDTO.class), eq(instrutor.getEmail()));
    }
  }

  @Nested
  @DisplayName("GET /api/workshops - Listar Workshops")
  class ListarWorkshopsTests {

    @Test
    @DisplayName("Deve listar todos os workshops")
    void deveListarTodosWorkshops() throws Exception {
      List<Workshop> workshops = Arrays.asList(workshopAberto, workshopEmAndamento, workshopConcluido);

      when(workshopService.listarTodos())
        .thenReturn(workshops);

      mockMvc.perform(get("/api/workshops"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));

      verify(workshopService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve listar workshops por status ABERTO")
    void deveListarWorkshopsAbertos() throws Exception {
      when(workshopService.listarPorStatus(StatusWorkshop.ABERTO))
        .thenReturn(Arrays.asList(workshopAberto));

      mockMvc.perform(get("/api/workshops")
          .param("status", "ABERTO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("ABERTO"));

      verify(workshopService, times(1)).listarPorStatus(StatusWorkshop.ABERTO);
    }

    @Test
    @DisplayName("Deve listar workshops por instrutor")
    void deveListarWorkshopsPorInstrutor() throws Exception {
      when(workshopService.listarPorInstrutor(instrutor.getId()))
        .thenReturn(Arrays.asList(workshopAberto, workshopEmAndamento, workshopConcluido));

      mockMvc.perform(get("/api/workshops")
          .param("instrutorId", instrutor.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));

      verify(workshopService, times(1)).listarPorInstrutor(instrutor.getId());
    }

    @Test
    @DisplayName("Deve listar workshops abertos (endpoint específico)")
    void deveListarWorkshopsAbertosEndpointEspecifico() throws Exception {
      // ✅ CORREÇÃO: Mock do método correto que o controller chama
      when(workshopService.listarWorkshopsAbertos())  // ← Este é o método correto!
        .thenReturn(Arrays.asList(workshopAberto));

      mockMvc.perform(get("/api/workshops/abertos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].status").value("ABERTO"));

      verify(workshopService, times(1)).listarWorkshopsAbertos();  // ← Verifica método correto
    }

    @Test
    @DisplayName("Deve buscar workshops por termo no título")
    void deveBuscarWorkshopsPorTitulo() throws Exception {
      when(workshopService.buscarPorTitulo("Spring"))
        .thenReturn(Arrays.asList(workshopAberto));

      mockMvc.perform(get("/api/workshops/buscar")
          .param("termo", "Spring"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].titulo").value(containsString("Spring")));

      verify(workshopService, times(1)).buscarPorTitulo("Spring");
    }
  }

  @Nested
  @DisplayName("PATCH /api/workshops/{id} - Atualizar Workshop")
  class AtualizarWorkshopTests {

    @Test
    @DisplayName("Deve atualizar título com sucesso")
    void deveAtualizarTitulo() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setTitulo("Workshop Atualizado");

      // ✅ CORRIGIDO: Workshop completo com todos os campos
      Workshop workshopAtualizado = new Workshop();
      workshopAtualizado.setId(BigInteger.valueOf(1));
      workshopAtualizado.setTitulo("Workshop Atualizado");
      workshopAtualizado.setLinkMeet("https://meet.google.com/abc-defg");
      workshopAtualizado.setStatus(StatusWorkshop.ABERTO);
      workshopAtualizado.setInstrutor(instrutor);
      workshopAtualizado.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      workshopAtualizado.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopAtualizado);

      mockMvc.perform(patch("/api/workshops/{id}", 1)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Workshop Atualizado"));

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve atualizar link do Meet com sucesso")
    void deveAtualizarLinkMeet() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setLinkMeet("https://meet.google.com/atualizado");

      // ✅ CORRIGIDO: Workshop completo
      Workshop workshopAtualizado = new Workshop();
      workshopAtualizado.setId(BigInteger.valueOf(1));
      workshopAtualizado.setTitulo("Spring Boot Avançado");
      workshopAtualizado.setLinkMeet("https://meet.google.com/atualizado");
      workshopAtualizado.setStatus(StatusWorkshop.ABERTO);
      workshopAtualizado.setInstrutor(instrutor);
      workshopAtualizado.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      workshopAtualizado.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopAtualizado);

      mockMvc.perform(patch("/api/workshops/{id}", 1)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.linkMeet").value("https://meet.google.com/atualizado"));

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve mudar status para EM_ANDAMENTO quando data adiantada")
    void deveMudarStatusQuandoDataAdiantada() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setDataInicio(Timestamp.from(Instant.now()));

      // ✅ CORRIGIDO: Workshop completo
      Workshop workshopAtualizado = new Workshop();
      workshopAtualizado.setId(BigInteger.valueOf(1));
      workshopAtualizado.setTitulo("Spring Boot Avançado");
      workshopAtualizado.setLinkMeet("https://meet.google.com/abc-defg");
      workshopAtualizado.setStatus(StatusWorkshop.EM_ANDAMENTO);
      workshopAtualizado.setInstrutor(instrutor);
      workshopAtualizado.setDataInicio(Timestamp.from(Instant.now()));
      workshopAtualizado.setDataTermino(Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS)));

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopAtualizado);

      mockMvc.perform(patch("/api/workshops/{id}", 1)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve concluir workshop quando status EM_ANDAMENTO")
    void deveConcluirWorkshop() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setStatus(StatusWorkshop.CONCLUIDO);

      // ✅ CORRIGIDO: Workshop completo
      Workshop workshopConcluido = new Workshop();
      workshopConcluido.setId(BigInteger.valueOf(2));
      workshopConcluido.setTitulo("Workshop Imediato");
      workshopConcluido.setLinkMeet("https://meet.google.com/now");
      workshopConcluido.setStatus(StatusWorkshop.CONCLUIDO);
      workshopConcluido.setInstrutor(instrutor);
      workshopConcluido.setDataInicio(Timestamp.from(Instant.now()));
      workshopConcluido.setDataTermino(Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS)));

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(2)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopConcluido);

      mockMvc.perform(patch("/api/workshops/{id}", 2)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONCLUIDO"));

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(2)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar iniciar workshop antes da data")
    void deveRetornar400AoIniciarAntesData() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setStatus(StatusWorkshop.EM_ANDAMENTO);

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenThrow(new IllegalArgumentException("Não é possível iniciar workshop antes da data de início prevista"));

      mockMvc.perform(patch("/api/workshops/{id}", 1)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar concluir workshop ABERTO")
    void deveRetornar400AoConcluirWorkshopAberto() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setStatus(StatusWorkshop.CONCLUIDO);

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenThrow(new IllegalArgumentException("Apenas workshops EM_ANDAMENTO podem ser concluídos"));

      mockMvc.perform(patch("/api/workshops/{id}", 1)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }

    @Test
    @DisplayName("Deve atualizar descrição existente")
    void deveAtualizarDescricao() throws Exception {
      DescricaoWorkshopDTO descricaoDTO = new DescricaoWorkshopDTO();
      descricaoDTO.setTema("Novo Tema");
      descricaoDTO.setDescricao("Nova Descrição");

      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setDescricao(descricaoDTO);

      // ✅ CORRIGIDO: Workshop completo
      Workshop workshopAtualizado = new Workshop();
      workshopAtualizado.setId(BigInteger.valueOf(1));
      workshopAtualizado.setTitulo("Spring Boot Avançado");
      workshopAtualizado.setLinkMeet("https://meet.google.com/abc-defg");
      workshopAtualizado.setStatus(StatusWorkshop.ABERTO);
      workshopAtualizado.setInstrutor(instrutor);
      workshopAtualizado.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      workshopAtualizado.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));

      when(workshopService.atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail())))
        .thenReturn(workshopAtualizado);

      mockMvc.perform(patch("/api/workshops/{id}", 1)
          .principal(() -> instrutor.getEmail())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk());

      verify(workshopService, times(1)).atualizarWorkshop(eq(BigInteger.valueOf(1)), any(WorkshopUpdateDTO.class), eq(instrutor.getEmail()));
    }
  }

  @Nested
  @DisplayName("DELETE /api/workshops/{id} - Deletar Workshop")
  class DeletarWorkshopTests {

    @Test
    @DisplayName("Deve deletar workshop com sucesso")
    void deveDeletarWorkshopComSucesso() throws Exception {
      doNothing().when(workshopService).deletarWorkshop(BigInteger.valueOf(1));

      mockMvc.perform(delete("/api/workshops/{id}", 1))
        .andExpect(status().isNoContent());

      verify(workshopService, times(1)).deletarWorkshop(BigInteger.valueOf(1));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar workshop inexistente")
    void deveRetornar404AoDeletarWorkshopInexistente() throws Exception {
      doThrow(new jakarta.persistence.EntityNotFoundException("Workshop com ID 999 não encontrado"))
        .when(workshopService).deletarWorkshop(BigInteger.valueOf(999));

      mockMvc.perform(delete("/api/workshops/{id}", 999))
        .andExpect(status().isNotFound());

      verify(workshopService, times(1)).deletarWorkshop(BigInteger.valueOf(999));
    }
  }

  @Nested
  @DisplayName("GET /api/workshops/instrutor/{id}/count - Contar Workshops")
  class ContarWorkshopsTests {

    @Test
    @DisplayName("Deve contar workshops do instrutor corretamente")
    void deveContarWorkshopsDoInstrutor() throws Exception {
      when(workshopService.contarWorkshopsPorInstrutor(instrutor.getId()))
        .thenReturn(3L);

      mockMvc.perform(get("/api/workshops/instrutor/{id}/count", instrutor.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(3));

      verify(workshopService, times(1)).contarWorkshopsPorInstrutor(instrutor.getId());
    }

    @Test
    @DisplayName("Deve retornar 0 quando instrutor não tem workshops")
    void deveRetornarZeroQuandoSemWorkshops() throws Exception {
      when(workshopService.contarWorkshopsPorInstrutor(instrutor.getId()))
        .thenReturn(0L);

      mockMvc.perform(get("/api/workshops/instrutor/{id}/count", instrutor.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(0));

      verify(workshopService, times(1)).contarWorkshopsPorInstrutor(instrutor.getId());
    }
  }
}
