package br.com.escoladeti.api_know_hall.dto.workshop;

import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopUpdateDTO {

  @Size(max = 255, message = "Título não pode ter mais de 255 caracteres")
  private String titulo;

  @Size(max = 500, message = "Link do Meet não pode ter mais de 500 caracteres")
  private String linkMeet;

  private StatusWorkshop status;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
  private Timestamp dataInicio;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
  private Timestamp dataTermino;

  @Valid
  private DescricaoWorkshopDTO descricao;

  private Integer capacidade;

  private Integer custo;
}
