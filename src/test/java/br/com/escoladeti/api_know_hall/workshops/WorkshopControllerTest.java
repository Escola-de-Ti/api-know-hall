package br.com.escoladeti.api_know_hall.workshops;

import br.com.escoladeti.api_know_hall.dto.workshop.DescricaoWorkshopDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopCreateDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - WorkshopController")
class WorkshopControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WorkshopRepository workshopRepository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  private Usuario instrutor;
  private Usuario usuarioComum;

  @BeforeEach
  void setUp() {
    workshopRepository.deleteAll();
    usuarioRepository.deleteAll();

    instrutor = new Usuario();
    instrutor.setNome("João");
    instrutor.setEmail("joao.instrutor@email.com");
    instrutor.setCpf("12345678901");
    instrutor.setSenhaHash("$2a$10$hash");
    instrutor.setTipoUsuario(TipoUsuario.INSTRUTOR);
    instrutor.setStatusUsuario(StatusUsuario.ATIVO);
    instrutor = usuarioRepository.save(instrutor);

    usuarioComum = new Usuario();
    usuarioComum.setNome("Maria");
    usuarioComum.setEmail("maria.comum@email.com");
    usuarioComum.setCpf("98765432109");
    usuarioComum.setSenhaHash("$2a$10$hash");
    usuarioComum.setTipoUsuario(TipoUsuario.ALUNO);
    usuarioComum.setStatusUsuario(StatusUsuario.ATIVO);
    usuarioComum = usuarioRepository.save(usuarioComum);
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
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Spring Boot Avançado");
      createDTO.setLinkMeet("https://meet.google.com/abc-defg");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);
      createDTO.setDescricao(descricaoDTO);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.titulo").value("Spring Boot Avançado"))
        .andExpect(jsonPath("$.linkMeet").value("https://meet.google.com/abc-defg"))
        .andExpect(jsonPath("$.status").value("ABERTO")) // ✅ Status determinado automaticamente
        .andExpect(jsonPath("$.instrutorId").value(instrutor.getId().intValue()))
        .andExpect(jsonPath("$.instrutorNome").value(containsString("João")))
        .andExpect(jsonPath("$.descricao.tema").value("Backend Java"))
        .andExpect(jsonPath("$.descricao.descricao").value("Workshop sobre Spring Boot"))
        .andExpect(jsonPath("$.dataInicio").exists())
        .andExpect(jsonPath("$.dataTermino").exists());
    }

    @Test
    @DisplayName("Deve criar workshop com status EM_ANDAMENTO quando data é hoje")
    void deveCriarWorkshopEmAndamento() throws Exception {
      Timestamp hoje = Timestamp.from(Instant.now());
      Timestamp fim = Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Workshop Imediato");
      createDTO.setLinkMeet("https://meet.google.com/now");
      createDTO.setDataInicio(hoje);
      createDTO.setDataTermino(fim);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titulo").value("Workshop Imediato"))
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO")); // ✅ Status automático baseado na data
    }

    @Test
    @DisplayName("Deve criar workshop com status EM_ANDAMENTO quando data no passado")
    void deveCriarWorkshopComDataPassada() throws Exception {
      Timestamp passado = Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS));
      Timestamp fim = Timestamp.from(Instant.now().plus(3, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Workshop Iniciado");
      createDTO.setLinkMeet("https://meet.google.com/started");
      createDTO.setDataInicio(passado);
      createDTO.setDataTermino(fim);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("Deve criar workshop sem descrição")
    void deveCriarWorkshopSemDescricao() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Workshop Sem Descrição");
      createDTO.setLinkMeet("https://meet.google.com/no-desc");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titulo").value("Workshop Sem Descrição"))
        .andExpect(jsonPath("$.descricao").doesNotExist());
    }

    @Test
    @DisplayName("Deve retornar 400 quando usuário não é instrutor")
    void deveRetornar400QuandoUsuarioNaoEhInstrutor() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(usuarioComum.getId());
      createDTO.setTitulo("Workshop Inválido");
      createDTO.setLinkMeet("https://meet.google.com/invalid");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando data término antes de data início")
    void deveRetornar400QuandoDataInvalida() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(6, ChronoUnit.DAYS)); // antes

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Workshop Inválido");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando título está vazio")
    void deveRetornar400QuandoTituloVazio() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando data de início não fornecida")
    void deveRetornar400QuandoDataInicioNaoFornecida() throws Exception {
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Workshop Sem Data Início");
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando data de término não fornecida")
    void deveRetornar400QuandoDataTerminoNaoFornecida() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(instrutor.getId());
      createDTO.setTitulo("Workshop Sem Data Término");
      createDTO.setDataInicio(dataInicio);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 quando instrutor não existe")
    void deveRetornar404QuandoInstrutorNaoExiste() throws Exception {
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      WorkshopCreateDTO createDTO = new WorkshopCreateDTO();
      createDTO.setInstrutorId(java.math.BigInteger.valueOf(99999)); // ID inexistente
      createDTO.setTitulo("Workshop Instrutor Inexistente");
      createDTO.setDataInicio(dataInicio);
      createDTO.setDataTermino(dataTermino);

      mockMvc.perform(post("/api/workshops")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/workshops - Listar Workshops")
  class ListarWorkshopsTests {

    @BeforeEach
    void criarWorkshops() {
      Timestamp dataFutura = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataFim = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      Workshop w1 = new Workshop();
      w1.setTitulo("Workshop 1");
      w1.setStatus(StatusWorkshop.ABERTO);
      w1.setInstrutor(instrutor);
      w1.setDataInicio(dataFutura);
      w1.setDataTermino(dataFim);
      workshopRepository.save(w1);

      Workshop w2 = new Workshop();
      w2.setTitulo("Workshop 2");
      w2.setStatus(StatusWorkshop.EM_ANDAMENTO);
      w2.setInstrutor(instrutor);
      w2.setDataInicio(Timestamp.from(Instant.now()));
      w2.setDataTermino(Timestamp.from(Instant.now().plus(2, ChronoUnit.HOURS)));
      workshopRepository.save(w2);

      Workshop w3 = new Workshop();
      w3.setTitulo("Workshop 3");
      w3.setStatus(StatusWorkshop.CONCLUIDO);
      w3.setInstrutor(instrutor);
      w3.setDataInicio(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
      w3.setDataTermino(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS)));
      workshopRepository.save(w3);
    }

    @Test
    @DisplayName("Deve listar todos os workshops")
    void deveListarTodosWorkshops() throws Exception {
      mockMvc.perform(get("/api/workshops"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Deve listar workshops por status ABERTO")
    void deveListarWorkshopsAbertos() throws Exception {
      mockMvc.perform(get("/api/workshops")
          .param("status", "ABERTO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].status").value("ABERTO"));
    }

    @Test
    @DisplayName("Deve listar workshops por instrutor")
    void deveListarWorkshopsPorInstrutor() throws Exception {
      mockMvc.perform(get("/api/workshops")
          .param("instrutorId", instrutor.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].instrutorId").value(instrutor.getId().intValue()));
    }

    @Test
    @DisplayName("Deve listar workshops abertos (endpoint específico)")
    void deveListarWorkshopsAbertosEndpointEspecifico() throws Exception {
      mockMvc.perform(get("/api/workshops/abertos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Deve buscar workshops por termo no título")
    void deveBuscarWorkshopsPorTitulo() throws Exception {
      mockMvc.perform(get("/api/workshops/buscar")
          .param("termo", "Workshop 1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].titulo").value(containsString("Workshop 1")));
    }
  }

  @Nested
  @DisplayName("GET /api/workshops/{id} - Buscar Workshop por ID")
  class BuscarWorkshopTests {

    @Test
    @DisplayName("Deve buscar workshop por ID com sucesso")
    void deveBuscarWorkshopPorId() throws Exception {
      Workshop workshop = new Workshop();
      workshop.setTitulo("Workshop Teste");
      workshop.setStatus(StatusWorkshop.ABERTO);
      workshop.setInstrutor(instrutor);
      workshop.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      workshop.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));
      workshop = workshopRepository.save(workshop);

      mockMvc.perform(get("/api/workshops/{id}", workshop.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(workshop.getId().intValue()))
        .andExpect(jsonPath("$.titulo").value("Workshop Teste"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando workshop não encontrado")
    void deveRetornar404QuandoWorkshopNaoEncontrado() throws Exception {
      mockMvc.perform(get("/api/workshops/{id}", 999))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PATCH /api/workshops/{id} - Atualizar Workshop")
  class AtualizarWorkshopTests {

    private Workshop workshop;

    @BeforeEach
    void criarWorkshop() {
      workshop = new Workshop();
      workshop.setTitulo("Workshop Original");
      workshop.setLinkMeet("https://meet.google.com/original");
      workshop.setStatus(StatusWorkshop.ABERTO);
      workshop.setInstrutor(instrutor);
      workshop.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      workshop.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));
      workshop = workshopRepository.save(workshop);
    }

    @Test
    @DisplayName("Deve atualizar título com sucesso")
    void deveAtualizarTitulo() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setTitulo("Workshop Atualizado");

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Workshop Atualizado"));
    }

    @Test
    @DisplayName("Deve atualizar link do Meet com sucesso")
    void deveAtualizarLinkMeet() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setLinkMeet("https://meet.google.com/atualizado");

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.linkMeet").value("https://meet.google.com/atualizado"));
    }

    @Test
    @DisplayName("Deve mudar status para EM_ANDAMENTO quando data adiantada")
    void deveMudarStatusQuandoDataAdiantada() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setDataInicio(Timestamp.from(Instant.now()));

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("Deve concluir workshop quando status EM_ANDAMENTO")
    void deveConcluirWorkshop() throws Exception {
      workshop.setStatus(StatusWorkshop.EM_ANDAMENTO);
      workshop.setDataInicio(Timestamp.from(Instant.now()));
      workshopRepository.save(workshop);

      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setStatus(StatusWorkshop.CONCLUIDO);

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar iniciar workshop antes da data")
    void deveRetornar400AoIniciarAntesData() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setStatus(StatusWorkshop.EM_ANDAMENTO);

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar concluir workshop ABERTO")
    void deveRetornar400AoConcluirWorkshopAberto() throws Exception {
      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setStatus(StatusWorkshop.CONCLUIDO);

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve atualizar descrição existente")
    void deveAtualizarDescricao() throws Exception {
      DescricaoWorkshopDTO descricaoDTO = new DescricaoWorkshopDTO();
      descricaoDTO.setTema("Novo Tema");
      descricaoDTO.setDescricao("Nova Descrição");

      WorkshopUpdateDTO updateDTO = new WorkshopUpdateDTO();
      updateDTO.setDescricao(descricaoDTO);

      mockMvc.perform(patch("/api/workshops/{id}", workshop.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.descricao.tema").value("Novo Tema"))
        .andExpect(jsonPath("$.descricao.descricao").value("Nova Descrição"));
    }
  }

  @Nested
  @DisplayName("DELETE /api/workshops/{id} - Deletar Workshop")
  class DeletarWorkshopTests {

    @Test
    @DisplayName("Deve deletar workshop com sucesso")
    void deveDeletarWorkshopComSucesso() throws Exception {
      Workshop workshop = new Workshop();
      workshop.setTitulo("Workshop Para Deletar");
      workshop.setStatus(StatusWorkshop.ABERTO);
      workshop.setInstrutor(instrutor);
      workshop.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      workshop.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));
      workshop = workshopRepository.save(workshop);

      mockMvc.perform(delete("/api/workshops/{id}", workshop.getId()))
        .andExpect(status().isNoContent());

      mockMvc.perform(get("/api/workshops/{id}", workshop.getId()))
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar workshop inexistente")
    void deveRetornar404AoDeletarWorkshopInexistente() throws Exception {
      mockMvc.perform(delete("/api/workshops/{id}", 999))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/workshops/instrutor/{id}/count - Contar Workshops")
  class ContarWorkshopsTests {

    @Test
    @DisplayName("Deve contar workshops do instrutor corretamente")
    void deveContarWorkshopsDoInstrutor() throws Exception {
      for (int i = 0; i < 3; i++) {
        Workshop w = new Workshop();
        w.setTitulo("Workshop " + i);
        w.setStatus(StatusWorkshop.ABERTO);
        w.setInstrutor(instrutor);
        w.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
        w.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));
        workshopRepository.save(w);
      }

      mockMvc.perform(get("/api/workshops/instrutor/{id}/count", instrutor.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(3));
    }

    @Test
    @DisplayName("Deve retornar 0 quando instrutor não tem workshops")
    void deveRetornarZeroQuandoSemWorkshops() throws Exception {
      mockMvc.perform(get("/api/workshops/instrutor/{id}/count", instrutor.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(0));
    }
  }
}
