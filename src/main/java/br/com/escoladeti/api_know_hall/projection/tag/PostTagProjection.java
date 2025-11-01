package br.com.escoladeti.api_know_hall.projection.tag;

import java.math.BigInteger;

public interface PostTagProjection {
  BigInteger getPostId();
  BigInteger getTagId();
  String getTagName();
}
