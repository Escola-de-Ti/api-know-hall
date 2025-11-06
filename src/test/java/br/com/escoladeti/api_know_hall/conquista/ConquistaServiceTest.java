package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.dto.conquista.ConquistaProgressoDTO;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.conquista.*;
import br.com.escoladeti.api_know_hall.enums.*;
import br.com.escoladeti.api_know_hall.repository.*;
import br.com.escoladeti.api_know_hall.service.ConquistaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ConquistaServiceTest {

  @Mock
  private ConquistaRepository conquistaRepository;

  @Mock
  private ConquistaTierRepository conquistaTierRepository;

  @Mock
  private UsuarioConquistaRepository usuarioConquistaRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private ConquistaService conquistaService;

  private Usuario usuario;
  private Conquista conquista;
  private ConquistaTier tierBronze;
  private ConquistaTier tierPrata;
  private ConquistaTier tierOuro;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(BigInteger.valueOf(1));
    usuario.setEmail("test@test.com");
    usuario.setNome("Test User");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);

    conquista = new Conquista();
    conquista.setId(BigInteger.valueOf(1));
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Participe de eventos");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");

    tierBronze = new ConquistaTier();
    tierBronze.setId(BigInteger.valueOf(1));
    tierBronze.setConquista(conquista);
    tierBronze.setTier(TierConquista.BRONZE);
    tierBronze.setQuantidadeNecessaria(10);

    tierPrata = new ConquistaTier();
    tierPrata.setId(BigInteger.valueOf(2));
    tierPrata.setConquista(conquista);
    tierPrata.setTier(TierConquista.PRATA);
    tierPrata.setQuantidadeNecessaria(25);

    tierOuro = new ConquistaTier();
    tierOuro.setId(BigInteger.valueOf(3));
    tierOuro.setConquista(conquista);
    tierOuro.setTier(TierConquista.OURO);
    tierOuro.setQuantidadeNecessaria(50);

    conquista.setTiers(Arrays.asList(tierBronze, tierPrata, tierOuro));
  }

  @Test
  void listarTodasConquistas_ShouldReturnAllConquistas() {
    List<Conquista> conquistas = Arrays.asList(conquista);
    when(conquistaRepository.findAll()).thenReturn(conquistas);

    List<Conquista> result = conquistaService.listarTodasConquistas();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(conquista.getNome(), result.get(0).getNome());
    verify(conquistaRepository, times(1)).findAll();
  }

  @Test
  void listarConquistasPorTipo_WithInsignia_ShouldReturnInsignias() {
    List<Conquista> conquistas = Arrays.asList(conquista);
    when(conquistaRepository.findByTipoConquista(TipoConquista.INSIGNIA)).thenReturn(conquistas);

    List<Conquista> result = conquistaService.listarConquistasPorTipo(TipoConquista.INSIGNIA);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(TipoConquista.INSIGNIA, result.get(0).getTipoConquista());
    verify(conquistaRepository, times(1)).findByTipoConquista(TipoConquista.INSIGNIA);
  }

  @Test
  void listarConquistasPorTipo_WithCertificado_ShouldReturnCertificados() {
    Conquista certificado = new Conquista();
    certificado.setTipoConquista(TipoConquista.CERTIFICADO);
    List<Conquista> conquistas = Arrays.asList(certificado);

    when(conquistaRepository.findByTipoConquista(TipoConquista.CERTIFICADO)).thenReturn(conquistas);

    List<Conquista> result = conquistaService.listarConquistasPorTipo(TipoConquista.CERTIFICADO);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(TipoConquista.CERTIFICADO, result.get(0).getTipoConquista());
    verify(conquistaRepository, times(1)).findByTipoConquista(TipoConquista.CERTIFICADO);
  }

  @Test
  void listarConquistasPorCampo_ShouldReturnFilteredConquistas() {
    List<Conquista> conquistas = Arrays.asList(conquista);
    when(conquistaRepository.findByCampoValidacao("participacoes")).thenReturn(conquistas);

    List<Conquista> result = conquistaService.listarConquistasPorCampo("participacoes");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("participacoes", result.get(0).getCampoValidacao());
    verify(conquistaRepository, times(1)).findByCampoValidacao("participacoes");
  }

  @Test
  void buscarConquistaPorId_WithValidId_ShouldReturnConquista() {
    when(conquistaRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(conquista));

    Conquista result = conquistaService.buscarConquistaPorId(BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(conquista.getNome(), result.getNome());
    verify(conquistaRepository, times(1)).findById(BigInteger.valueOf(1));
  }

  @Test
  void buscarConquistaPorId_WithInvalidId_ShouldThrowException() {
    when(conquistaRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> conquistaService.buscarConquistaPorId(BigInteger.valueOf(999))
    );

    assertTrue(exception.getMessage().contains("Conquista não encontrada"));
    verify(conquistaRepository, times(1)).findById(BigInteger.valueOf(999));
  }

  @Test
  void verificarEConcederConquistas_ShouldGrantBronzeTier() {
    when(usuarioRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(usuario));
    when(conquistaRepository.findByCampoValidacao("participacoes"))
      .thenReturn(Arrays.asList(conquista));
    when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaTierId(any(), any()))
      .thenReturn(false);
    when(usuarioConquistaRepository.save(any())).thenReturn(new UsuarioConquista());

    conquistaService.verificarEConcederConquistas(BigInteger.valueOf(1), "participacoes", 15);

    verify(usuarioConquistaRepository, times(1)).save(any(UsuarioConquista.class));
  }

  @Test
  void verificarEConcederConquistas_ShouldGrantMultipleTiers() {
    when(usuarioRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(usuario));
    when(conquistaRepository.findByCampoValidacao("participacoes"))
      .thenReturn(Arrays.asList(conquista));
    when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaTierId(any(), any()))
      .thenReturn(false);
    when(usuarioConquistaRepository.save(any())).thenReturn(new UsuarioConquista());

    conquistaService.verificarEConcederConquistas(BigInteger.valueOf(1), "participacoes", 30);

    verify(usuarioConquistaRepository, times(2)).save(any(UsuarioConquista.class));
  }

  @Test
  void verificarEConcederConquistas_WhenAlreadyHasTier_ShouldNotGrantAgain() {
    when(usuarioRepository.findById(BigInteger.valueOf(1))).thenReturn(Optional.of(usuario));
    when(conquistaRepository.findByCampoValidacao("participacoes"))
      .thenReturn(Arrays.asList(conquista));
    when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaTierId(any(), any()))
      .thenReturn(true);

    conquistaService.verificarEConcederConquistas(BigInteger.valueOf(1), "participacoes", 15);

    verify(usuarioConquistaRepository, never()).save(any(UsuarioConquista.class));
  }

  @Test
  void verificarEConcederConquistas_WithInvalidUser_ShouldThrowException() {
    when(usuarioRepository.findById(BigInteger.valueOf(999))).thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> conquistaService.verificarEConcederConquistas(BigInteger.valueOf(999), "participacoes", 15)
    );

    assertTrue(exception.getMessage().contains("Usuário não encontrado"));
    verify(usuarioConquistaRepository, never()).save(any());
  }

  @Test
  void concederConquistaTier_ShouldSaveUsuarioConquista() {
    when(usuarioConquistaRepository.save(any())).thenReturn(new UsuarioConquista());

    conquistaService.concederConquistaTier(usuario, tierBronze, 10);

    verify(usuarioConquistaRepository, times(1)).save(any(UsuarioConquista.class));
  }

  @Test
  void criarConquistaComTiers_ShouldCreateConquistaWithTiers() {
    Map<TierConquista, Integer> tiers = new HashMap<>();
    tiers.put(TierConquista.BRONZE, 10);
    tiers.put(TierConquista.PRATA, 25);

    Conquista conquistaSalva = new Conquista();
    conquistaSalva.setId(BigInteger.valueOf(1));
    conquistaSalva.setNome("Nova Conquista");
    conquistaSalva.setDescricao("Descrição");
    conquistaSalva.setCampoValidacao("campo_teste");
    conquistaSalva.setTipoConquista(TipoConquista.INSIGNIA);

    when(conquistaRepository.save(any(Conquista.class))).thenReturn(conquistaSalva);
    when(conquistaTierRepository.save(any(ConquistaTier.class))).thenAnswer(invocation -> {
      ConquistaTier tier = invocation.getArgument(0);
      tier.setId(BigInteger.valueOf(1));
      return tier;
    });
    when(conquistaRepository.findByIdWithTiers(any())).thenReturn(Optional.of(conquistaSalva));

    Conquista result = conquistaService.criarConquistaComTiers(
      "Nova Conquista",
      "Descrição",
      "campo_teste",
      TipoConquista.INSIGNIA,
      tiers
    );

    assertNotNull(result);
    verify(conquistaRepository, times(1)).save(any(Conquista.class));
    verify(conquistaTierRepository, times(2)).save(any(ConquistaTier.class));
    verify(conquistaRepository, times(1)).findByIdWithTiers(any());
  }

  @Test
  void listarConquistasUsuario_ShouldReturnUserConquistas() {
    UsuarioConquista uc = new UsuarioConquista();
    uc.setUsuario(usuario);
    uc.setConquista(conquista);
    uc.setConquistaTier(tierBronze);

    when(usuarioConquistaRepository.findByUsuarioIdWithDetails(BigInteger.valueOf(1)))
      .thenReturn(Arrays.asList(uc));

    List<UsuarioConquista> result = conquistaService.listarConquistasUsuario(BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(usuarioConquistaRepository, times(1)).findByUsuarioIdWithDetails(BigInteger.valueOf(1));
  }

  @Test
  void listarConquistasUsuarioPorTipo_ShouldReturnFilteredConquistas() {
    UsuarioConquista uc = new UsuarioConquista();
    uc.setUsuario(usuario);
    uc.setConquista(conquista);
    uc.setConquistaTier(tierBronze);

    when(usuarioConquistaRepository.findByUsuarioIdAndTipo(BigInteger.valueOf(1), "INSIGNIA"))
      .thenReturn(Arrays.asList(uc));

    List<UsuarioConquista> result = conquistaService.listarConquistasUsuarioPorTipo(
      BigInteger.valueOf(1),
      TipoConquista.INSIGNIA
    );

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(usuarioConquistaRepository, times(1))
      .findByUsuarioIdAndTipo(BigInteger.valueOf(1), "INSIGNIA");
  }

  @Test
  void obterProgressoConquista_WithNoTiersConquered_ShouldReturnCorrectProgress() {
    when(conquistaRepository.findByIdWithTiers(BigInteger.valueOf(1)))
      .thenReturn(Optional.of(conquista));
    when(usuarioConquistaRepository.findByUsuarioIdAndConquistaId(any(), any()))
      .thenReturn(new ArrayList<>());

    ConquistaProgressoDTO result = conquistaService.obterProgressoConquista(
      BigInteger.valueOf(1),
      BigInteger.valueOf(1)
    );

    assertNotNull(result);
    assertNull(result.getMaiorTierConquistado());
    assertNotNull(result.getProximoTier());
    assertEquals(TierConquista.BRONZE, result.getProximoTier().getTier());
    assertFalse(result.isCompleta());
  }

  @Test
  void obterProgressoConquista_WithSomeTiersConquered_ShouldReturnCorrectProgress() {
    UsuarioConquista uc = new UsuarioConquista();
    uc.setConquistaTier(tierBronze);

    when(conquistaRepository.findByIdWithTiers(BigInteger.valueOf(1)))
      .thenReturn(Optional.of(conquista));
    when(usuarioConquistaRepository.findByUsuarioIdAndConquistaId(any(), any()))
      .thenReturn(Arrays.asList(uc));

    ConquistaProgressoDTO result = conquistaService.obterProgressoConquista(
      BigInteger.valueOf(1),
      BigInteger.valueOf(1)
    );

    assertNotNull(result);
    assertEquals(TierConquista.BRONZE, result.getMaiorTierConquistado());
    assertNotNull(result.getProximoTier());
    assertEquals(TierConquista.PRATA, result.getProximoTier().getTier());
    assertFalse(result.isCompleta());
  }

  @Test
  void obterProgressoConquista_WithAllTiersConquered_ShouldReturnCompleted() {
    UsuarioConquista uc1 = new UsuarioConquista();
    uc1.setConquistaTier(tierBronze);
    UsuarioConquista uc2 = new UsuarioConquista();
    uc2.setConquistaTier(tierPrata);
    UsuarioConquista uc3 = new UsuarioConquista();
    uc3.setConquistaTier(tierOuro);

    when(conquistaRepository.findByIdWithTiers(BigInteger.valueOf(1)))
      .thenReturn(Optional.of(conquista));
    when(usuarioConquistaRepository.findByUsuarioIdAndConquistaId(any(), any()))
      .thenReturn(Arrays.asList(uc1, uc2, uc3));

    ConquistaProgressoDTO result = conquistaService.obterProgressoConquista(
      BigInteger.valueOf(1),
      BigInteger.valueOf(1)
    );

    assertNotNull(result);
    assertEquals(TierConquista.OURO, result.getMaiorTierConquistado());
    assertNull(result.getProximoTier());
    assertTrue(result.isCompleta());
  }

  @Test
  void obterProgressoConquista_WithInvalidConquista_ShouldThrowException() {
    when(conquistaRepository.findByIdWithTiers(BigInteger.valueOf(999)))
      .thenReturn(Optional.empty());

    EntityNotFoundException exception = assertThrows(
      EntityNotFoundException.class,
      () -> conquistaService.obterProgressoConquista(BigInteger.valueOf(1), BigInteger.valueOf(999))
    );

    assertTrue(exception.getMessage().contains("Conquista não encontrada"));
  }
}
