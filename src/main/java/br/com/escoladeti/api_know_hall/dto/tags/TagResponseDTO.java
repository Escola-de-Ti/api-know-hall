package br.com.escoladeti.api_know_hall.dto.tags;

import br.com.escoladeti.api_know_hall.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagResponseDTO {

  private BigInteger id;
  private String name;

  public static TagResponseDTO fromEntity(Tag tag) {
    return TagResponseDTO.builder()
      .id(tag.getId())
      .name(tag.getName())
      .build();
  }
}
