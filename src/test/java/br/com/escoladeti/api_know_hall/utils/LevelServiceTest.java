package br.com.escoladeti.api_know_hall.utils;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.MotivoTransacao;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.service.HistoricoTransacaoService;
import br.com.escoladeti.api_know_hall.util.LevelService;
import br.com.escoladeti.api_know_hall.util.LevelConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - LevelService")
class LevelServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private HistoricoTransacaoService historicoTransacaoService;

  @InjectMocks
  private LevelService levelService;

  private Usuario usuario;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(BigInteger.ONE);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash123");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setQntdToken(1000L);
    usuario.setQntdXp(1000L);
    usuario.setNivel(2);
  }

  @Nested
  @DisplayName("Testes de processLevelChange - Level Up")
  class ProcessLevelUpTests {

    @Test
    @DisplayName("Deve subir de nível 1 para 2 e conceder 100 tokens")
    void deveSubirDeNivel1Para2EConceder100Tokens() {
      // Arrange
      usuario.setNivel(1);
      usuario.setQntdXp(0L);
      usuario.setQntdToken(500L);

      long oldXp = 0L;
      long newXp = 150L; // Nível 2 começa em 100 XP, 150 está no nível 2

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(2);
      assertThat(usuario.getQntdToken()).isEqualTo(600L); // 500 + 100 de recompensa

      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(100L),
        eq(MotivoTransacao.LEVEL_UP),
        contains("nível 2")
      );
    }

    @Test
    @DisplayName("Deve subir de nível 4 para 5 e conceder 250 tokens com badge Explorador")
    void deveSubirDeNivel4Para5ComBadgeExplorador() {
      // Arrange
      usuario.setNivel(4);
      usuario.setQntdXp(830L);
      usuario.setQntdToken(1000L);

      long oldXp = 830L;
      long newXp = 1532L; // Nível 5 começa em 1532 XP

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(5);
      assertThat(usuario.getQntdToken()).isEqualTo(1250L); // 1000 + 250 de recompensa

      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(250L),
        eq(MotivoTransacao.LEVEL_UP),
        contains("nível 5")
      );
      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        anyLong(),
        eq(MotivoTransacao.LEVEL_UP),
        contains("Explorador(a)")
      );
    }

    @Test
    @DisplayName("Deve subir múltiplos níveis de uma vez (1 para 3)")
    void deveSubirMultiplosNiveisDeUmaVez() {
      // Arrange
      usuario.setNivel(1);
      usuario.setQntdXp(0L);
      usuario.setQntdToken(500L);

      long oldXp = 0L;
      long newXp = 364L; // Nível 3 começa em 364 XP

      levelService.processLevelChange(usuario, oldXp, newXp);

      assertThat(usuario.getNivel()).isEqualTo(3);
      assertThat(usuario.getQntdToken()).isEqualTo(750L); // 500 + 100 + 150 = 750

      verify(historicoTransacaoService, times(2)).registrarTransacao(
        eq(usuario),
        anyLong(),
        eq(MotivoTransacao.LEVEL_UP),
        anyString()
      );
      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(100L),
        eq(MotivoTransacao.LEVEL_UP),
        contains("nível 2")
      );
      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(150L),
        eq(MotivoTransacao.LEVEL_UP),
        contains("nível 3")
      );
    }

    @Test
    @DisplayName("Deve conceder recompensas corretas para nível 10 (Veterano)")
    void deveConcederRecompensasCorretasParaNivel10() {
      // Arrange
      usuario.setNivel(9);
      usuario.setQntdXp(7008L);
      usuario.setQntdToken(200L);

      long oldXp = 7008L;
      long newXp = 9125L; // Nível 10 começa em 9125 XP

      levelService.processLevelChange(usuario, oldXp, newXp);

      assertThat(usuario.getNivel()).isEqualTo(10);
      assertThat(usuario.getQntdToken()).isEqualTo(700L); // 200 + 500 de recompensa

      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(500L),
        eq(MotivoTransacao.LEVEL_UP),
        argThat(desc -> desc.contains("nível 10") && desc.contains("Veterano(a)"))
      );
    }

    @Test
    @DisplayName("Deve conceder recompensas corretas para nível 30 (Sábio)")
    void deveConcederRecompensasCorretasParaNivel30() {
      usuario.setNivel(29);
      usuario.setQntdXp(160532L);
      usuario.setQntdToken(500L);

      long oldXp = 160532L;
      long newXp = 180339L; // Nível 30 começa em 180339 XP

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(30);
      assertThat(usuario.getQntdToken()).isEqualTo(2000L); // 500 + 1500 de recompensa

      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(1500L),
        eq(MotivoTransacao.LEVEL_UP),
        argThat(desc -> desc.contains("nível 30") && desc.contains("Sábio(a)"))
      );
    }
  }

  @Nested
  @DisplayName("Testes de processLevelChange - Level Down")
  class ProcessLevelDownTests {

    @Test
    @DisplayName("Deve perder nível de 2 para 1 sem remover tokens")
    void devePerderNivelDe2Para1SemRemoverTokens() {
      // Arrange
      usuario.setNivel(2);
      usuario.setQntdXp(100L);
      usuario.setQntdToken(50L);

      long oldXp = 100L;
      long newXp = 50L; // Menos que 100, volta para nível 1

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(1);
      assertThat(usuario.getQntdToken()).isEqualTo(50L); // Tokens não são removidos

      verify(historicoTransacaoService, never()).registrarTransacao(
        any(),
        anyLong(),
        eq(MotivoTransacao.LEVEL_UP),
        anyString()
      );
    }

    @Test
    @DisplayName("Deve perder múltiplos níveis sem remover tokens")
    void devePerderMultiplosNiveisSemRemoverTokens() {
      // Arrange
      usuario.setNivel(5);
      usuario.setQntdXp(1532L);
      usuario.setQntdToken(100L);

      long oldXp = 1532L;
      long newXp = 200L; // Volta para nível 2

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(2);
      assertThat(usuario.getQntdToken()).isEqualTo(100L); // Tokens não são removidos

      verify(historicoTransacaoService, never()).registrarTransacao(
        any(),
        anyLong(),
        eq(MotivoTransacao.LEVEL_UP),
        anyString()
      );
    }

    @Test
    @DisplayName("Deve perder nível de 10 para 9 sem perder badge")
    void devePerderNivelDe10Para9SemPerderBadge() {
      // Arrange
      usuario.setNivel(10);
      usuario.setQntdXp(9125L);
      usuario.setQntdToken(200L);

      long oldXp = 9125L;
      long newXp = 8000L; // Volta para nível 9

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(9);
      assertThat(usuario.getQntdToken()).isEqualTo(200L); // Tokens mantidos
    }
  }

  @Nested
  @DisplayName("Testes de processLevelChange - Sem Mudança de Nível")
  class ProcessNoLevelChangeTests {

    @Test
    @DisplayName("Não deve alterar nível quando XP aumenta mas permanece no mesmo nível")
    void naoDeveAlterarNivelQuandoXpAumentaMasPermaneceNoMesmoNivel() {
      // Arrange
      usuario.setNivel(2);
      usuario.setQntdXp(150L);
      usuario.setQntdToken(50L);

      long oldXp = 150L;
      long newXp = 200L; // Ainda nível 2 (que vai até 364 - 1)

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(2);
      assertThat(usuario.getQntdToken()).isEqualTo(50L);

      verify(historicoTransacaoService, never()).registrarTransacao(
        any(),
        anyLong(),
        any(),
        anyString()
      );
    }

    @Test
    @DisplayName("Deve atualizar nível quando estava null")
    void deveAtualizarNivelQuandoEstavaNull() {
      // Arrange
      usuario.setNivel(null);
      usuario.setQntdXp(150L);
      usuario.setQntdToken(50L);

      long oldXp = 150L;
      long newXp = 200L; // Ainda nível 2

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(2);
      assertThat(usuario.getQntdToken()).isEqualTo(50L);

      verify(historicoTransacaoService, never()).registrarTransacao(
        any(),
        anyLong(),
        any(),
        anyString()
      );
    }
  }

  @Nested
  @DisplayName("Testes de initializeLevel")
  class InitializeLevelTests {

    @Test
    @DisplayName("Deve inicializar usuário novo com nível 1 e 0 XP")
    void deveInicializarUsuarioNovoComNivel1E0Xp() {
      // Arrange
      Usuario novoUsuario = new Usuario();
      novoUsuario.setQntdXp(null);
      novoUsuario.setNivel(null);

      // Act
      levelService.initializeLevel(novoUsuario);

      // Assert
      assertThat(novoUsuario.getQntdXp()).isEqualTo(0L);
      assertThat(novoUsuario.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve calcular nível correto para usuário com XP existente")
    void deveCalcularNivelCorretoParaUsuarioComXpExistente() {
      // Arrange
      Usuario usuarioComXp = new Usuario();
      usuarioComXp.setQntdXp(1532L); // XP para nível 5
      usuarioComXp.setNivel(null);

      // Act
      levelService.initializeLevel(usuarioComXp);

      // Assert
      assertThat(usuarioComXp.getNivel()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("Testes de recalculateLevel")
  class RecalculateLevelTests {

    @Test
    @DisplayName("Deve recalcular e corrigir nível incorreto")
    void deveRecalcularECorrigirNivelIncorreto() {
      // Arrange
      usuario.setQntdXp(1532L); // XP para nível 5
      usuario.setNivel(3); // Nível incorreto

      when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

      // Act
      levelService.recalculateLevel(usuario);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(5);
      verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Não deve salvar quando nível já está correto")
    void naoDeveSalvarQuandoNivelJaEstaCorreto() {
      // Arrange
      usuario.setQntdXp(150L); // XP para nível 2
      usuario.setNivel(2); // Nível correto

      // Act
      levelService.recalculateLevel(usuario);

      // Assert
      verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve calcular nível quando está null")
    void deveCalcularNivelQuandoEstaNullNoRecalculate() {
      // Arrange
      usuario.setQntdXp(830L); // XP para nível 4
      usuario.setNivel(null);

      when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

      // Act
      levelService.recalculateLevel(usuario);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(4);
      verify(usuarioRepository).save(usuario);
    }
  }

  @Nested
  @DisplayName("Testes de getLevelInfo")
  class GetLevelInfoTests {

    @Test
    @DisplayName("Deve retornar informações corretas para nível 2")
    void deveRetornarInformacoesCorretasParaNivel2() {
      // Arrange
      usuario.setQntdXp(150L); // Nível 2
      usuario.setNivel(2);

      // Act
      LevelService.LevelInfo info = levelService.getLevelInfo(usuario);

      // Assert
      assertThat(info.getNivel()).isEqualTo(2);
      assertThat(info.getXpAtual()).isEqualTo(150L);
      assertThat(info.getXpProximoNivel()).isEqualTo(214L); // 364 - 150
      assertThat(info.getProgressoPercentual()).isBetween(15.0, 20.0); // ~18.94%
      assertThat(info.getBadge()).isNull(); // Nível 2 não tem badge
    }

    @Test
    @DisplayName("Deve retornar badge para nível 5 (Explorador)")
    void deveRetornarBadgeParaNivel5() {
      // Arrange
      usuario.setQntdXp(2000L); // Nível 5
      usuario.setNivel(5);

      // Act
      LevelService.LevelInfo info = levelService.getLevelInfo(usuario);

      // Assert
      assertThat(info.getNivel()).isEqualTo(5);
      assertThat(info.getBadge()).isEqualTo("Explorador(a)");
    }

    @Test
    @DisplayName("Deve retornar progresso 0% no início do nível")
    void deveRetornarProgresso0NoInicioDoNivel() {
      // Arrange
      usuario.setQntdXp(100L); // Exatamente no início do nível 2
      usuario.setNivel(2);

      // Act
      LevelService.LevelInfo info = levelService.getLevelInfo(usuario);

      // Assert
      assertThat(info.getProgressoPercentual()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve retornar progresso 100% no nível máximo")
    void deveRetornarProgresso100NoNivelMaximo() {
      // Arrange
      usuario.setQntdXp(200000L); // Muito acima do nível 30
      usuario.setNivel(30);

      // Act
      LevelService.LevelInfo info = levelService.getLevelInfo(usuario);

      // Assert
      assertThat(info.getNivel()).isEqualTo(30);
      assertThat(info.getXpProximoNivel()).isEqualTo(0L);
      assertThat(info.getProgressoPercentual()).isEqualTo(100.0);
      assertThat(info.getBadge()).isEqualTo("Sábio(a)");
    }

    @Test
    @DisplayName("Deve lidar com XP null")
    void deveLidarComXpNull() {
      // Arrange
      usuario.setQntdXp(null);
      usuario.setNivel(1);

      // Act
      LevelService.LevelInfo info = levelService.getLevelInfo(usuario);

      // Assert
      assertThat(info.getNivel()).isEqualTo(1);
      assertThat(info.getXpAtual()).isEqualTo(0L);
      assertThat(info.getBadge()).isEqualTo("Iniciante");
    }
  }

  @Nested
  @DisplayName("Testes de Recompensas Específicas")
  class RecompensasEspecificasTests {

    @Test
    @DisplayName("Não deve conceder tokens para nível 1 (sem recompensa)")
    void naoDeveConcederTokensParaNivel1() {
      // Arrange
      usuario.setNivel(null);
      usuario.setQntdXp(0L);
      usuario.setQntdToken(0L);

      // Forçando inicialização do nível
      long oldXp = 0L;
      long newXp = 0L;

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(1);
      assertThat(usuario.getQntdToken()).isEqualTo(0L);

      verify(historicoTransacaoService, never()).registrarTransacao(
        any(),
        anyLong(),
        eq(MotivoTransacao.LEVEL_UP),
        anyString()
      );
    }

    @Test
    @DisplayName("Deve conceder recompensas para todos os badges")
    void deveConcederRecompensasParaTodosOsBadges() {
      // Testa subindo do nível 1 até o 5 (inclui badge de nível 5)
      usuario.setNivel(1);
      usuario.setQntdXp(0L);
      usuario.setQntdToken(0L);

      // Subir até XP >= 1532 (nível 5)
      levelService.processLevelChange(usuario, 0L, 1532L);

      // Espera-se que uma transação com 250 (badge Explorador) tenha sido registrada
      verify(historicoTransacaoService, atLeastOnce()).registrarTransacao(
        eq(usuario),
        eq(250L),
        eq(MotivoTransacao.LEVEL_UP),
        contains("Explorador(a)")
      );
    }

    @Test
    @DisplayName("Deve conceder soma correta de recompensas ao subir vários níveis")
    void deveConcederSomaCorretaDeRecompensasAoSubirVariosNiveis() {
      // Arrange
      usuario.setNivel(1);
      usuario.setQntdXp(0L);
      usuario.setQntdToken(0L);

      // Subir do nível 1 ao 4 (até XP >= 830)
      long oldXp = 0L;
      long newXp = 830L; // Nível 4 começa em 830

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(4);
      // Recompensas: 100 (nível 2) + 150 (nível 3) + 200 (nível 4) = 450
      assertThat(usuario.getQntdToken()).isEqualTo(450L);

      verify(historicoTransacaoService, times(3)).registrarTransacao(
        eq(usuario),
        anyLong(),
        eq(MotivoTransacao.LEVEL_UP),
        anyString()
      );
    }
  }

  @Nested
  @DisplayName("Testes de Casos Extremos")
  class CasosExtremosTests {

    @Test
    @DisplayName("Deve lidar com XP negativo")
    void deveLidarComXpNegativo() {
      // Arrange
      usuario.setNivel(2);
      usuario.setQntdXp(100L);
      usuario.setQntdToken(50L);

      long oldXp = 100L;
      long newXp = -100L; // XP negativo

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(1); // Volta para nível mínimo
      assertThat(usuario.getQntdToken()).isEqualTo(50L); // Não perde tokens
    }

    @Test
    @DisplayName("Deve parar no nível 30 mesmo com muito XP")
    void devePararNoNivel30MesmoComMuitoXp() {
      // Arrange
      usuario.setNivel(29);
      usuario.setQntdXp(160532L);
      usuario.setQntdToken(500L);

      long oldXp = 160532L;
      long newXp = 1000000L; // XP absurdo

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(30); // Nível máximo
      assertThat(usuario.getQntdToken()).isEqualTo(2000L); // 500 + 1500
    }

    @Test
    @DisplayName("Deve processar corretamente XP que permanece em 0")
    void deveProcessarCorretamenteXpQuePermaneceEm0() {
      // Arrange
      usuario.setNivel(1);
      usuario.setQntdXp(0L);
      usuario.setQntdToken(100L);

      long oldXp = 0L;
      long newXp = 0L;

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      assertThat(usuario.getNivel()).isEqualTo(1);
      assertThat(usuario.getQntdToken()).isEqualTo(100L);

      verify(historicoTransacaoService, never()).registrarTransacao(
        any(),
        anyLong(),
        any(),
        anyString()
      );
    }
  }

  @Nested
  @DisplayName("Testes de Integração com LevelConfiguration")
  class IntegracaoLevelConfigurationTests {

    @Test
    @DisplayName("Deve usar valores corretos da configuração para nível 15")
    void deveUsarValoresCorretosDaConfiguracaoParaNivel15() {
      // Arrange
      usuario.setNivel(14);
      usuario.setQntdXp(21537L);
      usuario.setQntdToken(300L);

      long oldXp = 21537L;
      long newXp = 25837L; // Nível 15 começa em 25837 XP

      // Act
      levelService.processLevelChange(usuario, oldXp, newXp);

      // Assert
      LevelConfiguration.LevelData levelData = LevelConfiguration.getLevelData(15);

      assertThat(usuario.getNivel()).isEqualTo(15);
      assertThat(usuario.getQntdToken()).isEqualTo(1050L); // 300 + 750
      assertThat(levelData.getTokensRecompensa()).isEqualTo(750L);
      assertThat(levelData.getBadge()).isEqualTo("Especialista");

      verify(historicoTransacaoService).registrarTransacao(
        eq(usuario),
        eq(750L),
        eq(MotivoTransacao.LEVEL_UP),
        contains("Especialista")
      );
    }

    @Test
    @DisplayName("Deve usar valores corretos da configuração para todos os níveis com badge")
    void deveUsarValoresCorretosDaConfiguracaoParaTodosOsNiveisComBadge() {
      // Nível 1: Iniciante
      assertThat(LevelConfiguration.getLevelBadge(1)).isEqualTo("Iniciante");
      assertThat(LevelConfiguration.getLevelReward(1)).isEqualTo(0L);

      // Nível 5: Explorador(a)
      assertThat(LevelConfiguration.getLevelBadge(5)).isEqualTo("Explorador(a)");
      assertThat(LevelConfiguration.getLevelReward(5)).isEqualTo(250L);

      assertThat(LevelConfiguration.getLevelBadge(10)).isEqualTo("Veterano(a)");
      assertThat(LevelConfiguration.getLevelReward(10)).isEqualTo(500L);

      // Nível 15: Especialista
      assertThat(LevelConfiguration.getLevelBadge(15)).isEqualTo("Especialista");
      assertThat(LevelConfiguration.getLevelReward(15)).isEqualTo(750L);

      // Nível 20: Mestre
      assertThat(LevelConfiguration.getLevelBadge(20)).isEqualTo("Mestre");
      assertThat(LevelConfiguration.getLevelReward(20)).isEqualTo(1000L);

      // Nível 25: Lenda
      assertThat(LevelConfiguration.getLevelBadge(25)).isEqualTo("Lenda");
      assertThat(LevelConfiguration.getLevelReward(25)).isEqualTo(1250L);

      // Nível 30: Sábio(a)
      assertThat(LevelConfiguration.getLevelBadge(30)).isEqualTo("Sábio(a)");
      assertThat(LevelConfiguration.getLevelReward(30)).isEqualTo(1500L);
    }
  }
}
