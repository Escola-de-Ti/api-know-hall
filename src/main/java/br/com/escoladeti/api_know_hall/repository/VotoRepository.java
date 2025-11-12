package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Voto;
import br.com.escoladeti.api_know_hall.enums.TipoVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface VotoRepository extends JpaRepository<Voto, BigInteger> {

  @Query(value = "SELECT * FROM votos v WHERE v.post_id = :postId AND v.usuario_id = :usuarioId AND v.tipo = :tipo", nativeQuery = true)
  Optional<Voto> findByPostIdAndUsuarioIdAndTipo(
    @Param("postId") BigInteger postId,
    @Param("usuarioId") BigInteger usuarioId,
    @Param("tipo") String tipo
  );

  @Query(value = "SELECT * FROM votos v WHERE v.comentario_id = :comentarioId AND v.usuario_id = :usuarioId AND v.tipo = :tipo", nativeQuery = true)
  Optional<Voto> findByComentarioIdAndUsuarioIdAndTipo(
    @Param("comentarioId") BigInteger comentarioId,
    @Param("usuarioId") BigInteger usuarioId,
    @Param("tipo") String tipo
  );

  @Query(value = "SELECT COUNT(*) FROM votos v WHERE v.post_id = :postId AND v.tipo = :tipo", nativeQuery = true)
  Long countByPostIdAndTipo(
    @Param("postId") BigInteger postId,
    @Param("tipo") String tipo
  );

  @Query(value = "SELECT COUNT(*) FROM votos v WHERE v.comentario_id = :comentarioId AND v.tipo = :tipo", nativeQuery = true)
  Long countByComentarioIdAndTipo(
    @Param("comentarioId") BigInteger comentarioId,
    @Param("tipo") String tipo
  );

  @Query(value = """
    SELECT v.* FROM votos v
    INNER JOIN comentario c ON v.comentario_id = c.id
    WHERE c.post_id = :postId
    AND v.usuario_id = :usuarioId
    AND v.tipo = :tipo
    """, nativeQuery = true)
  Optional<Voto> findSuperVoteByPostIdAndUsuarioId(
    @Param("postId") BigInteger postId,
    @Param("usuarioId") BigInteger usuarioId,
    @Param("tipo") String tipo
  );

  @Query(value = "SELECT COUNT(*) FROM votos v WHERE v.usuario_id = :usuarioId AND v.tipo = :tipo", nativeQuery = true)
  Optional<Integer> countByUsuarioIdAndTipo(@Param("usuarioId") BigInteger usuarioId, @Param("tipo") String tipo);
}
