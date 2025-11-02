package br.com.escoladeti.api_know_hall.dto.post;

import java.math.BigInteger;
import java.util.List;

public record PostUpdateDTO(
  String titulo,
  String descricao,
  List<BigInteger> tagIds
) {}
