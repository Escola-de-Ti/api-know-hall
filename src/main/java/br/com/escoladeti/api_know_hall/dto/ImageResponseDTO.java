package br.com.escoladeti.api_know_hall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageResponseDTO(@JsonProperty("Key") String key, @JsonProperty("Id") String id) {
}
