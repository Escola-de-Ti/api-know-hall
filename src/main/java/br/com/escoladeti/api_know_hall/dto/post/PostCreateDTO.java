package br.com.escoladeti.api_know_hall.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;

public record PostCreateDTO(
  @NotNull(message = "Usuario ID é obrigatório")
  BigInteger usuarioId,
  @NotBlank(message = "Título é obrigatório")
  String titulo,
  String descricao,
  List<BigInteger> tagIds
) {}
