package br.com.escoladeti.api_know_hall.conquista;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.conquista.*;
import br.com.escoladeti.api_know_hall.enums.*;
import br.com.escoladeti.api_know_hall.repository.UsuarioConquistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioConquistaRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UsuarioConquistaRepository usuarioConquistaRepository;

  private Usuario usuario;
  private Usuario usuario2;
  private Conquista conquista;
  private ConquistaTier tierBronze;
  private ConquistaTier tierPrata;
  private UsuarioConquista usuarioConquista;

  @BeforeEach
  void setUp() {
    // Limpar todas as usuario_conquistas antes de cada teste
    usuarioConquistaRepository.deleteAll();
    usuarioConquistaRepository.flush();
    entityManager.clear();

    usuario = new Usuario();
    usuario.setEmail("test@test.com");
    usuario.setCpf("12345678901");
    usuario.setNome("Test User");
    usuario.setSenhaHash("hashedPassword");
    usuario.setStatusUsuario(StatusUsuario.ATIVO);
    usuario.setTipoUsuario(TipoUsuario.ALUNO);
    usuario = entityManager.persistAndFlush(usuario);

    usuario2 = new Usuario();
    usuario2.setEmail("test2@test.com");
    usuario2.setCpf("98765432100");
    usuario2.setNome("Test User 2");
    usuario2.setSenhaHash("hashedPassword");
    usuario2.setStatusUsuario(StatusUsuario.ATIVO);
    usuario2.setTipoUsuario(TipoUsuario.ALUNO);
    usuario2 = entityManager.persistAndFlush(usuario2);

    conquista = new Conquista();
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Participe de eventos");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");
    conquista = entityManager.persistAndFlush(conquista);

    tierBronze = new ConquistaTier();
    tierBronze.setConquista(conquista);
    tierBronze.setTier(TierConquista.BRONZE);
    tierBronze.setQuantidadeNecessaria(10);
    tierBronze = entityManager.persistAndFlush(tierBronze);

    tierPrata = new ConquistaTier();
    tierPrata.setConquista(conquista);
    tierPrata.setTier(TierConquista.PRATA);
    tierPrata.setQuantidadeNecessaria(25);
    tierPrata = entityManager.persistAndFlush(tierPrata);

    usuarioConquista = new UsuarioConquista();
    usuarioConquista.setUsuario(usuario);
    usuarioConquista.setConquista(conquista);
    usuarioConquista.setConquistaTier(tierBronze);
    usuarioConquista.setDataObtencao(LocalDateTime.now());
    usuarioConquista.setProgressoAtual(10);
  }

  @Test
  void save_ShouldPersistUsuarioConquista() {
    UsuarioConquista saved = usuarioConquistaRepository.save(usuarioConquista);

    assertNotNull(saved.getId());
    assertEquals(usuario.getId(), saved.getUsuario().getId());
    assertEquals(conquista.getId(), saved.getConquista().getId());
    assertNotNull(saved.getDataObtencao());
  }

  @Test
  void findByUsuarioId_ShouldReturnUserConquistas() {
    entityManager.persistAndFlush(usuarioConquista);

    List<UsuarioConquista> found = usuarioConquistaRepository.findByUsuarioId(usuario.getId());

    assertNotNull(found);
    assertTrue(found.size() > 0);
    assertEquals(usuario.getId(), found.get(0).getUsuario().getId());
  }

  @Test
  void findByUsuarioId_WithMultipleConquistas_ShouldReturnAll() {
    entityManager.persistAndFlush(usuarioConquista);

    UsuarioConquista uc2 = new UsuarioConquista();
    uc2.setUsuario(usuario);
    uc2.setConquista(conquista);
    uc2.setConquistaTier(tierPrata);
    uc2.setDataObtencao(LocalDateTime.now());
    uc2.setProgressoAtual(25);
    entityManager.persistAndFlush(uc2);

    List<UsuarioConquista> found = usuarioConquistaRepository.findByUsuarioId(usuario.getId());

    assertEquals(2, found.size());
  }

  @Test
  void findByConquistaId_ShouldReturnConquistaUsers() {
    entityManager.persistAndFlush(usuarioConquista);

    UsuarioConquista uc2 = new UsuarioConquista();
    uc2.setUsuario(usuario2);
    uc2.setConquista(conquista);
    uc2.setConquistaTier(tierBronze);
    uc2.setDataObtencao(LocalDateTime.now());
    uc2.setProgressoAtual(10);
    entityManager.persistAndFlush(uc2);

    List<UsuarioConquista> found = usuarioConquistaRepository.findByConquistaId(conquista.getId());

    assertNotNull(found);
    assertEquals(2, found.size());
  }

  @Test
  void existsByUsuarioIdAndConquistaTierId_ShouldReturnTrue() {
    entityManager.persistAndFlush(usuarioConquista);
    entityManager.clear();

    boolean exists = usuarioConquistaRepository
      .existsByUsuarioIdAndConquistaTierId(usuario.getId(), tierBronze.getId());

    assertTrue(exists);
  }

  @Test
  void existsByUsuarioIdAndConquistaTierId_ShouldReturnFalse() {
    // Garantir que o banco está limpo (já feito no @BeforeEach)
    entityManager.clear();

    boolean exists = usuarioConquistaRepository
      .existsByUsuarioIdAndConquistaTierId(usuario.getId(), tierBronze.getId());

    assertFalse(exists);
  }

  @Test
  void findByUsuarioIdAndTipo_ShouldReturnInsignias() {
    entityManager.persistAndFlush(usuarioConquista);

    List<UsuarioConquista> found = usuarioConquistaRepository
      .findByUsuarioIdAndTipo(usuario.getId(), "INSIGNIA");

    assertNotNull(found);
    assertTrue(found.size() > 0);
    assertEquals(TipoConquista.INSIGNIA, found.get(0).getConquista().getTipoConquista());
  }

  @Test
  void findByUsuarioIdWithDetails_ShouldReturnConquistasWithDetails() {
    entityManager.persistAndFlush(usuarioConquista);

    List<UsuarioConquista> found = usuarioConquistaRepository
      .findByUsuarioIdWithDetails(usuario.getId());

    assertNotNull(found);
    assertTrue(found.size() > 0);
  }

  @Test
  void findByUsuarioIdAndConquistaId_ShouldReturnOrderedTiers() {
    entityManager.persistAndFlush(usuarioConquista);

    UsuarioConquista uc2 = new UsuarioConquista();
    uc2.setUsuario(usuario);
    uc2.setConquista(conquista);
    uc2.setConquistaTier(tierPrata);
    uc2.setDataObtencao(LocalDateTime.now());
    uc2.setProgressoAtual(25);
    entityManager.persistAndFlush(uc2);

    List<UsuarioConquista> found = usuarioConquistaRepository
      .findByUsuarioIdAndConquistaId(usuario.getId(), conquista.getId());

    assertNotNull(found);
    assertEquals(2, found.size());
    assertEquals(TierConquista.BRONZE, found.get(0).getConquistaTier().getTier());
    assertEquals(TierConquista.PRATA, found.get(1).getConquistaTier().getTier());
  }

  @Test
  void deleteById_ShouldRemoveUsuarioConquista() {
    UsuarioConquista saved = entityManager.persistAndFlush(usuarioConquista);
    BigInteger id = saved.getId();

    entityManager.clear();

    usuarioConquistaRepository.deleteById(id);
    usuarioConquistaRepository.flush();

    boolean exists = usuarioConquistaRepository.existsById(id);
    assertFalse(exists);
  }
}
