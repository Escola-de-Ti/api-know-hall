package br.com.escoladeti.api_know_hall.tags;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.exception.PalavraProibidaException;
import br.com.escoladeti.api_know_hall.repository.TagsRepository;
import br.com.escoladeti.api_know_hall.service.TagsService;
import br.com.escoladeti.api_know_hall.service.utils.PalavrasProibidasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagsServiceTest {

  @Mock
  private TagsRepository tagsRepository;

  @Mock
  private PalavrasProibidasService palavrasProibidasService;

  @InjectMocks
  private TagsService tagsService;

  private Tag tagExistente;

  @BeforeEach
  void setUp() {
    tagExistente = new Tag();
    tagExistente.setId(BigInteger.ONE);
    tagExistente.setName("JAVA");
  }

  @Test
  void deveCriarNovaTagQuandoNaoExistir() {
    // Arrange
    String tagName = "Python";
    Tag novaTag = new Tag();
    novaTag.setId(BigInteger.TWO);
    novaTag.setName("PYTHON");

    when(palavrasProibidasService.contemPalavraProibida("PYTHON")).thenReturn(false);
    when(tagsRepository.findByName("PYTHON")).thenReturn(Optional.empty());
    when(tagsRepository.save(any(Tag.class))).thenReturn(novaTag);

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertNotNull(resultado);
    assertEquals("PYTHON", resultado.getName());
    verify(tagsRepository, times(1)).save(any(Tag.class));
  }

  @Test
  void deveRetornarTagExistenteQuandoJaExistir() {
    // Arrange
    String tagName = "java";

    when(palavrasProibidasService.contemPalavraProibida("JAVA")).thenReturn(false);
    when(tagsRepository.findByName("JAVA")).thenReturn(Optional.of(tagExistente));

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertNotNull(resultado);
    assertEquals(tagExistente.getId(), resultado.getId());
    assertEquals("JAVA", resultado.getName());
    verify(tagsRepository, never()).save(any(Tag.class));
  }

  @Test
  void deveNormalizarNomeDaTag() {
    // Arrange
    String tagName = "  java  ";

    when(palavrasProibidasService.contemPalavraProibida("JAVA")).thenReturn(false);
    when(tagsRepository.findByName("JAVA")).thenReturn(Optional.of(tagExistente));

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertEquals("JAVA", resultado.getName());
    verify(tagsRepository).findByName("JAVA");
  }

  @Test
  void deveLancarExceptionQuandoNomeVazio() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class,
      () -> tagsService.createOrGetExisting(""));
  }

  @Test
  void deveLancarExceptionQuandoNomeNulo() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class,
      () -> tagsService.createOrGetExisting(null));
  }

  @Test
  void deveLancarExceptionQuandoNomeApenasEspacos() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class,
      () -> tagsService.createOrGetExisting("   "));
  }

  @Test
  void deveLancarExceptionQuandoPalavraProibida() {
    // Arrange
    String tagName = "idiota";

    when(palavrasProibidasService.contemPalavraProibida("IDIOTA")).thenReturn(true);
    when(palavrasProibidasService.identificarPalavraProibida("IDIOTA")).thenReturn("IDIOTA");

    // Act & Assert
    PalavraProibidaException exception = assertThrows(
      PalavraProibidaException.class,
      () -> tagsService.createOrGetExisting(tagName)
    );

    assertTrue(exception.getMessage().contains("inapropriado"));
    verify(tagsRepository, never()).save(any(Tag.class));
  }

  @Test
  void deveDetectarPalavraProibidaComVariacoes() {
    // Arrange
    String tagName = "1d10t4";

    when(palavrasProibidasService.contemPalavraProibida("1D10T4")).thenReturn(true);
    when(palavrasProibidasService.identificarPalavraProibida("1D10T4"))
      .thenReturn("padrão suspeito");

    // Act & Assert
    assertThrows(PalavraProibidaException.class,
      () -> tagsService.createOrGetExisting(tagName));
  }

  @Test
  void deveRetornarTagsPopularesComSucesso() {
    // Arrange
    Tag tag1 = new Tag();
    tag1.setId(BigInteger.ONE);
    tag1.setName("JAVA");

    Tag tag2 = new Tag();
    tag2.setId(BigInteger.TWO);
    tag2.setName("PYTHON");

    List<Tag> tagsPopulares = Arrays.asList(tag1, tag2);

    when(tagsRepository.findMostPopularTags(10)).thenReturn(tagsPopulares);

    // Act
    List<Tag> resultado = tagsService.findMostPopular(10);

    // Assert
    assertNotNull(resultado);
    assertEquals(2, resultado.size());
    assertEquals("JAVA", resultado.get(0).getName());
    assertEquals("PYTHON", resultado.get(1).getName());
    verify(tagsRepository, times(1)).findMostPopularTags(10);
  }

  @Test
  void deveRetornarListaVaziaQuandoNaoHaTagsPopulares() {
    // Arrange
    when(tagsRepository.findMostPopularTags(10)).thenReturn(Arrays.asList());

    // Act
    List<Tag> resultado = tagsService.findMostPopular(10);

    // Assert
    assertNotNull(resultado);
    assertTrue(resultado.isEmpty());
  }

  @Test
  void deveRespeitarLimiteDeTagsPopulares() {
    // Arrange
    int limite = 5;
    List<Tag> tags = Arrays.asList(new Tag(), new Tag(), new Tag(), new Tag(), new Tag());

    when(tagsRepository.findMostPopularTags(limite)).thenReturn(tags);

    // Act
    List<Tag> resultado = tagsService.findMostPopular(limite);

    // Assert
    assertEquals(limite, resultado.size());
    verify(tagsRepository).findMostPopularTags(limite);
  }
}
