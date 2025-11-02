package br.com.escoladeti.api_know_hall.dto.post;

import java.math.BigInteger;
import java.util.List;

public record FeedResponseDTO(
  List<PostFeedDTO> posts,
  Boolean hasMore,
  BigInteger lastPostId,
  Double lastScore
) {}
