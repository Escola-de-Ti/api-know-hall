package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.repository.ConquistaRepository;
import br.com.escoladeti.api_know_hall.repository.ConquistaTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ConquistaRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ConquistaRepository conquistaRepository;

  @Autowired
  private ConquistaTierRepository conquistaTierRepository;

  private Conquista conquista;

  @BeforeEach
  void setUp() {
    conquista = new Conquista();
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Participe de múltiplos eventos");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");
    conquista.setIconeUrl("https://example.com/icon.png");
  }

  @Test
  void findAll_ShouldReturnAllConquistas() {
    entityManager.persistAndFlush(conquista);

    List<Conquista> conquistas = conquistaRepository.findAll();

    assertNotNull(conquistas);
    assertTrue(conquistas.size() > 0);
    assertEquals("Participante Ativo", conquistas.get(0).getNome());
  }

  @Test
  void findById_WithValidId_ShouldReturnConquista() {
    Conquista savedConquista = entityManager.persistAndFlush(conquista);

    Optional<Conquista> found = conquistaRepository.findById(savedConquista.getId());

    assertTrue(found.isPresent());
    assertEquals("Participante Ativo", found.get().getNome());
  }

  @Test
  void findById_WithInvalidId_ShouldReturnEmpty() {
    Optional<Conquista> found = conquistaRepository.findById(BigInteger.valueOf(999));

    assertFalse(found.isPresent());
  }

  @Test
  void save_ShouldPersistConquista() {
    Conquista savedConquista = conquistaRepository.save(conquista);

    assertNotNull(savedConquista.getId());
    assertEquals("Participante Ativo", savedConquista.getNome());

    Conquista foundConquista = entityManager.find(Conquista.class, savedConquista.getId());
    assertNotNull(foundConquista);
    assertEquals("Participante Ativo", foundConquista.getNome());
  }

  @Test
  void deleteById_ShouldRemoveConquista() {
    Conquista savedConquista = entityManager.persistAndFlush(conquista);
    BigInteger id = savedConquista.getId();

    conquistaRepository.deleteById(id);
    entityManager.flush();

    Conquista deletedConquista = entityManager.find(Conquista.class, id);
    assertNull(deletedConquista);
  }

  @Test
  void findByTipoConquista_ShouldReturnInsignias() {
    entityManager.persistAndFlush(conquista);

    List<Conquista> insignias = conquistaRepository.findByTipoConquista(TipoConquista.INSIGNIA);

    assertNotNull(insignias);
    assertTrue(insignias.size() > 0);
    assertEquals(TipoConquista.INSIGNIA, insignias.get(0).getTipoConquista());
  }

  @Test
  void findByCampoValidacao_ShouldReturnConquistasByCampo() {
    entityManager.persistAndFlush(conquista);

    List<Conquista> found = conquistaRepository.findByCampoValidacao("participacoes");

    assertNotNull(found);
    assertTrue(found.size() > 0);
    assertEquals("participacoes", found.get(0).getCampoValidacao());
  }

  @Test
  void findByCampoValidacao_WithNonExistentCampo_ShouldReturnEmpty() {
    List<Conquista> found = conquistaRepository.findByCampoValidacao("campo_inexistente");

    assertNotNull(found);
    assertTrue(found.isEmpty());
  }

  @Test
  void findByIdWithTiers_ShouldReturnConquistaWithTiers() {
    Conquista savedConquista = entityManager.persistAndFlush(conquista);

    ConquistaTier tier = new ConquistaTier();
    tier.setConquista(savedConquista);
    tier.setTier(TierConquista.BRONZE);
    tier.setQuantidadeNecessaria(10);
    entityManager.persistAndFlush(tier);

    Optional<Conquista> found = conquistaRepository.findByIdWithTiers(savedConquista.getId());

    assertTrue(found.isPresent());
    assertNotNull(found.get().getTiers());
  }
}
