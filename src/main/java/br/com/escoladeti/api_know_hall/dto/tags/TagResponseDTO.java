package br.com.escoladeti.api_know_hall.dto.tags;

import br.com.escoladeti.api_know_hall.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

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

  public BigInteger getId() {
    return id;
  }

  public void setId(BigInteger id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static TagResponseDTO fromEntity(Tag tag) {
    return TagResponseDTO.builder()
      .id(tag.getId())
      .name(tag.getName())
      .build();
  }
}
