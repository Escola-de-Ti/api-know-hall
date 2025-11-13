package br.com.escoladeti.api_know_hall.workshops;

import br.com.escoladeti.api_know_hall.dto.workshop.DescricaoWorkshopDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopCreateDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.workshop.DescricaoWorkshop;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.ImagemTipo;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.repository.DescricaoWorkshopRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import br.com.escoladeti.api_know_hall.service.WorkshopService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - WorkshopService")
class WorkshopServiceTest {

  @Mock
  private WorkshopRepository workshopRepository;

  @Mock
  private DescricaoWorkshopRepository descricaoWorkshopRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private WorkshopService workshopService;

  private Usuario instrutor;
  private Usuario usuarioComum;
  private WorkshopCreateDTO workshopCreateDTO;
  private Workshop workshop;
  private DescricaoWorkshop descricaoWorkshop;

  @BeforeEach
  void setUp() {
    // Setup Instrutor
    instrutor = new Usuario();
    instrutor.setId(BigInteger.ONE);
    instrutor.setNome("João");
    instrutor.setEmail("joao@email.com");
    instrutor.setTipoUsuario(TipoUsuario.INSTRUTOR);
    instrutor.setStatusUsuario(StatusUsuario.ATIVO);

    // Setup Usuário Comum
    usuarioComum = new Usuario();
    usuarioComum.setId(BigInteger.TWO);
    usuarioComum.setNome("Maria");
    usuarioComum.setEmail("maria@email.com");
    usuarioComum.setTipoUsuario(TipoUsuario.ALUNO);
    usuarioComum.setStatusUsuario(StatusUsuario.ATIVO);

    // Setup DTO
    Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
    Timestamp dataTermino = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

    DescricaoWorkshopDTO descricaoDTO = new DescricaoWorkshopDTO();
    descricaoDTO.setTema("Backend Java");
    descricaoDTO.setDescricao("Workshop sobre Spring Boot");

    workshopCreateDTO = new WorkshopCreateDTO();
    workshopCreateDTO.setTitulo("Spring Boot Avançado");
    workshopCreateDTO.setLinkMeet("https://meet.google.com/abc-defg");
    workshopCreateDTO.setDataInicio(dataInicio);
    workshopCreateDTO.setDataTermino(dataTermino);
    workshopCreateDTO.setDescricao(descricaoDTO);
    workshopCreateDTO.setCusto(100);

    // Setup Workshop
    workshop = new Workshop();
    workshop.setId(BigInteger.ONE);
    workshop.setTitulo(workshopCreateDTO.getTitulo());
    workshop.setLinkMeet(workshopCreateDTO.getLinkMeet());
    workshop.setStatus(StatusWorkshop.ABERTO);
    workshop.setDataInicio(dataInicio);
    workshop.setDataTermino(dataTermino);
    workshop.setInstrutor(instrutor);

    // Setup DescricaoWorkshop
    descricaoWorkshop = new DescricaoWorkshop();
    descricaoWorkshop.setId(BigInteger.ONE);
    descricaoWorkshop.setTema("Backend Java");
    descricaoWorkshop.setDescricao("Workshop sobre Spring Boot");
    descricaoWorkshop.setWorkshop(workshop);

  }

  @Nested
  @DisplayName("Testes de Criação de Workshop")
  class CriarWorkshopTests {

    @Test
    @DisplayName("Deve criar workshop com sucesso quando usuário é INSTRUTOR e data futura")
    void deveCriarWorkshopComSucesso() {
      // Arrange
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);
      when(descricaoWorkshopRepository.save(any(DescricaoWorkshop.class))).thenReturn(descricaoWorkshop);

      // Act
      Workshop resultado = workshopService.criarWorkshop(workshopCreateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getTitulo()).isEqualTo("Spring Boot Avançado");
      assertThat(resultado.getStatus()).isEqualTo(StatusWorkshop.ABERTO);
      assertThat(resultado.getInstrutor()).isEqualTo(instrutor);

      verify(usuarioRepository).findByEmail(instrutor.getEmail());
      verify(workshopRepository).save(any(Workshop.class));
      verify(descricaoWorkshopRepository).save(any(DescricaoWorkshop.class));
    }

    @Test
    @DisplayName("Deve criar workshop com status EM_ANDAMENTO quando data de início é hoje")
    void deveCriarWorkshopEmAndamentoQuandoDataHoje() {
      // Arrange
      Timestamp hoje = Timestamp.from(Instant.now());
      workshopCreateDTO.setDataInicio(hoje);
      workshopCreateDTO.setDataTermino(Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS)));

      workshop.setDataInicio(hoje);
      workshop.setStatus(StatusWorkshop.EM_ANDAMENTO);

      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);
      when(descricaoWorkshopRepository.save(any(DescricaoWorkshop.class))).thenReturn(descricaoWorkshop);

      // Act
      Workshop resultado = workshopService.criarWorkshop(workshopCreateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getStatus()).isEqualTo(StatusWorkshop.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve criar workshop sem descrição quando descrição não fornecida")
    void deveCriarWorkshopSemDescricao() {
      // Arrange
      workshopCreateDTO.setDescricao(null);

      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.criarWorkshop(workshopCreateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado).isNotNull();
      verify(descricaoWorkshopRepository, never()).save(any(DescricaoWorkshop.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
      // Arrange
      when(usuarioRepository.findByEmail(usuarioComum.getEmail())).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> workshopService.criarWorkshop(workshopCreateDTO, usuarioComum.getEmail()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Usuário não encontrado");

      verify(workshopRepository, never()).save(any(Workshop.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é INSTRUTOR")
    void deveLancarExcecaoQuandoUsuarioNaoEhInstrutor() {
      // Arrange
      when(usuarioRepository.findByEmail(usuarioComum.getEmail())).thenReturn(Optional.of(usuarioComum));

      // Act & Assert
      assertThatThrownBy(() -> workshopService.criarWorkshop(workshopCreateDTO, usuarioComum.getEmail()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Apenas usuários do tipo INSTRUTOR podem criar workshops");

      verify(workshopRepository, never()).save(any(Workshop.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data término antes de data início")
    void deveLancarExcecaoQuandoDataTerminoAntesDeInicio() {
      // Arrange
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(6, ChronoUnit.DAYS)); // antes

      workshopCreateDTO.setDataInicio(dataInicio);
      workshopCreateDTO.setDataTermino(dataTermino);

      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));

      // Act & Assert
      assertThatThrownBy(() -> workshopService.criarWorkshop(workshopCreateDTO, instrutor.getEmail()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Data de término deve ser maior que data de início");

      verify(workshopRepository, never()).save(any(Workshop.class));
    }
  }

  @Nested
  @DisplayName("Testes de Busca de Workshop")
  class BuscarWorkshopTests {

    @Test
    @DisplayName("Deve buscar workshop por ID com sucesso")
    void deveBuscarWorkshopPorId() {
      // Arrange
      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));

      // Act
      Workshop resultado = workshopService.buscarPorId(BigInteger.ONE);

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getId()).isEqualTo(BigInteger.ONE);
      assertThat(resultado.getTitulo()).isEqualTo("Spring Boot Avançado");

      verify(workshopRepository).findById(BigInteger.ONE);
    }

    @Test
    @DisplayName("Deve lançar exceção quando workshop não encontrado")
    void deveLancarExcecaoQuandoWorkshopNaoEncontrado() {
      // Arrange
      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> workshopService.buscarPorId(BigInteger.ONE))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Workshop não encontrado");
    }

    @Test
    @DisplayName("Deve listar todos os workshops")
    void deveListarTodosWorkshops() {
      // Arrange
      List<Workshop> workshops = Arrays.asList(workshop, new Workshop());
      when(workshopRepository.findAll()).thenReturn(workshops);

      // Act
      List<Workshop> resultado = workshopService.listarTodos();

      // Assert
      assertThat(resultado).hasSize(2);
      verify(workshopRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar workshops por instrutor")
    void deveListarWorkshopsPorInstrutor() {
      // Arrange
      List<Workshop> workshops = Arrays.asList(workshop);
      when(workshopRepository.findByInstrutorId(1L)).thenReturn(workshops);

      // Act
      List<Workshop> resultado = workshopService.listarPorInstrutor(BigInteger.ONE);

      // Assert
      assertThat(resultado).hasSize(1);
      assertThat(resultado.get(0).getInstrutor()).isEqualTo(instrutor);
      verify(workshopRepository).findByInstrutorId(1L);
    }

    @Test
    @DisplayName("Deve listar workshops por status")
    void deveListarWorkshopsPorStatus() {
      // Arrange
      List<Workshop> workshops = Arrays.asList(workshop);
      when(workshopRepository.findByStatus("ABERTO")).thenReturn(workshops);

      // Act
      List<Workshop> resultado = workshopService.listarPorStatus(StatusWorkshop.ABERTO);

      // Assert
      assertThat(resultado).hasSize(1);
      assertThat(resultado.get(0).getStatus()).isEqualTo(StatusWorkshop.ABERTO);
      verify(workshopRepository).findByStatus("ABERTO");
    }

    @Test
    @DisplayName("Deve buscar workshops por título")
    void deveBuscarWorkshopsPorTitulo() {
      // Arrange
      List<Workshop> workshops = Arrays.asList(workshop);
      when(workshopRepository.findByTituloContaining("Spring")).thenReturn(workshops);

      // Act
      List<Workshop> resultado = workshopService.buscarPorTitulo("Spring");

      // Assert
      assertThat(resultado).hasSize(1);
      assertThat(resultado.get(0).getTitulo()).contains("Spring");
      verify(workshopRepository).findByTituloContaining("Spring");
    }

    @Test
    @DisplayName("Deve contar workshops por instrutor")
    void deveContarWorkshopsPorInstrutor() {
      // Arrange
      when(workshopRepository.countByInstrutorId(1L)).thenReturn(5L);

      // Act
      Long resultado = workshopService.contarWorkshopsPorInstrutor(BigInteger.ONE);

      // Assert
      assertThat(resultado).isEqualTo(5L);
      verify(workshopRepository).countByInstrutorId(1L);
    }
  }

  @Nested
  @DisplayName("Testes de Atualização de Workshop")
  class AtualizarWorkshopTests {

    private WorkshopUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
      updateDTO = new WorkshopUpdateDTO();
    }

    @Test
    @DisplayName("Deve atualizar título com sucesso")
    void deveAtualizarTitulo() {
      // Arrange
      updateDTO.setTitulo("Novo Título");
      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getTitulo()).isEqualTo("Novo Título");
      verify(workshopRepository).save(workshop);
    }

    @Test
    @DisplayName("Deve atualizar link do Meet com sucesso")
    void deveAtualizarLinkMeet() {
      // Arrange
      updateDTO.setLinkMeet("https://meet.google.com/novo-link");
      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getLinkMeet()).isEqualTo("https://meet.google.com/novo-link");
      verify(workshopRepository).save(workshop);
    }

    @Test
    @DisplayName("Deve atualizar datas com sucesso")
    void deveAtualizarDatas() {
      // Arrange
      Timestamp novaDataInicio = Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS));
      Timestamp novaDataTermino = Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

      updateDTO.setDataInicio(novaDataInicio);
      updateDTO.setDataTermino(novaDataTermino);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getDataInicio()).isEqualTo(novaDataInicio);
      assertThat(resultado.getDataTermino()).isEqualTo(novaDataTermino);
      verify(workshopRepository).save(workshop);
    }

    @Test
    @DisplayName("Deve mudar status para EM_ANDAMENTO quando data início é adiantada para hoje")
    void deveMudarStatusQuandoDataAdiantada() {
      // Arrange
      Timestamp hoje = Timestamp.from(Instant.now());
      updateDTO.setDataInicio(hoje);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getStatus()).isEqualTo(StatusWorkshop.EM_ANDAMENTO);
      verify(workshopRepository).save(workshop);
    }

    @Test
    @DisplayName("Deve atualizar descrição existente")
    void deveAtualizarDescricaoExistente() {
      // Arrange
      workshop.setDescricao(descricaoWorkshop);

      DescricaoWorkshopDTO novaDescricaoDTO = new DescricaoWorkshopDTO();
      novaDescricaoDTO.setTema("Novo Tema");
      novaDescricaoDTO.setDescricao("Nova Descrição");
      updateDTO.setDescricao(novaDescricaoDTO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(descricaoWorkshopRepository.save(any(DescricaoWorkshop.class))).thenReturn(descricaoWorkshop);
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      verify(descricaoWorkshopRepository).save(any(DescricaoWorkshop.class));
      verify(workshopRepository).save(workshop);
    }

    @Test
    @DisplayName("Deve criar descrição quando não existia")
    void deveCriarDescricaoQuandoNaoExistia() {
      // Arrange
      DescricaoWorkshopDTO novaDescricaoDTO = new DescricaoWorkshopDTO();
      novaDescricaoDTO.setTema("Novo Tema");
      novaDescricaoDTO.setDescricao("Nova Descrição");
      updateDTO.setDescricao(novaDescricaoDTO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(descricaoWorkshopRepository.save(any(DescricaoWorkshop.class))).thenReturn(descricaoWorkshop);
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      verify(descricaoWorkshopRepository).save(any(DescricaoWorkshop.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data término antes de data início na atualização")
    void deveLancarExcecaoDataTerminoInvalida() {
      // Arrange
      Timestamp dataInicio = Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS));
      Timestamp dataTermino = Timestamp.from(Instant.now().plus(9, ChronoUnit.DAYS));

      updateDTO.setDataInicio(dataInicio);
      updateDTO.setDataTermino(dataTermino);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));

      // Act & Assert
      assertThatThrownBy(() -> workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Data de término deve ser maior que data de início");
    }
  }

  @Nested
  @DisplayName("Testes de Validação de Status")
  class ValidarStatusTests {

    private WorkshopUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
      updateDTO = new WorkshopUpdateDTO();
    }

    @Test
    @DisplayName("Deve iniciar workshop quando data início é hoje")
    void deveIniciarWorkshopQuandoDataHoje() {
      // Arrange
      workshop.setDataInicio(Timestamp.from(Instant.now()));
      updateDTO.setStatus(StatusWorkshop.EM_ANDAMENTO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getStatus()).isEqualTo(StatusWorkshop.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar iniciar workshop antes da data prevista")
    void deveLancarExcecaoAoIniciarAntesDataPrevista() {
      // Arrange
      workshop.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
      updateDTO.setStatus(StatusWorkshop.EM_ANDAMENTO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));

      // Act & Assert
      assertThatThrownBy(() -> workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Não é possível iniciar workshop antes da data de início prevista");
    }

    @Test
    @DisplayName("Deve concluir workshop quando status é EM_ANDAMENTO")
    void deveConcluirWorkshop() {
      // Arrange
      workshop.setStatus(StatusWorkshop.EM_ANDAMENTO);
      updateDTO.setStatus(StatusWorkshop.CONCLUIDO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

      // Act
      Workshop resultado = workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail());

      // Assert
      assertThat(resultado.getStatus()).isEqualTo(StatusWorkshop.CONCLUIDO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar concluir workshop ABERTO")
    void deveLancarExcecaoAoConcluirWorkshopAberto() {
      // Arrange
      workshop.setStatus(StatusWorkshop.ABERTO);
      updateDTO.setStatus(StatusWorkshop.CONCLUIDO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));

      // Act & Assert
      assertThatThrownBy(() -> workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Apenas workshops EM_ANDAMENTO podem ser concluídos");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar reabrir workshop CONCLUÍDO")
    void deveLancarExcecaoAoReabrirWorkshopConcluido() {
      // Arrange
      workshop.setStatus(StatusWorkshop.CONCLUIDO);
      updateDTO.setStatus(StatusWorkshop.ABERTO);

      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));

      // Act & Assert
      assertThatThrownBy(() -> workshopService.atualizarWorkshop(BigInteger.ONE, updateDTO, instrutor.getEmail()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Não é possível reabrir um workshop já concluído");
    }
  }

  @Nested
  @DisplayName("Testes de Deleção de Workshop")
  class DeletarWorkshopTests {

    @Test
    @DisplayName("Deve deletar workshop com sucesso")
    void deveDeletarWorkshopComSucesso() {
      // Arrange
      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.of(workshop));
      doNothing().when(workshopRepository).delete(workshop);

      // Act
      workshopService.deletarWorkshop(BigInteger.ONE);

      // Assert
      verify(workshopRepository).findById(BigInteger.ONE);
      verify(workshopRepository).delete(workshop);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar workshop inexistente")
    void deveLancarExcecaoAoDeletarWorkshopInexistente() {
      // Arrange
      when(workshopRepository.findById(BigInteger.ONE)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> workshopService.deletarWorkshop(BigInteger.ONE))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Workshop não encontrado");

      verify(workshopRepository, never()).delete(any(Workshop.class));
    }
  }

  @Test
  void atualizarImagemWorkshop_sucesso() {
    BigInteger workshopId = BigInteger.ONE;
    Imagem imagem = new Imagem(workshopId, "img.png", "url", "idImg", "path", ImagemTipo.WORKSHOP);

    DescricaoWorkshop descricaoWorkshop = new DescricaoWorkshop();
    Workshop workshop = new Workshop();
    workshop.setId(workshopId);
    workshop.setDescricao(descricaoWorkshop);

    when(workshopRepository.findById(workshopId)).thenReturn(Optional.of(workshop));
    when(workshopRepository.save(any(Workshop.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertThatCode(() -> workshopService.atualizarImagemWorkshop(imagem, workshopId))
      .doesNotThrowAnyException();

    assertThat(descricaoWorkshop.getImagemWorkshop()).isEqualTo(imagem);
    verify(workshopRepository).save(workshop);
  }

  @Test
  void atualizarImagemWorkshop_workshopNaoEncontrado_lancaExcecao() {
    BigInteger workshopId = BigInteger.ONE;
    Imagem imagem = new Imagem(workshopId, "img.png", "url", "idImg", "path", ImagemTipo.WORKSHOP);

    when(workshopRepository.findById(workshopId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> workshopService.atualizarImagemWorkshop(imagem, workshopId))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("Workshop não encontrado");

    verify(workshopRepository, never()).save(any());
  }

  @Test
  void atualizarImagemWorkshop_semDescricao_lancaExcecao() {
    BigInteger workshopId = BigInteger.ONE;
    Imagem imagem = new Imagem(workshopId, "img.png", "url", "idImg", "path", ImagemTipo.WORKSHOP);

    Workshop workshop = new Workshop();
    workshop.setId(workshopId);
    workshop.setDescricao(null); // <--- sem descrição

    when(workshopRepository.findById(workshopId)).thenReturn(Optional.of(workshop));

    assertThatThrownBy(() -> workshopService.atualizarImagemWorkshop(imagem, workshopId))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("Descrição do workshop não encontrada");

    verify(workshopRepository, never()).save(any());
  }


}
