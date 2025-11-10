package br.com.escoladeti.api_know_hall.inscricao;

import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Inscricao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.exception.DuplicateResourceException;
import br.com.escoladeti.api_know_hall.exception.TokenInsuficienteException;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
import br.com.escoladeti.api_know_hall.exception.ValidationException;
import br.com.escoladeti.api_know_hall.repository.InscricaoRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import br.com.escoladeti.api_know_hall.service.InscricaoService;
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
@DisplayName("Testes Unitários - InscricaoService")
class InscricaoServiceTest {

  @Mock
  private InscricaoRepository inscricaoRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private WorkshopRepository workshopRepository;

  @InjectMocks
  private InscricaoService inscricaoService;

  private Usuario usuario;
  private Usuario instrutor;
  private Workshop workshop;
  private Inscricao inscricao;

  @BeforeEach
  void setUp() {
    // Setup Instrutor
    instrutor = new Usuario();
    instrutor.setId(BigInteger.ONE);
    instrutor.setNome("João Instrutor");
    instrutor.setEmail("joao@email.com");
    instrutor.setTipoUsuario(TipoUsuario.INSTRUTOR);
    instrutor.setStatusUsuario(StatusUsuario.ATIVO);
    instrutor.setQntdToken(100L);

    // Setup Usuário
    usuario = new Usuario();
    usuario.setId(BigInteger.TWO);
    usuario.setNome("Maria Aluna");
    usuario.setEmail("maria@email.com");
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setQntdToken(50L);

    // Setup Workshop
    workshop = new Workshop();
    workshop.setId(BigInteger.valueOf(10));
    workshop.setTitulo("Spring Boot Avançado");
    workshop.setInstrutor(instrutor);
    workshop.setStatus(StatusWorkshop.ABERTO);
    workshop.setCusto(10);
    workshop.setDataInicio(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS))); // Workshop começa em 7 dias
    workshop.setDataTermino(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS)));

    // Setup Inscrição
    inscricao = new Inscricao();
    inscricao.setId(BigInteger.valueOf(100));
    inscricao.setUsuario(usuario);
    inscricao.setWorkshop(workshop);
    inscricao.setStatus(StatusInscricao.INSCRITO);
    inscricao.setDataInscricao(Timestamp.from(Instant.now()));
  }

  @Nested
  @DisplayName("Testes de Inscrição em Workshop")
  class InscricaoTests {

    @Test
    @DisplayName("Deve inscrever usuário com sucesso quando possui tokens suficientes")
    void deveInscreverUsuarioComSucesso() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())).thenReturn(false);
      when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricao);
      when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario).thenReturn(instrutor);

      // Act
      InscricaoResponseDTO resultado = inscricaoService.inscrever(usuario.getEmail(), workshop.getId());

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getUsuarioId()).isEqualTo(usuario.getId());
      assertThat(resultado.getWorkshopId()).isEqualTo(workshop.getId());
      assertThat(resultado.getStatus()).isEqualTo(StatusInscricao.INSCRITO);

      verify(usuarioRepository).findByEmail(usuario.getEmail());
      verify(workshopRepository).findById(workshop.getId());
      verify(inscricaoRepository).existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId());
      verify(inscricaoRepository).save(any(Inscricao.class));
      verify(usuarioRepository, times(2)).save(any(Usuario.class)); // Salva usuário e instrutor
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Email ou senha inválidos");

      verify(usuarioRepository).findByEmail(usuario.getEmail());
      verify(workshopRepository, never()).findById(any());
      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando workshop não encontrado")
    void deveLancarExcecaoQuandoWorkshopNaoEncontrado() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Workshop com ID " + workshop.getId() + " não encontrado");

      verify(usuarioRepository).findByEmail(usuario.getEmail());
      verify(workshopRepository).findById(workshop.getId());
      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário já está inscrito")
    void deveLancarExcecaoQuandoUsuarioJaInscrito() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Usuário já está inscrito neste workshop.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando instrutor tenta se inscrever no próprio workshop")
    void deveLancarExcecaoQuandoInstrutorTentaSeInscrever() {
      // Arrange
      when(usuarioRepository.findByEmail(instrutor.getEmail())).thenReturn(Optional.of(instrutor));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(instrutor.getId(), workshop.getId())).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(instrutor.getEmail(), workshop.getId()))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Instrutor não pode se inscrever em seu próprio workshop.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não possui tokens suficientes")
    void deveLancarExcecaoQuandoTokenInsuficiente() {
      // Arrange
      usuario.setQntdToken(5L);
      workshop.setCusto(10);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(TokenInsuficienteException.class)
        .hasMessage("Usuário não possui tokens suficientes para se inscrever neste workshop.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando workshop não está com status ABERTO")
    void deveLancarExcecaoQuandoWorkshopNaoAberto() {
      // Arrange
      workshop.setStatus(StatusWorkshop.CONCLUIDO);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Inscrições só podem ser feitas em workshops com status ABERTO.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário está inativo")
    void deveLancarExcecaoQuandoUsuarioInativo() {
      // Arrange
      usuario.setStatusUsuario(StatusUsuario.INATIVO);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(UsuarioInativoException.class)
        .hasMessage("Apenas usuários ativos podem se inscrever em workshops.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando workshop já começou")
    void deveLancarExcecaoQuandoWorkshopJaComecou() {
      // Arrange
      workshop.setDataInicio(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS))); // Workshop começou há 1 hora

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.inscrever(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Não é possível se inscrever em um workshop que já começou.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve transferir tokens do usuário para o instrutor ao inscrever")
    void deveTransferirTokensParaInstrutorAoInscrever() {
      // Arrange
      Long tokensUsuarioAntes = 50L;
      Long tokensInstrutorAntes = 100L;
      Integer custoWorkshop = 10;
      
      usuario.setQntdToken(tokensUsuarioAntes);
      instrutor.setQntdToken(tokensInstrutorAntes);
      workshop.setCusto(custoWorkshop);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())).thenReturn(false);
      when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricao);
      when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario).thenReturn(instrutor);

      // Act
      inscricaoService.inscrever(usuario.getEmail(), workshop.getId());

      // Assert
      assertThat(usuario.getQntdToken()).isEqualTo(tokensUsuarioAntes - custoWorkshop); // 50 - 10 = 40
      assertThat(instrutor.getQntdToken()).isEqualTo(tokensInstrutorAntes + custoWorkshop); // 100 + 10 = 110
      
      verify(usuarioRepository, times(2)).save(any(Usuario.class));
    }
  }

  @Nested
  @DisplayName("Testes de Cancelamento de Inscrição")
  class CancelamentoTests {

    @Test
    @DisplayName("Deve cancelar inscrição com sucesso")
    void deveCancelarInscricaoComSucesso() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.of(inscricao));
      when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricao);
      when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario).thenReturn(instrutor);

      // Act
      inscricaoService.cancelarInscricao(usuario.getEmail(), workshop.getId());

      // Assert
      verify(inscricaoRepository).save(any(Inscricao.class));
      verify(usuarioRepository, times(2)).save(any(Usuario.class)); // Salva usuário e instrutor
    }

    @Test
    @DisplayName("Deve lançar exceção quando inscrição não encontrada para cancelamento")
    void deveLancarExcecaoQuandoInscricaoNaoEncontrada() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.cancelarInscricao(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Inscrição não encontrada para este usuário nesse workshop.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando inscrição já está cancelada")
    void deveLancarExcecaoQuandoInscricaoJaCancelada() {
      // Arrange
      inscricao.setStatus(StatusInscricao.CANCELADO);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.of(inscricao));

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.cancelarInscricao(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(ValidationException.class)
        .hasMessage("A inscrição não pode ser cancelada no status atual.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar cancelar inscrição de workshop que já começou")
    void deveLancarExcecaoQuandoTentarCancelarWorkshopQueJaComecou() {
      // Arrange
      workshop.setDataInicio(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS))); // Workshop começou há 1 hora

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.of(inscricao));

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.cancelarInscricao(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Não é possível cancelar inscrição em um workshop que já começou.");

      verify(inscricaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve devolver tokens ao usuário e retirar do instrutor ao cancelar")
    void deveDevolverTokensAoUsuarioERetirarDoInstrutorAoCancelar() {
      // Arrange
      Long tokensUsuarioAntes = 40L;
      Long tokensInstrutorAntes = 110L;
      Integer custoWorkshop = 10;
      
      usuario.setQntdToken(tokensUsuarioAntes);
      instrutor.setQntdToken(tokensInstrutorAntes);
      workshop.setCusto(custoWorkshop);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.of(inscricao));
      when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricao);
      when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario).thenReturn(instrutor);

      // Act
      inscricaoService.cancelarInscricao(usuario.getEmail(), workshop.getId());

      // Assert
      assertThat(usuario.getQntdToken()).isEqualTo(tokensUsuarioAntes + custoWorkshop); // 40 + 10 = 50
      assertThat(instrutor.getQntdToken()).isEqualTo(tokensInstrutorAntes - custoWorkshop); // 110 - 10 = 100
      
      verify(usuarioRepository, times(2)).save(any(Usuario.class));
      verify(inscricaoRepository).save(any(Inscricao.class));
    }
  }

  @Nested
  @DisplayName("Testes de Busca de Inscrição")
  class BuscaTests {

    @Test
    @DisplayName("Deve buscar inscrição com sucesso")
    void deveBuscarInscricaoComSucesso() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.of(inscricao));

      // Act
      InscricaoResponseDTO resultado = inscricaoService.buscarInscricao(usuario.getEmail(), workshop.getId());

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getUsuarioId()).isEqualTo(usuario.getId());
      assertThat(resultado.getWorkshopId()).isEqualTo(workshop.getId());
      assertThat(resultado.getStatus()).isEqualTo(StatusInscricao.INSCRITO);

      verify(usuarioRepository).findByEmail(usuario.getEmail());
      verify(workshopRepository).findById(workshop.getId());
      verify(inscricaoRepository).findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando inscrição não encontrada na busca")
    void deveLancarExcecaoQuandoInscricaoNaoEncontradaNaBusca() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId()))
        .thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.buscarInscricao(usuario.getEmail(), workshop.getId()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Inscrição não encontrada para este usuário nesse workshop.");

      verify(inscricaoRepository).findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId());
    }
  }

  @Nested
  @DisplayName("Testes de Listagem de Inscrições")
  class ListagemTests {

    @Test
    @DisplayName("Deve listar inscrições por usuário com sucesso")
    void deveListarInscricoesPorUsuario() {
      // Arrange
      Inscricao inscricao2 = new Inscricao();
      inscricao2.setId(BigInteger.valueOf(101));
      inscricao2.setUsuario(usuario);
      inscricao2.setWorkshop(workshop);
      inscricao2.setStatus(StatusInscricao.INSCRITO);
      inscricao2.setDataInscricao(Timestamp.from(Instant.now()));

      List<Inscricao> inscricoes = Arrays.asList(inscricao, inscricao2);

      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(inscricaoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(inscricoes));

      // Act
      List<InscricaoResponseDTO> resultado = inscricaoService.listarInscricoesPorUsuario(usuario.getEmail());

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado).hasSize(2);
      assertThat(resultado.get(0).getUsuarioId()).isEqualTo(usuario.getId());
      assertThat(resultado.get(1).getUsuarioId()).isEqualTo(usuario.getId());

      verify(usuarioRepository).findByEmail(usuario.getEmail());
      verify(inscricaoRepository).findByUsuarioId(usuario.getId());
    }

    @Test
    @DisplayName("Deve listar inscrições por workshop com sucesso")
    void deveListarInscricoesPorWorkshop() {
      // Arrange
      Usuario usuario2 = new Usuario();
      usuario2.setId(BigInteger.valueOf(3));
      usuario2.setNome("Pedro");
      usuario2.setEmail("pedro@email.com");

      Inscricao inscricao2 = new Inscricao();
      inscricao2.setId(BigInteger.valueOf(101));
      inscricao2.setUsuario(usuario2);
      inscricao2.setWorkshop(workshop);
      inscricao2.setStatus(StatusInscricao.INSCRITO);
      inscricao2.setDataInscricao(Timestamp.from(Instant.now()));

      List<Inscricao> inscricoes = Arrays.asList(inscricao, inscricao2);

      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByWorkshopId(workshop.getId())).thenReturn(Optional.of(inscricoes));

      // Act
      List<InscricaoResponseDTO> resultado = inscricaoService.listarInscricoesPorWorkshop(workshop.getId());

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado).hasSize(2);
      assertThat(resultado.get(0).getWorkshopId()).isEqualTo(workshop.getId());
      assertThat(resultado.get(1).getWorkshopId()).isEqualTo(workshop.getId());

      verify(workshopRepository).findById(workshop.getId());
      verify(inscricaoRepository).findByWorkshopId(workshop.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há inscrições para o usuário")
    void deveLancarExcecaoQuandoNaoHaInscricoesParaUsuario() {
      // Arrange
      when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
      when(inscricaoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.listarInscricoesPorUsuario(usuario.getEmail()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Nenhuma inscrição encontrada para este usuário.");

      verify(inscricaoRepository).findByUsuarioId(usuario.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há inscrições para o workshop")
    void deveLancarExcecaoQuandoNaoHaInscricoesParaWorkshop() {
      // Arrange
      when(workshopRepository.findById(workshop.getId())).thenReturn(Optional.of(workshop));
      when(inscricaoRepository.findByWorkshopId(workshop.getId())).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.listarInscricoesPorWorkshop(workshop.getId()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Nenhuma inscrição encontrada para este workshop.");

      verify(inscricaoRepository).findByWorkshopId(workshop.getId());
    }
  }

  @Nested
  @DisplayName("Testes de Atualização de Status")
  class AtualizacaoStatusTests {

    @Test
    @DisplayName("Deve atualizar status da inscrição com sucesso")
    void deveAtualizarStatusComSucesso() {
      // Arrange
      when(inscricaoRepository.findById(inscricao.getId())).thenReturn(Optional.of(inscricao));
      when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricao);

      // Act
      InscricaoResponseDTO resultado = inscricaoService.atualizarStatusInscricao(
        inscricao.getId(), 
        StatusInscricao.CANCELADO
      );

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getId()).isEqualTo(inscricao.getId());

      verify(inscricaoRepository).findById(inscricao.getId());
      verify(inscricaoRepository).save(any(Inscricao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando inscrição não encontrada para atualização")
    void deveLancarExcecaoQuandoInscricaoNaoEncontradaParaAtualizacao() {
      // Arrange
      when(inscricaoRepository.findById(inscricao.getId())).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> inscricaoService.atualizarStatusInscricao(
        inscricao.getId(), 
        StatusInscricao.CANCELADO
      ))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Inscrição não encontrada.");

      verify(inscricaoRepository).findById(inscricao.getId());
      verify(inscricaoRepository, never()).save(any());
    }
  }
}
