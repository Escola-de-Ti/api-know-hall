package br.com.escoladeti.api_know_hall.dto.comentario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record ComentarioCreateDTO(
  @NotNull(message = "O ID do post é obrigatório")
  BigInteger postId,

  @NotBlank(message = "O texto do comentário não pode estar vazio")
  String texto,

  BigInteger comentarioPaiId  // Opcional, para respostas
) {}
