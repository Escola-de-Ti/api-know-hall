package br.com.escoladeti.api_know_hall.controller;

import br.com.escoladeti.api_know_hall.dto.tags.TagCreateDTO;
import br.com.escoladeti.api_know_hall.dto.tags.TagResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.service.TagsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagsController {

  private final TagsService tagsService;

  @PostMapping
  public ResponseEntity<TagResponseDTO> createOrGetTag(
    @Valid @RequestBody TagCreateDTO tagCreateDTO) {

    Tag tag = tagsService.createOrGetExisting(tagCreateDTO.getName());
    TagResponseDTO response = TagResponseDTO.fromEntity(tag);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/popular")
  public ResponseEntity<List<TagResponseDTO>> getMostPopularTags() {
    List<Tag> tags = tagsService.findMostPopular(10);
    List<TagResponseDTO> response = tags.stream()
      .map(TagResponseDTO::fromEntity)
      .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }
}
