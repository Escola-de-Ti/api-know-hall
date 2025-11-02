package br.com.escoladeti.api_know_hall.dto.conquista;

import br.com.escoladeti.api_know_hall.enums.TierConquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConquistaCreateDTO {

  @NotBlank(message = "Nome é obrigatório")
  @Size(max = 255, message = "Nome não pode ter mais de 255 caracteres")
  private String nome;

  @NotBlank(message = "Descrição é obrigatória")
  private String descricao;

  @NotNull(message = "Tipo de conquista é obrigatório")
  private TipoConquista tipoConquista;

  @NotBlank(message = "Campo de validação é obrigatório")
  @Size(max = 100, message = "Campo de validação não pode ter mais de 100 caracteres")
  private String campoValidacao;

  private String iconeUrl;

  // Apenas para CERTIFICADO
  private BigInteger workshopId;

  @NotEmpty(message = "Pelo menos um tier é obrigatório")
  @Valid
  private Map<TierConquista, Integer> tiers;
}
