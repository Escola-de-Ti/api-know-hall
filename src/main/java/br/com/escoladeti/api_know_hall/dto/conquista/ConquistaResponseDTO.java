package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import lombok.*;

import java.math.BigInteger;
import java.util.List;

import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConquistaResponseDTO {

  private BigInteger id;
  private String nome;
  private String descricao;
  private TipoConquista tipoConquista;
  private String campoValidacao;
  private String iconeUrl;
  private List<ConquistaTierResponseDTO> tiers;

  public static ConquistaResponseDTO fromEntity(Conquista conquista) {
    return ConquistaResponseDTO.builder()
      .id(conquista.getId())
      .nome(conquista.getNome())
      .descricao(conquista.getDescricao())
      .tipoConquista(conquista.getTipoConquista())
      .campoValidacao(conquista.getCampoValidacao())
      .iconeUrl(conquista.getIconeUrl())
      .tiers(conquista.getTiers().stream()
        .map(ConquistaTierResponseDTO::fromEntity)
        .collect(Collectors.toList()))
      .build();
  }
}
