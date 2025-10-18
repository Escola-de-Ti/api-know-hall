package br.com.escoladeti.api_know_hall.dto.conquista;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificarProgressoDTO {

  @NotBlank(message = "Campo de validação é obrigatório")
  private String campoValidacao;

  @NotNull(message = "Progresso atual é obrigatório")
  @Min(value = 0, message = "Progresso não pode ser negativo")
  private Integer progressoAtual;
}
