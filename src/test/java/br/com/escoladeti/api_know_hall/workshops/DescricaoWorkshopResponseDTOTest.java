package br.com.escoladeti.api_know_hall.workshops;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.workshop.DescricaoWorkshop;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.ImagemTipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DescricaoWorkshopResponseDTO Tests - Cobertura de Branch Completa")
class DescricaoWorkshopResponseDTOTest {

  private DescricaoWorkshop descricao;
  private Imagem imagemWorkshop;
  private Workshop workshop;

  @BeforeEach
  void setUp() {
    // Setup Workshop
    workshop = new Workshop();
    workshop.setId(BigInteger.valueOf(1));

    // Setup Imagem
    imagemWorkshop = new Imagem();
    imagemWorkshop.setId(BigInteger.valueOf(100));
    imagemWorkshop.setNome("workshop-image.jpg");
    imagemWorkshop.setUrl("https://example.com/images/workshop.jpg");
    imagemWorkshop.setPath("/uploads/workshops/workshop.jpg");
    imagemWorkshop.setType(ImagemTipo.WORKSHOP);
    imagemWorkshop.setIdImagemSupabase("supabase-123");

    // Setup Descrição
    descricao = new DescricaoWorkshop();
    descricao.setId(BigInteger.valueOf(10));
    descricao.setTema("Spring Boot Avançado");
    descricao.setDescricao("Workshop completo sobre Spring Boot, incluindo segurança, testes e deploy");
    descricao.setWorkshop(workshop);
    descricao.setImagemWorkshop(imagemWorkshop);
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com todos os campos preenchidos usando fromEntity")
  void deveCriarDescricaoWorkshopResponseDTOComTodosOsCampos() {
    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertNotNull(dto);
    assertEquals(BigInteger.valueOf(10), dto.getId());
    assertEquals("Spring Boot Avançado", dto.getTema());
    assertEquals("Workshop completo sobre Spring Boot, incluindo segurança, testes e deploy", dto.getDescricao());
    assertEquals("https://example.com/images/workshop.jpg", dto.getUrlImagem());
    assertEquals(BigInteger.valueOf(100), dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve retornar null quando descrição é null - branch específico")
  void deveRetornarNullQuandoDescricaoNull() {
    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(null);

    // Assert
    assertNull(dto);
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com imagemWorkshop null - branch específico")
  void deveCriarDescricaoWorkshopResponseDTOComImagemNull() {
    // Arrange
    descricao.setImagemWorkshop(null);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertNotNull(dto);
    assertEquals(BigInteger.valueOf(10), dto.getId());
    assertEquals("Spring Boot Avançado", dto.getTema());
    assertEquals("Workshop completo sobre Spring Boot, incluindo segurança, testes e deploy", dto.getDescricao());
    assertNull(dto.getUrlImagem());
    assertNull(dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com descrição vazia")
  void deveCriarDescricaoWorkshopResponseDTOComDescricaoVazia() {
    // Arrange
    descricao.setDescricao("");

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals("", dto.getDescricao());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com tema curto")
  void deveCriarDescricaoWorkshopResponseDTOComTemaCurto() {
    // Arrange
    descricao.setTema("Java");

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals("Java", dto.getTema());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com tema longo")
  void deveCriarDescricaoWorkshopResponseDTOComTemaLongo() {
    // Arrange
    String temaLongo = "Workshop Completo de Spring Boot com Microserviços, Docker, Kubernetes e CI/CD";
    descricao.setTema(temaLongo);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals(temaLongo, dto.getTema());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO usando construtor sem argumentos")
  void deveCriarDescricaoWorkshopResponseDTOComConstrutorSemArgumentos() {
    // Act
    DescricaoWorkshopResponseDTO dto = new DescricaoWorkshopResponseDTO();

    // Assert
    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getTema());
    assertNull(dto.getDescricao());
    assertNull(dto.getUrlImagem());
    assertNull(dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO usando construtor com todos argumentos")
  void deveCriarDescricaoWorkshopResponseDTOComConstrutorComTodosArgumentos() {
    // Act
    DescricaoWorkshopResponseDTO dto = new DescricaoWorkshopResponseDTO(
      BigInteger.valueOf(99),
      "Tema Test",
      "Descrição Test",
      "https://test.com/image.jpg",
      BigInteger.valueOf(999)
    );

    // Assert
    assertNotNull(dto);
    assertEquals(BigInteger.valueOf(99), dto.getId());
    assertEquals("Tema Test", dto.getTema());
    assertEquals("Descrição Test", dto.getDescricao());
    assertEquals("https://test.com/image.jpg", dto.getUrlImagem());
    assertEquals(BigInteger.valueOf(999), dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve testar setters e getters completos")
  void deveTestarSettersEGetters() {
    // Arrange
    DescricaoWorkshopResponseDTO dto = new DescricaoWorkshopResponseDTO();

    // Act
    dto.setId(BigInteger.valueOf(50));
    dto.setTema("Setter Tema");
    dto.setDescricao("Setter Descrição");
    dto.setUrlImagem("https://setter.com/image.png");
    dto.setIdImagem(BigInteger.valueOf(500));

    // Assert
    assertEquals(BigInteger.valueOf(50), dto.getId());
    assertEquals("Setter Tema", dto.getTema());
    assertEquals("Setter Descrição", dto.getDescricao());
    assertEquals("https://setter.com/image.png", dto.getUrlImagem());
    assertEquals(BigInteger.valueOf(500), dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com diferentes tipos de imagem")
  void deveCriarDescricaoWorkshopResponseDTOComDiferentesTiposDeImagem() {
    // Arrange
    Imagem novaImagem = new Imagem();
    novaImagem.setId(BigInteger.valueOf(200));
    novaImagem.setUrl("https://cdn.example.com/workshop-banner.png");
    novaImagem.setType(ImagemTipo.WORKSHOP);
    descricao.setImagemWorkshop(novaImagem);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals("https://cdn.example.com/workshop-banner.png", dto.getUrlImagem());
    assertEquals(BigInteger.valueOf(200), dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com descrição null")
  void deveCriarDescricaoWorkshopResponseDTOComDescricaoNullNoTexto() {
    // Arrange
    descricao.setDescricao(null);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getDescricao());
    assertEquals("Spring Boot Avançado", dto.getTema());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com tema null")
  void deveCriarDescricaoWorkshopResponseDTOComTemaNull() {
    // Arrange
    descricao.setTema(null);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getTema());
    assertNotNull(dto.getDescricao());
  }

  @Test
  @DisplayName("Deve preservar ID null quando entidade não tem ID")
  void devePreservarIdNull() {
    // Arrange
    descricao.setId(null);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertNotNull(dto);
    assertNull(dto.getId());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com valores extremos de ID")
  void deveCriarDescricaoWorkshopResponseDTOComValoresExtremosDeId() {
    // Arrange
    BigInteger idGrande = new BigInteger("999999999999999999");
    descricao.setId(idGrande);
    imagemWorkshop.setId(idGrande);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals(idGrande, dto.getId());
    assertEquals(idGrande, dto.getIdImagem());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com URL de imagem longa")
  void deveCriarDescricaoWorkshopResponseDTOComUrlLonga() {
    // Arrange
    String urlLonga = "https://cdn.cloudinary.com/workshop-platform/images/workshops/2024/janeiro/workshop-spring-boot-avancado-com-microservicos-docker-kubernetes-completo-banner-principal.jpg";
    imagemWorkshop.setUrl(urlLonga);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals(urlLonga, dto.getUrlImagem());
  }

  @Test
  @DisplayName("Deve manter consistência quando fromEntity é chamado múltiplas vezes")
  void deveManterConsistenciaEmMultiplasChamadas() {
    // Act
    DescricaoWorkshopResponseDTO dto1 = DescricaoWorkshopResponseDTO.fromEntity(descricao);
    DescricaoWorkshopResponseDTO dto2 = DescricaoWorkshopResponseDTO.fromEntity(descricao);

    // Assert
    assertEquals(dto1.getId(), dto2.getId());
    assertEquals(dto1.getTema(), dto2.getTema());
    assertEquals(dto1.getDescricao(), dto2.getDescricao());
    assertEquals(dto1.getUrlImagem(), dto2.getUrlImagem());
    assertEquals(dto1.getIdImagem(), dto2.getIdImagem());
  }

  @Test
  @DisplayName("Deve criar DescricaoWorkshopResponseDTO com todos os campos null exceto ID")
  void deveCriarDescricaoWorkshopResponseDTOComApenasId() {
    // Arrange
    DescricaoWorkshop descricaoMinima = new DescricaoWorkshop();
    descricaoMinima.setId(BigInteger.valueOf(1));
    descricaoMinima.setTema(null);
    descricaoMinima.setDescricao(null);
    descricaoMinima.setImagemWorkshop(null);

    // Act
    DescricaoWorkshopResponseDTO dto = DescricaoWorkshopResponseDTO.fromEntity(descricaoMinima);

    // Assert
    assertNotNull(dto);
    assertEquals(BigInteger.valueOf(1), dto.getId());
    assertNull(dto.getTema());
    assertNull(dto.getDescricao());
    assertNull(dto.getUrlImagem());
    assertNull(dto.getIdImagem());
  }
}

