package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.service.ImagemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
public class ImagemController {

  private final ImagemService imagemService;

  public ImagemController(ImagemService imagemService) {
    this.imagemService = imagemService;
  }

  @PostMapping("/upload")
  public ResponseEntity<String> upload(@RequestParam("image") MultipartFile image,
                                       @RequestParam(value = "userId", required = false) String userId) {
    try {
      if (userId == null || userId.isBlank()) userId = "test";
      String url = imagemService.uploadImage(image, userId);
      return ResponseEntity.ok(url);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Erro: " + e.getMessage());
    }
  }
}
