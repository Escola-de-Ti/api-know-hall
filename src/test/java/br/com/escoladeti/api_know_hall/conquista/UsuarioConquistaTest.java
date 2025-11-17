package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioConquista Entity Tests - Cobertura de Branch")
class UsuarioConquistaTest {

  private UsuarioConquista usuarioConquista;
  private Usuario usuario;
  private Conquista conquista;
  private ConquistaTier conquistaTier;

  @BeforeEach
  void setUp() {
    // Setup Usuario
    usuario = new Usuario();
    usuario.setId(BigInteger.valueOf(1));
    usuario.setEmail("test@test.com");
    usuario.setNome("Test User");
    usuario.setCpf("12345678901");
    usuario.setSenhaHash("hash");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario.setConquistas(new ArrayList<>());

    // Setup Conquista
    conquista = new Conquista();
    conquista.setId(BigInteger.valueOf(1));
    conquista.setNome("Conquistador");
    conquista.setDescricao("Conquista de teste");
    conquista.setTipoConquista(TipoConquista.CERTIFICADO);
    conquista.setCampoValidacao("posts");
    conquista.setUsuariosConquistas(new ArrayList<>());

    // Setup ConquistaTier
    conquistaTier = new ConquistaTier();
    conquistaTier.setId(BigInteger.valueOf(1));
    conquistaTier.setConquista(conquista);
    conquistaTier.setTier(TierConquista.BRONZE);
    conquistaTier.setQuantidadeNecessaria(10);
    conquistaTier.setDescricaoTier("Bronze tier");

    // Setup UsuarioConquista
    usuarioConquista = new UsuarioConquista();
  }

  @Test
  @DisplayName("Deve criar UsuarioConquista com construtor sem argumentos")
  void deveCriarUsuarioConquistaComConstrutorSemArgumentos() {
    // Act
    UsuarioConquista uc = new UsuarioConquista();

    // Assert
    assertNotNull(uc);
    assertNull(uc.getId());
    assertNull(uc.getUsuario());
    assertNull(uc.getConquista());
    assertNull(uc.getConquistaTier());
    assertNull(uc.getDataObtencao());
    assertNull(uc.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve criar UsuarioConquista com construtor com todos argumentos")
  void deveCriarUsuarioConquistaComConstrutorComArgumentos() {
    // Arrange
    LocalDateTime dataObtencao = LocalDateTime.now();

    // Act
    UsuarioConquista uc = new UsuarioConquista(
      BigInteger.valueOf(1),
      usuario,
      conquista,
      conquistaTier,
      dataObtencao,
      5
    );

    // Assert
    assertNotNull(uc);
    assertEquals(BigInteger.valueOf(1), uc.getId());
    assertEquals(usuario, uc.getUsuario());
    assertEquals(conquista, uc.getConquista());
    assertEquals(conquistaTier, uc.getConquistaTier());
    assertEquals(dataObtencao, uc.getDataObtencao());
    assertEquals(5, uc.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve chamar onCreate e setar dataObtencao quando null")
  void deveSetarDataObtencaoQuandoNullNoOnCreate() {
    // Arrange
    usuarioConquista.setDataObtencao(null);

    // Act
    usuarioConquista.onCreate();

    // Assert
    assertNotNull(usuarioConquista.getDataObtencao());
    assertTrue(usuarioConquista.getDataObtencao().isBefore(LocalDateTime.now().plusSeconds(1)));
    assertTrue(usuarioConquista.getDataObtencao().isAfter(LocalDateTime.now().minusSeconds(5)));
  }

  @Test
  @DisplayName("Deve manter dataObtencao quando já definida no onCreate")
  void deveManterDataObtencaoQuandoJaDefinidaNoOnCreate() {
    // Arrange
    LocalDateTime dataEspecifica = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    usuarioConquista.setDataObtencao(dataEspecifica);

    // Act
    usuarioConquista.onCreate();

    // Assert
    assertEquals(dataEspecifica, usuarioConquista.getDataObtencao());
  }

  @Test
  @DisplayName("Deve setar conquista e adicionar ao relacionamento bidirecional quando conquista não é null e não contém a usuarioConquista")
  void deveSetarConquistaEAdicionarAoRelacionamentoBidirecional() {
    // Arrange
    Conquista novaConquista = new Conquista();
    novaConquista.setId(BigInteger.valueOf(2));
    novaConquista.setNome("Nova Conquista");
    novaConquista.setUsuariosConquistas(new ArrayList<>());

    // Act
    usuarioConquista.setConquista(novaConquista);

    // Assert
    assertEquals(novaConquista, usuarioConquista.getConquista());
    assertTrue(novaConquista.getUsuariosConquistas().contains(usuarioConquista));
    assertEquals(1, novaConquista.getUsuariosConquistas().size());
  }

  @Test
  @DisplayName("Deve setar conquista e não adicionar duplicado quando já existe no relacionamento")
  void deveSetarConquistaSemAdicionarDuplicado() {
    // Arrange
    conquista.getUsuariosConquistas().add(usuarioConquista);

    // Act
    usuarioConquista.setConquista(conquista);

    // Assert
    assertEquals(conquista, usuarioConquista.getConquista());
    assertEquals(1, conquista.getUsuariosConquistas().size());
  }

  @Test
  @DisplayName("Deve setar conquista como null")
  void deveSetarConquistaComoNull() {
    // Arrange
    usuarioConquista.setConquista(conquista);

    // Act
    usuarioConquista.setConquista(null);

    // Assert
    assertNull(usuarioConquista.getConquista());
  }

  @Test
  @DisplayName("Deve setar conquistaTier e automaticamente setar conquista quando conquistaTier não é null")
  void deveSetarConquistaTierEAutomaticamenteSetarConquista() {
    // Act
    usuarioConquista.setConquistaTier(conquistaTier);

    // Assert
    assertEquals(conquistaTier, usuarioConquista.getConquistaTier());
    assertEquals(conquista, usuarioConquista.getConquista());
  }

  @Test
  @DisplayName("Deve setar conquistaTier como null")
  void deveSetarConquistaTierComoNull() {
    // Arrange
    usuarioConquista.setConquistaTier(conquistaTier);

    // Act
    usuarioConquista.setConquistaTier(null);

    // Assert
    assertNull(usuarioConquista.getConquistaTier());
  }

  @Test
  @DisplayName("Deve setar conquistaTier com tier diferente")
  void deveSetarConquistaTierComTierDiferente() {
    // Arrange
    ConquistaTier tierPrata = new ConquistaTier();
    tierPrata.setId(BigInteger.valueOf(2));
    tierPrata.setConquista(conquista);
    tierPrata.setTier(TierConquista.PRATA);
    tierPrata.setQuantidadeNecessaria(50);

    // Act
    usuarioConquista.setConquistaTier(tierPrata);

    // Assert
    assertEquals(tierPrata, usuarioConquista.getConquistaTier());
    assertEquals(TierConquista.PRATA, usuarioConquista.getConquistaTier().getTier());
  }

  @Test
  @DisplayName("Deve testar setters e getters para todos os campos")
  void deveTestarSettersEGetters() {
    // Arrange
    LocalDateTime dataObtencao = LocalDateTime.now();

    // Act
    usuarioConquista.setId(BigInteger.valueOf(99));
    usuarioConquista.setUsuario(usuario);
    usuarioConquista.setDataObtencao(dataObtencao);
    usuarioConquista.setProgressoAtual(75);

    // Assert
    assertEquals(BigInteger.valueOf(99), usuarioConquista.getId());
    assertEquals(usuario, usuarioConquista.getUsuario());
    assertEquals(dataObtencao, usuarioConquista.getDataObtencao());
    assertEquals(75, usuarioConquista.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve setar progressoAtual como zero")
  void deveSetarProgressoAtualComoZero() {
    // Act
    usuarioConquista.setProgressoAtual(0);

    // Assert
    assertEquals(0, usuarioConquista.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve setar progressoAtual como null")
  void deveSetarProgressoAtualComoNull() {
    // Arrange
    usuarioConquista.setProgressoAtual(50);

    // Act
    usuarioConquista.setProgressoAtual(null);

    // Assert
    assertNull(usuarioConquista.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve setar progressoAtual com valor alto")
  void deveSetarProgressoAtualComValorAlto() {
    // Act
    usuarioConquista.setProgressoAtual(9999);

    // Assert
    assertEquals(9999, usuarioConquista.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve manter relacionamento bidirecional ao adicionar multiplas conquistas")
  void deveManterRelacionamentoBidirecionalComMultiplasConquistas() {
    // Arrange
    UsuarioConquista uc1 = new UsuarioConquista();
    UsuarioConquista uc2 = new UsuarioConquista();
    UsuarioConquista uc3 = new UsuarioConquista();

    // Act
    uc1.setConquista(conquista);
    uc2.setConquista(conquista);
    uc3.setConquista(conquista);

    // Assert
    assertEquals(3, conquista.getUsuariosConquistas().size());
    assertTrue(conquista.getUsuariosConquistas().contains(uc1));
    assertTrue(conquista.getUsuariosConquistas().contains(uc2));
    assertTrue(conquista.getUsuariosConquistas().contains(uc3));
  }

  @Test
  @DisplayName("Deve setar conquistaTier e atualizar conquista mesmo com conquista prévia")
  void deveAtualizarConquistaAoSetarConquistaTier() {
    // Arrange
    Conquista outraConquista = new Conquista();
    outraConquista.setId(BigInteger.valueOf(99));
    outraConquista.setNome("Outra Conquista");
    usuarioConquista.setConquista(outraConquista);

    // Act
    usuarioConquista.setConquistaTier(conquistaTier);

    // Assert
    assertEquals(conquistaTier, usuarioConquista.getConquistaTier());
    assertEquals(conquista, usuarioConquista.getConquista());
    assertNotEquals(outraConquista, usuarioConquista.getConquista());
  }

  @Test
  @DisplayName("Deve criar instância completa de UsuarioConquista")
  void deveCriarInstanciaCompletaDeUsuarioConquista() {
    // Arrange
    LocalDateTime dataObtencao = LocalDateTime.of(2024, 6, 15, 14, 30);

    // Act
    usuarioConquista.setId(BigInteger.valueOf(1));
    usuarioConquista.setUsuario(usuario);
    usuarioConquista.setConquistaTier(conquistaTier);
    usuarioConquista.setDataObtencao(dataObtencao);
    usuarioConquista.setProgressoAtual(10);

    // Assert
    assertEquals(BigInteger.valueOf(1), usuarioConquista.getId());
    assertEquals(usuario, usuarioConquista.getUsuario());
    assertEquals(conquista, usuarioConquista.getConquista());
    assertEquals(conquistaTier, usuarioConquista.getConquistaTier());
    assertEquals(dataObtencao, usuarioConquista.getDataObtencao());
    assertEquals(10, usuarioConquista.getProgressoAtual());
  }

  @Test
  @DisplayName("Deve testar todos os tiers possíveis")
  void deveTestarTodosTiersPossiveis() {
    // Arrange & Act & Assert
    ConquistaTier tierBronze = new ConquistaTier();
    tierBronze.setTier(TierConquista.BRONZE);
    tierBronze.setConquista(conquista);
    usuarioConquista.setConquistaTier(tierBronze);
    assertEquals(TierConquista.BRONZE, usuarioConquista.getConquistaTier().getTier());

    ConquistaTier tierPrata = new ConquistaTier();
    tierPrata.setTier(TierConquista.PRATA);
    tierPrata.setConquista(conquista);
    usuarioConquista.setConquistaTier(tierPrata);
    assertEquals(TierConquista.PRATA, usuarioConquista.getConquistaTier().getTier());

    ConquistaTier tierOuro = new ConquistaTier();
    tierOuro.setTier(TierConquista.OURO);
    tierOuro.setConquista(conquista);
    usuarioConquista.setConquistaTier(tierOuro);
    assertEquals(TierConquista.OURO, usuarioConquista.getConquistaTier().getTier());

    ConquistaTier tierPlatina = new ConquistaTier();
    tierPlatina.setTier(TierConquista.PLATINA);
    tierPlatina.setConquista(conquista);
    usuarioConquista.setConquistaTier(tierPlatina);
    assertEquals(TierConquista.PLATINA, usuarioConquista.getConquistaTier().getTier());

    ConquistaTier tierDiamante = new ConquistaTier();
    tierDiamante.setTier(TierConquista.DIAMANTE);
    tierDiamante.setConquista(conquista);
    usuarioConquista.setConquistaTier(tierDiamante);
    assertEquals(TierConquista.DIAMANTE, usuarioConquista.getConquistaTier().getTier());
  }

  @Test
  @DisplayName("Deve chamar onCreate multiplas vezes e manter primeira data")
  void deveChamarOnCreateMultiplasVezes() {
    // Arrange
    usuarioConquista.setDataObtencao(null);

    // Act
    usuarioConquista.onCreate();
    LocalDateTime primeiraData = usuarioConquista.getDataObtencao();
    usuarioConquista.onCreate();
    LocalDateTime segundaData = usuarioConquista.getDataObtencao();

    // Assert
    assertNotNull(primeiraData);
    assertEquals(primeiraData, segundaData);
  }
}
