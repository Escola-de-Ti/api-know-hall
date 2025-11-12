package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.projection.comentario.ComentarioProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, BigInteger> {

  @Query(value = """
    SELECT c.id as id,
           c.post_id as postId,
           c.usuario_id as usuarioId,
           u.nome as usuarioNome,
           c.texto as texto,
           c.total_up_votes as totalUpVotes,
           c.total_super_votes as totalSuperVotes,
           c.comentario_pai_id as comentarioPaiId,
           c.data_criacao as dataCriacao
    FROM comentario c
    INNER JOIN usuario u ON c.usuario_id = u.id
    WHERE c.post_id = :postId
      AND c.comentario_pai_id IS NULL
      AND (:lastComentarioId IS NULL OR c.id < :lastComentarioId)
    ORDER BY c.data_criacao DESC, c.id DESC
    LIMIT :pageSize
    """, nativeQuery = true)
  List<ComentarioProjection> findComentariosByPostId(
    @Param("postId") BigInteger postId,
    @Param("lastComentarioId") BigInteger lastComentarioId,
    @Param("pageSize") Integer pageSize
  );

  @Query(value = """
    SELECT c.id as id,
           c.post_id as postId,
           c.usuario_id as usuarioId,
           u.nome as usuarioNome,
           c.texto as texto,
           c.total_up_votes as totalUpVotes,
           c.total_super_votes as totalSuperVotes,
           c.comentario_pai_id as comentarioPaiId,
           c.data_criacao as dataCriacao
    FROM comentario c
    INNER JOIN usuario u ON c.usuario_id = u.id
    WHERE c.comentario_pai_id = :comentarioPaiId
      AND (:lastComentarioId IS NULL OR c.id < :lastComentarioId)
    ORDER BY c.data_criacao ASC, c.id ASC
    LIMIT :pageSize
    """, nativeQuery = true)
  List<ComentarioProjection> findRespostasByComentarioPaiId(
    @Param("comentarioPaiId") BigInteger comentarioPaiId,
    @Param("lastComentarioId") BigInteger lastComentarioId,
    @Param("pageSize") Integer pageSize
  );

  @Query(value = """
    SELECT c.id as id,
           c.post_id as postId,
           c.usuario_id as usuarioId,
           u.nome as usuarioNome,
           c.texto as texto,
           c.total_up_votes as totalUpVotes,
           c.total_super_votes as totalSuperVotes,
           c.comentario_pai_id as comentarioPaiId,
           c.data_criacao as dataCriacao
    FROM comentario c
    INNER JOIN usuario u ON c.usuario_id = u.id
    WHERE c.usuario_id = :usuarioId
      AND (:lastComentarioId IS NULL OR c.id < :lastComentarioId)
    ORDER BY c.data_criacao DESC, c.id DESC
    LIMIT :pageSize
    """, nativeQuery = true)
  List<ComentarioProjection> findComentariosByUsuarioId(
    @Param("usuarioId") BigInteger usuarioId,
    @Param("lastComentarioId") BigInteger lastComentarioId,
    @Param("pageSize") Integer pageSize
  );

  long countByPostIdAndRespostaDestaque(BigInteger postId, Boolean respostaDestaque);

  @Query("SELECT c FROM Comentario c " +
    "JOIN FETCH c.usuario " +
    "JOIN FETCH c.post p " +
    "JOIN FETCH p.usuario " +
    "WHERE c.id = :id")
  Optional<Comentario> findByIdWithRelations(@Param("id") BigInteger id);

  @Query("SELECT COUNT(*) FROM Comentario c " +
    "WHERE c.usuario.id = :usuarioId")
  Optional<Integer> countByUsuarioId(@Param("usuarioId") BigInteger usuarioId);
}
