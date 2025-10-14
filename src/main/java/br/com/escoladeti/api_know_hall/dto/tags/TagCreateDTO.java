package br.com.escoladeti.api_know_hall.dto.tags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TagCreateDTO {


  @NotBlank(message = "Nome da tag é obrigatório")
  @Size(min = 2, max = 50, message = "Nome da tag deve ter entre 2 e 50 caracteres")
  private String name;

  public TagCreateDTO(String name) {
    this.name = name;
  }

  public TagCreateDTO() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
