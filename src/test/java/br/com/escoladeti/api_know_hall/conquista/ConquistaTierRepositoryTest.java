package br.com.escoladeti.api_know_hall.conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
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
class ConquistaTierRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ConquistaTierRepository conquistaTierRepository;

  private Conquista conquista;
  private ConquistaTier tierBronze;
  private ConquistaTier tierPrata;
  private ConquistaTier tierOuro;

  @BeforeEach
  void setUp() {
    conquista = new Conquista();
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Participe de múltiplos eventos");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");
    conquista = entityManager.persistAndFlush(conquista);

    tierBronze = new ConquistaTier();
    tierBronze.setConquista(conquista);
    tierBronze.setTier(TierConquista.BRONZE);
    tierBronze.setQuantidadeNecessaria(10);
    tierBronze.setDescricaoTier("Nível inicial");

    tierPrata = new ConquistaTier();
    tierPrata.setConquista(conquista);
    tierPrata.setTier(TierConquista.PRATA);
    tierPrata.setQuantidadeNecessaria(25);

    tierOuro = new ConquistaTier();
    tierOuro.setConquista(conquista);
    tierOuro.setTier(TierConquista.OURO);
    tierOuro.setQuantidadeNecessaria(50);
  }

  @Test
  void save_ShouldPersistConquistaTier() {
    ConquistaTier saved = conquistaTierRepository.save(tierBronze);

    assertNotNull(saved.getId());
    assertEquals(TierConquista.BRONZE, saved.getTier());
    assertEquals(10, saved.getQuantidadeNecessaria());
    assertEquals("Nível inicial", saved.getDescricaoTier());
  }

  @Test
  void findById_WithValidId_ShouldReturnTier() {
    ConquistaTier saved = entityManager.persistAndFlush(tierBronze);

    Optional<ConquistaTier> found = conquistaTierRepository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals(TierConquista.BRONZE, found.get().getTier());
  }

  @Test
  void findById_WithInvalidId_ShouldReturnEmpty() {
    Optional<ConquistaTier> found = conquistaTierRepository.findById(BigInteger.valueOf(999));

    assertFalse(found.isPresent());
  }

  @Test
  void findByConquistaId_ShouldReturnTiersForConquista() {
    entityManager.persistAndFlush(tierBronze);
    entityManager.persistAndFlush(tierPrata);

    List<ConquistaTier> tiers = conquistaTierRepository.findByConquistaId(conquista.getId());

    assertNotNull(tiers);
    assertEquals(2, tiers.size());
  }

  @Test
  void findByConquistaId_WithNonExistentId_ShouldReturnEmpty() {
    List<ConquistaTier> tiers = conquistaTierRepository.findByConquistaId(BigInteger.valueOf(999));

    assertNotNull(tiers);
    assertTrue(tiers.isEmpty());
  }

  @Test
  void findByConquistaIdAndTier_ShouldReturnSpecificTier() {
    entityManager.persistAndFlush(tierBronze);

    Optional<ConquistaTier> found = conquistaTierRepository
      .findByConquistaIdAndTier(conquista.getId(), TierConquista.BRONZE);

    assertTrue(found.isPresent());
    assertEquals(TierConquista.BRONZE, found.get().getTier());
  }

  @Test
  void findByConquistaIdAndTier_WithNonExistentTier_ShouldReturnEmpty() {
    Optional<ConquistaTier> found = conquistaTierRepository
      .findByConquistaIdAndTier(conquista.getId(), TierConquista.DIAMANTE);

    assertFalse(found.isPresent());
  }

  @Test
  void findByConquistaIdOrderByTier_ShouldReturnOrderedTiers() {
    entityManager.persistAndFlush(tierOuro);
    entityManager.persistAndFlush(tierPrata);
    entityManager.persistAndFlush(tierBronze);

    List<ConquistaTier> tiers = conquistaTierRepository
      .findByConquistaIdOrderByTier(conquista.getId());

    assertNotNull(tiers);
    assertEquals(3, tiers.size());
    assertEquals(TierConquista.BRONZE, tiers.get(0).getTier());
    assertEquals(TierConquista.PRATA, tiers.get(1).getTier());
    assertEquals(TierConquista.OURO, tiers.get(2).getTier());
  }

  @Test
  void deleteById_ShouldRemoveTier() {
    ConquistaTier saved = entityManager.persistAndFlush(tierBronze);
    BigInteger id = saved.getId();

    conquistaTierRepository.deleteById(id);
    entityManager.flush();

    ConquistaTier deleted = entityManager.find(ConquistaTier.class, id);
    assertNull(deleted);
  }
}
