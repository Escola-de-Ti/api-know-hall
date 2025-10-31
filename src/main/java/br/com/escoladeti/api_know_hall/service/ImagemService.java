// java
package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.ImageResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Service
public class ImagemService {

  private final String supabaseUrl;

  private final String token;

  private final HttpClient httpClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

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

  public Imagem uploadImage(byte[] imageBytes, String imageName) {
    try {
      URI uri = URI.create(supabaseUrl + "/assets/" + imageName);

      HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofMinutes(1))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "image/png")
        .PUT(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
        .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      ImageResponseDTO imageResponse = objectMapper.readValue(response.body(), ImageResponseDTO.class);

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Falha ao enviar imagem para Supabase. Status: " + response.statusCode() + " Body: " + response.body());
      }

      Imagem imagem = new Imagem();
      imagem.setNome(imageName);
      imagem.setUrl(supabaseUrl + "/assets/" + imageName);
      imagem.setPath(imageResponse.key());
      imagem.setId_imagem(imageResponse.id());
      return imagem;

    } catch (IOException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Erro ao enviar imagem para Supabase", e);
    }
  }


}
