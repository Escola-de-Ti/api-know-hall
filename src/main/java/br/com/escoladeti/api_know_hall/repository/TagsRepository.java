package br.com.escoladeti.api_know_hall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.escoladeti.api_know_hall.entity.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface TagsRepository extends JpaRepository<Tag, BigInteger> {

  Optional<Tag> findByName(String name);

  @Query(nativeQuery = true,
          value = "SELECT t.* " +
                  "FROM TAGS t " +
                  "LEFT JOIN USUARIO_TAGS ut ON ut.tag_id = t.id " +
                  "GROUP BY t.id " +
                  "ORDER BY COUNT(ut.usuario_id) DESC" +
                  "LIMIT :limit")
  List<Tag> findMostPopularTags(@Param("limit") int limit);
}
