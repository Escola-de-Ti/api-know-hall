package br.com.escoladeti.api_know_hall.dto.workshop;

import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopResponseDTO {

  private BigInteger id;
  private String titulo;
  private String linkMeet;
  private StatusWorkshop status;
  private BigInteger instrutorId;
  private String instrutorNome;

  // ✅ Adicionar anotações de formatação JSON
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
  private Timestamp dataCriacao;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
  private Timestamp dataInicio;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
  private Timestamp dataTermino;

  private DescricaoWorkshopResponseDTO descricao;

  public static WorkshopResponseDTO fromEntity(Workshop workshop) {
    WorkshopResponseDTO dto = new WorkshopResponseDTO();
    dto.setId(workshop.getId());
    dto.setTitulo(workshop.getTitulo());
    dto.setLinkMeet(workshop.getLinkMeet());
    dto.setStatus(workshop.getStatus());
    dto.setInstrutorId(workshop.getInstrutor().getId());
    dto.setInstrutorNome(workshop.getInstrutor().getNome());
    dto.setDataCriacao(workshop.getDataCriacao());
    dto.setDataInicio(workshop.getDataInicio());
    dto.setDataTermino(workshop.getDataTermino());

    if (workshop.getDescricao() != null) {
      dto.setDescricao(DescricaoWorkshopResponseDTO.fromEntity(workshop.getDescricao()));
    }

    return dto;
  }
}
