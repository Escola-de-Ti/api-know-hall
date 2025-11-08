package br.com.escoladeti.api_know_hall.dto.workshop;

import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class WorkshopCreateDTO {

  @NotBlank(message = "Título é obrigatório")
  @Size(max = 255, message = "Título não pode ter mais de 255 caracteres")
  private String titulo;

  @Size(max = 500, message = "Link do Meet não pode ter mais de 500 caracteres")
  private String linkMeet;

  @NotNull(message = "Data de início é obrigatória")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
  private Timestamp dataInicio;

  @NotNull(message = "Data de término é obrigatória")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
  private Timestamp dataTermino;

  @Valid
  private DescricaoWorkshopDTO descricao;
}
