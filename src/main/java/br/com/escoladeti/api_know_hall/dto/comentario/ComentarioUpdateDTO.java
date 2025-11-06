package br.com.escoladeti.api_know_hall.dto.comentario;

import jakarta.validation.constraints.NotBlank;

public record ComentarioUpdateDTO(
  @NotBlank(message = "O texto do comentário não pode estar vazio")
  String texto
) {}
