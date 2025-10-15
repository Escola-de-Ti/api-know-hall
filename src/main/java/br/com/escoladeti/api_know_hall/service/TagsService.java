package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.entity.Tag;
import br.com.escoladeti.api_know_hall.exception.PalavraProibidaException;
import br.com.escoladeti.api_know_hall.repository.TagsRepository;
import br.com.escoladeti.api_know_hall.service.utils.PalavrasProibidasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagsService {

  private final TagsRepository tagsRepository;
  private final PalavrasProibidasService palavrasProibidasService;

  @Transactional
  public Tag createOrGetExisting(String tagName) {
    String normalizedName = normalizeTagName(tagName);

    if (palavrasProibidasService.contemPalavraProibida(normalizedName)) {
      String palavraEncontrada = palavrasProibidasService.identificarPalavraProibida(normalizedName);
      throw new PalavraProibidaException(
        "A tag contém conteúdo inapropriado: " + palavraEncontrada
      );
    }

    return tagsRepository.findByName(normalizedName)
      .orElseGet(() -> {
        Tag newTag = new Tag();
        newTag.setName(normalizedName);
        return tagsRepository.save(newTag);
      });
  }

  public List<Tag> findMostPopular(int limit) {
    return tagsRepository.findMostPopularTags(limit);
  }

  private String normalizeTagName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Nome da tag não pode ser vazio");
    }
    return name.trim().toUpperCase();
  }

}
