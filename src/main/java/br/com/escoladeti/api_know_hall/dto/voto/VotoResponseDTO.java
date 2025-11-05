package br.com.escoladeti.api_know_hall.dto.voto;

public record VotoResponseDTO(
  boolean votado,
  Long totalUpVotes
) {}
