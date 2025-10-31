package br.com.escoladeti.api_know_hall.dto.tags;

import br.com.escoladeti.api_know_hall.entity.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Setter
@Getter
@Builder
public class TagResponseDTO {

  private BigInteger id;
  private String name;

  public TagResponseDTO(BigInteger id, String name) {
    this.id = id;
    this.name = name;
  }

  public TagResponseDTO() {
  }

  public static TagResponseDTO fromEntity(Tag tag) {
    return TagResponseDTO.builder()
      .id(tag.getId())
      .name(tag.getName())
      .build();
  }
}
