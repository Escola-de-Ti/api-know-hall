package br.com.escoladeti.api_know_hall.tags;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.exception.PalavraProibidaException;
import br.com.escoladeti.api_know_hall.repository.TagsRepository;
import br.com.escoladeti.api_know_hall.service.utils.PalavrasProibidasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagsService Tests - Cobertura de Branch Completa")
class TagsServiceTest {

  @Mock
  private TagsRepository tagsRepository;

  @Mock
  private PalavrasProibidasService palavrasProibidasService;

  @InjectMocks
  private TagsService tagsService;

  private Tag tag;

  @BeforeEach
  void setUp() {
    tag = new Tag();
    tag.setId(BigInteger.valueOf(1));
    tag.setName("JAVA");
  }

  @Test
  @DisplayName("Deve criar nova tag quando não existir")
  void deveCriarNovaTagQuandoNaoExistir() {
    // Arrange
    String tagName = "java";
    when(palavrasProibidasService.contemPalavraProibida("JAVA")).thenReturn(false);
    when(tagsRepository.findByName("JAVA")).thenReturn(Optional.empty());
    when(tagsRepository.save(any(Tag.class))).thenReturn(tag);

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertNotNull(resultado);
    assertEquals("JAVA", resultado.getName());
    verify(palavrasProibidasService).contemPalavraProibida("JAVA");
    verify(tagsRepository).findByName("JAVA");
    verify(tagsRepository).save(any(Tag.class));
  }

  @Test
  @DisplayName("Deve retornar tag existente quando já existe")
  void deveRetornarTagExistenteQuandoJaExiste() {
    // Arrange
    String tagName = "java";
    when(palavrasProibidasService.contemPalavraProibida("JAVA")).thenReturn(false);
    when(tagsRepository.findByName("JAVA")).thenReturn(Optional.of(tag));

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertNotNull(resultado);
    assertEquals("JAVA", resultado.getName());
    verify(palavrasProibidasService).contemPalavraProibida("JAVA");
    verify(tagsRepository).findByName("JAVA");
    verify(tagsRepository, never()).save(any(Tag.class));
  }

  @Test
  @DisplayName("Deve lançar exceção quando tag contém palavra proibida")
  void deveLancarExcecaoQuandoTagContemPalavraProibida() {
    // Arrange
    String tagName = "palavrao";
    when(palavrasProibidasService.contemPalavraProibida("PALAVRAO")).thenReturn(true);
    when(palavrasProibidasService.identificarPalavraProibida("PALAVRAO")).thenReturn("palavrao");

    // Act & Assert
    PalavraProibidaException exception = assertThrows(
      PalavraProibidaException.class,
      () -> tagsService.createOrGetExisting(tagName)
    );

    assertTrue(exception.getMessage().contains("A tag contém conteúdo inapropriado"));
    assertTrue(exception.getMessage().contains("palavrao"));
    verify(palavrasProibidasService).contemPalavraProibida("PALAVRAO");
    verify(palavrasProibidasService).identificarPalavraProibida("PALAVRAO");
    verify(tagsRepository, never()).findByName(anyString());
    verify(tagsRepository, never()).save(any(Tag.class));
  }

  @Test
  @DisplayName("Deve normalizar nome da tag para uppercase")
  void deveNormalizarNomeDaTagParaUppercase() {
    // Arrange
    String tagName = "python";
    Tag tagPython = new Tag();
    tagPython.setName("PYTHON");

    when(palavrasProibidasService.contemPalavraProibida("PYTHON")).thenReturn(false);
    when(tagsRepository.findByName("PYTHON")).thenReturn(Optional.of(tagPython));

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertEquals("PYTHON", resultado.getName());
    verify(tagsRepository).findByName("PYTHON");
  }

  @Test
  @DisplayName("Deve remover espaços em branco ao normalizar")
  void deveRemoverEspacosEmBrancoAoNormalizar() {
    // Arrange
    String tagName = "  spring boot  ";
    Tag tagSpring = new Tag();
    tagSpring.setName("SPRING BOOT");

    when(palavrasProibidasService.contemPalavraProibida("SPRING BOOT")).thenReturn(false);
    when(tagsRepository.findByName("SPRING BOOT")).thenReturn(Optional.of(tagSpring));

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertEquals("SPRING BOOT", resultado.getName());
    verify(tagsRepository).findByName("SPRING BOOT");
  }

  @Test
  @DisplayName("Deve lançar exceção quando nome da tag é null")
  void deveLancarExcecaoQuandoNomeDaTagEhNull() {
    // Act & Assert
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> tagsService.createOrGetExisting(null)
    );

    assertEquals("Nome da tag não pode ser vazio", exception.getMessage());
    verify(tagsRepository, never()).findByName(anyString());
    verify(tagsRepository, never()).save(any(Tag.class));
  }

  @Test
  @DisplayName("Deve lançar exceção quando nome da tag é vazio")
  void deveLancarExcecaoQuandoNomeDaTagEhVazio() {
    // Act & Assert
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> tagsService.createOrGetExisting("")
    );

    assertEquals("Nome da tag não pode ser vazio", exception.getMessage());
    verify(tagsRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("Deve lançar exceção quando nome da tag é apenas espaços em branco")
  void deveLancarExcecaoQuandoNomeDaTagEhApenasEspacos() {
    // Act & Assert
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> tagsService.createOrGetExisting("   ")
    );

    assertEquals("Nome da tag não pode ser vazio", exception.getMessage());
    verify(tagsRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("Deve buscar tags mais populares com limite")
  void deveBuscarTagsMaisPopularesComLimite() {
    // Arrange
    Tag tag1 = new Tag(BigInteger.valueOf(1), "JAVA");
    Tag tag2 = new Tag(BigInteger.valueOf(2), "PYTHON");
    Tag tag3 = new Tag(BigInteger.valueOf(3), "JAVASCRIPT");
    List<Tag> tagsMaisPopulares = Arrays.asList(tag1, tag2, tag3);

    when(tagsRepository.findMostPopularTags(3)).thenReturn(tagsMaisPopulares);

    // Act
    List<Tag> resultado = tagsService.findMostPopular(3);

    // Assert
    assertNotNull(resultado);
    assertEquals(3, resultado.size());
    assertEquals("JAVA", resultado.get(0).getName());
    assertEquals("PYTHON", resultado.get(1).getName());
    assertEquals("JAVASCRIPT", resultado.get(2).getName());
    verify(tagsRepository).findMostPopularTags(3);
  }

  @Test
  @DisplayName("Deve buscar tags mais populares com limite 10")
  void deveBuscarTagsMaisPopularesComLimite10() {
    // Arrange
    when(tagsRepository.findMostPopularTags(10)).thenReturn(Arrays.asList());

    // Act
    List<Tag> resultado = tagsService.findMostPopular(10);

    // Assert
    assertNotNull(resultado);
    verify(tagsRepository).findMostPopularTags(10);
  }

  @Test
  @DisplayName("Deve criar tag com caracteres especiais normalizados")
  void deveCriarTagComCaracteresEspeciais() {
    // Arrange
    String tagName = "c++";
    Tag tagCpp = new Tag();
    tagCpp.setName("C++");

    when(palavrasProibidasService.contemPalavraProibida("C++")).thenReturn(false);
    when(tagsRepository.findByName("C++")).thenReturn(Optional.empty());
    when(tagsRepository.save(any(Tag.class))).thenReturn(tagCpp);

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertNotNull(resultado);
    verify(palavrasProibidasService).contemPalavraProibida("C++");
    verify(tagsRepository).findByName("C++");
    verify(tagsRepository).save(any(Tag.class));
  }

  @Test
  @DisplayName("Deve processar tag com múltiplas palavras")
  void deveProcessarTagComMultiplasPalavras() {
    // Arrange
    String tagName = "spring boot framework";
    Tag tagCompleta = new Tag();
    tagCompleta.setName("SPRING BOOT FRAMEWORK");

    when(palavrasProibidasService.contemPalavraProibida("SPRING BOOT FRAMEWORK")).thenReturn(false);
    when(tagsRepository.findByName("SPRING BOOT FRAMEWORK")).thenReturn(Optional.of(tagCompleta));

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertEquals("SPRING BOOT FRAMEWORK", resultado.getName());
    verify(tagsRepository).findByName("SPRING BOOT FRAMEWORK");
  }

  @Test
  @DisplayName("Deve criar tag quando findByName retorna empty")
  void deveCriarTagQuandoFindByNameRetornaEmpty() {
    // Arrange
    String tagName = "kotlin";
    Tag novaTag = new Tag();
    novaTag.setId(BigInteger.valueOf(99));
    novaTag.setName("KOTLIN");

    when(palavrasProibidasService.contemPalavraProibida("KOTLIN")).thenReturn(false);
    when(tagsRepository.findByName("KOTLIN")).thenReturn(Optional.empty());
    when(tagsRepository.save(any(Tag.class))).thenReturn(novaTag);

    // Act
    Tag resultado = tagsService.createOrGetExisting(tagName);

    // Assert
    assertNotNull(resultado);
    assertEquals(BigInteger.valueOf(99), resultado.getId());
    assertEquals("KOTLIN", resultado.getName());
    verify(tagsRepository).save(any(Tag.class));
  }
}
