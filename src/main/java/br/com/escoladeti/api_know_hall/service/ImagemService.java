// java
package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.ImageResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.repository.ImagemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ImagemService {

  private final String supabaseUrl;

  private final String token;

  private final HttpClient httpClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private ImagemRepository imagemRepository;

  @Autowired
  private UsuarioService usuarioService;

  @Autowired
  private PostService postService;

  @Autowired
  private WorkshopService workshopService;

  public ImagemService(
    @Value("${supabase.url:${SUPABASE_URL:}}") String supabaseUrl,
    @Value("${supabase.token:${SUPABASE_TOKEN:}}") String token
  ) {

    if (supabaseUrl == null || supabaseUrl.isBlank()) {
      throw new IllegalStateException("Propriedade 'supabase.url' ou variável de ambiente 'SUPABASE_URL' não definida.");
    }

    this.token = token;
    this.supabaseUrl = supabaseUrl;
    this.httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();

  }

  public Imagem uploadImage(byte[] imageBytes, String userEmail, String type, String idType) {
    try {
      String imageName = userEmail + idType + java.util.UUID.randomUUID();
      URI uri = URI.create(supabaseUrl + "/assets/" + type + "/" + imageName);

      HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofMinutes(1))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "image/png")
        .PUT(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
        .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Falha ao enviar imagem para Supabase. Status: " + response.statusCode() + " Body: " + response.body());
      }
      ImageResponseDTO imageResponse = objectMapper.readValue(response.body(), ImageResponseDTO.class);

      Imagem imagem = new Imagem();
      imagem.setNome(imageName);
      imagem.setUrl(String.valueOf(uri));
      imagem.setPath(imageResponse.key());
      imagem.setIdImagem(imageResponse.id());

      imagemRepository.findByIdImagem(imagem.getIdImagem()).ifPresent(existingImage -> {
        imagem.setId(existingImage.getId());
      });

      imagemRepository.save(imagem);

      switch (type) {
        case "perfil" -> usuarioService.atualizarImagemPerfil(userEmail, imagem);
        case "post" -> {
          BigInteger postId = new BigInteger(idType);

          postService.atualizarImagemPerfil(imagem, 0, postId);
        }
        case "workshop" -> {
          BigInteger workshopId = new BigInteger(idType);
          workshopService.atualizarImagemWorkshop(imagem, workshopId);
        }
      }
      return imagem;

    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Erro ao enviar imagem para Supabase", e);
    }
  }

  public void UpdateImagem(BigInteger idImagem, byte[] imageBytes) {
    try {
      Imagem existingImage = imagemRepository.findById(idImagem)
        .orElseThrow(() -> new IllegalStateException("Imagem não encontrada com id: " + idImagem));

      URI uri = URI.create(supabaseUrl + existingImage.getPath().replace("files", ""));

      HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofMinutes(1))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "image/png")
        .PUT(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
        .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Falha ao enviar imagem para Supabase. Status: " + response.statusCode() + " Body: " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Erro ao enviar imagem para Supabase", e);
    }
  }

  public void deleteImage(BigInteger idImagem) {
    try {
      Imagem imagem = imagemRepository.findById(idImagem)
        .orElseThrow(() -> new IllegalStateException("Imagem não encontrada com id: " + idImagem));
      URI uri = URI.create(imagem.getUrl());

      HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofSeconds(30))
        .header("Authorization", "Bearer " + token)
        .DELETE()
        .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new IllegalStateException("Falha ao deletar imagem do Supabase. Status: " + response.statusCode() + " Body: " + response.body());
      }

      imagemRepository.deleteById(imagem.getId());

    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Erro ao deletar imagem do Supabase", e);
    }
  }
}
