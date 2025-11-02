package br.com.escoladeti.api_know_hall.conquista;

import br.com.escoladeti.api_know_hall.dto.conquista.*;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.entity.conquista.ConquistaTier;
import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import br.com.escoladeti.api_know_hall.enums.TierConquista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;

class ConquistaDTOTest {

  private Conquista conquista;
  private ConquistaTier tier;
  private Usuario usuario;
  private UsuarioConquista usuarioConquista;

  @BeforeEach
  void setUp() {

    conquista = new Conquista();
    conquista.setId(BigInteger.valueOf(1));
    conquista.setNome("Participante Ativo");
    conquista.setDescricao("Participe de eventos");
    conquista.setTipoConquista(TipoConquista.INSIGNIA);
    conquista.setCampoValidacao("participacoes");
    conquista.setIconeUrl("https://example.com/icon.png");

    tier = new ConquistaTier();
    tier.setId(BigInteger.valueOf(1));
    tier.setConquista(conquista);
    tier.setTier(TierConquista.BRONZE);
    tier.setQuantidadeNecessaria(10);
    tier.setDescricaoTier("Nível inicial");

    conquista.setTiers(Arrays.asList(tier));

    usuario = new Usuario();
    usuario.setId(BigInteger.valueOf(1));
    usuario.setNome("Test User");

    usuarioConquista = new UsuarioConquista();
    usuarioConquista.setId(BigInteger.valueOf(1));
    usuarioConquista.setUsuario(usuario);
    usuarioConquista.setConquista(conquista);
    usuarioConquista.setConquistaTier(tier);
    usuarioConquista.setDataObtencao(LocalDateTime.now());
    usuarioConquista.setProgressoAtual(10);
  }

  @Test
  void conquistaResponseDTO_FromEntity_ShouldMapCorrectly() {
    ConquistaResponseDTO dto = ConquistaResponseDTO.fromEntity(conquista);

    assertNotNull(dto);
    assertEquals(conquista.getId(), dto.getId());
    assertEquals(conquista.getNome(), dto.getNome());
    assertEquals(conquista.getDescricao(), dto.getDescricao());
    assertEquals(conquista.getTipoConquista(), dto.getTipoConquista());
    assertEquals(conquista.getCampoValidacao(), dto.getCampoValidacao());
    assertEquals(conquista.getIconeUrl(), dto.getIconeUrl());
    assertNotNull(dto.getTiers());
    assertEquals(1, dto.getTiers().size());
  }


  @Test
  void conquistaTierResponseDTO_FromEntity_ShouldMapCorrectly() {
    ConquistaTierResponseDTO dto = ConquistaTierResponseDTO.fromEntity(tier);

    assertNotNull(dto);
    assertEquals(tier.getId(), dto.getId());
    assertEquals(tier.getTier(), dto.getTier());
    assertEquals(tier.getQuantidadeNecessaria(), dto.getQuantidadeNecessaria());
    assertEquals(tier.getDescricaoTier(), dto.getDescricaoTier());
    assertEquals(tier.getTier().getNivel(), dto.getNivel());
  }

  @Test
  void usuarioConquistaResponseDTO_FromEntity_ShouldMapCorrectly() {
    UsuarioConquistaResponseDTO dto = UsuarioConquistaResponseDTO.fromEntity(usuarioConquista);

    assertNotNull(dto);
    assertEquals(usuarioConquista.getId(), dto.getId());
    assertNotNull(dto.getConquista());
    assertEquals(conquista.getId(), dto.getConquista().getId());
    assertNotNull(dto.getTier());
    assertEquals(tier.getTier(), dto.getTier().getTier());
    assertEquals(usuarioConquista.getDataObtencao(), dto.getDataObtencao());
    assertEquals(usuarioConquista.getProgressoAtual(), dto.getProgressoAtual());
  }

  @Test
  void conquistaSimpleDTO_FromEntity_ShouldMapCorrectly() {
    ConquistaSimpleDTO dto = ConquistaSimpleDTO.fromEntity(conquista);

    assertNotNull(dto);
    assertEquals(conquista.getId(), dto.getId());
    assertEquals(conquista.getNome(), dto.getNome());
    assertEquals(conquista.getDescricao(), dto.getDescricao());
    assertEquals(conquista.getTipoConquista(), dto.getTipoConquista());
    assertEquals(conquista.getIconeUrl(), dto.getIconeUrl());
  }

  @Test
  void conquistaProgressoResponseDTO_FromDTO_ShouldMapCorrectly() {
    ConquistaProgressoDTO progressoDTO = new ConquistaProgressoDTO(
      conquista,
      TierConquista.BRONZE,
      tier,
      Arrays.asList(usuarioConquista)
    );

    ConquistaProgressoResponseDTO dto = ConquistaProgressoResponseDTO.fromDTO(progressoDTO);

    assertNotNull(dto);
    assertNotNull(dto.getConquista());
    assertEquals(TierConquista.BRONZE, dto.getMaiorTierConquistado());
    assertNotNull(dto.getProximoTier());
    assertNotNull(dto.getTiersConquistados());
    assertEquals(1, dto.getTiersConquistados().size());
  }

  @Test
  void conquistaProgressoDTO_IsCompleta_WithProximoTier_ShouldReturnFalse() {
    ConquistaProgressoDTO dto = new ConquistaProgressoDTO(
      conquista,
      TierConquista.BRONZE,
      tier,
      Arrays.asList(usuarioConquista)
    );

    assertFalse(dto.isCompleta());
  }

  @Test
  void conquistaProgressoDTO_IsCompleta_WithoutProximoTier_ShouldReturnTrue() {
    ConquistaProgressoDTO dto = new ConquistaProgressoDTO(
      conquista,
      TierConquista.OURO,
      null,
      Arrays.asList(usuarioConquista)
    );

    assertTrue(dto.isCompleta());
  }

  @Test
  void conquistaProgressoDTO_GetPercentualProgresso_ShouldCalculateCorrectly() {
    usuarioConquista.setProgressoAtual(5);
    tier.setQuantidadeNecessaria(10);

    ConquistaProgressoDTO dto = new ConquistaProgressoDTO(
      conquista,
      null,
      tier,
      Arrays.asList(usuarioConquista)
    );

    assertEquals(50.0, dto.getPercentualProgresso(), 0.01);
  }

  @Test
  void conquistaProgressoDTO_GetPercentualProgresso_WhenCompleted_ShouldReturn100() {
    ConquistaProgressoDTO dto = new ConquistaProgressoDTO(
      conquista,
      TierConquista.OURO,
      null,
      Arrays.asList(usuarioConquista)
    );

    assertEquals(100.0, dto.getPercentualProgresso(), 0.01);
  }
}
