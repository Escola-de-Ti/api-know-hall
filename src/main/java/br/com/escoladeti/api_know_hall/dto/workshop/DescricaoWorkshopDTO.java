package br.com.escoladeti.api_know_hall.dto.workshop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DescricaoWorkshopDTO {

  @NotBlank(message = "Tema é obrigatório")
  @Size(max = 255, message = "Tema não pode ter mais de 255 caracteres")
  private String tema;

  private String descricao;
}
