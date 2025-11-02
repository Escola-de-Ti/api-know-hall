package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

  @Test
  void tierConquista_GetNivel_ShouldReturnCorrectOrder() {
    assertEquals(1, TierConquista.BRONZE.getNivel());
    assertEquals(2, TierConquista.PRATA.getNivel());
    assertEquals(3, TierConquista.OURO.getNivel());
    assertEquals(4, TierConquista.PLATINA.getNivel());
    assertEquals(5, TierConquista.DIAMANTE.getNivel());
  }

  @Test
  void tipoConquista_Values_ShouldExist() {
    assertEquals(2, TipoConquista.values().length);
    assertNotNull(TipoConquista.valueOf("INSIGNIA"));
    assertNotNull(TipoConquista.valueOf("CERTIFICADO"));
  }

  @Test
  void tierConquista_CompareTo_ShouldOrderCorrectly() {
    assertTrue(TierConquista.BRONZE.getNivel() < TierConquista.PRATA.getNivel());
    assertTrue(TierConquista.PRATA.getNivel() < TierConquista.OURO.getNivel());
    assertTrue(TierConquista.OURO.getNivel() < TierConquista.PLATINA.getNivel());
    assertTrue(TierConquista.PLATINA.getNivel() < TierConquista.DIAMANTE.getNivel());
  }
}
