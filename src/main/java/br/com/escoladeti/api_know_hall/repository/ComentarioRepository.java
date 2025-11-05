package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Comentario;
import br.com.escoladeti.api_know_hall.projection.comentario.ComentarioProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, BigInteger> {

  @Query("""
    SELECT c.id as id,
           c.post.id as postId,
           c.usuario.id as usuarioId,
           c.usuario.nome as usuarioNome,
           c.texto as texto,
           c.totalUpVotes as totalUpVotes,
           c.totalSuperVotes as totalSuperVotes,
           c.comentarioPai.id as comentarioPaiId,
           c.dataCriacao as dataCriacao
    FROM Comentario c
    WHERE c.post.id = :postId
      AND c.comentarioPai IS NULL
      AND (:lastComentarioId IS NULL OR c.id < :lastComentarioId)
    ORDER BY c.dataCriacao DESC, c.id DESC
    """)
  List<ComentarioProjection> findComentariosByPostId(
    @Param("postId") BigInteger postId,
    @Param("lastComentarioId") BigInteger lastComentarioId,
    @Param("pageSize") Integer pageSize
  );

  @Query("""
    SELECT c.id as id,
           c.post.id as postId,
           c.usuario.id as usuarioId,
           c.usuario.nome as usuarioNome,
           c.texto as texto,
           c.totalUpVotes as totalUpVotes,
           c.totalSuperVotes as totalSuperVotes,
           c.comentarioPai.id as comentarioPaiId,
           c.dataCriacao as dataCriacao
    FROM Comentario c
    WHERE c.comentarioPai.id = :comentarioPaiId
      AND (:lastComentarioId IS NULL OR c.id < :lastComentarioId)
    ORDER BY c.dataCriacao ASC, c.id ASC
    """)
  List<ComentarioProjection> findRespostasByComentarioPaiId(
    @Param("comentarioPaiId") BigInteger comentarioPaiId,
    @Param("lastComentarioId") BigInteger lastComentarioId,
    @Param("pageSize") Integer pageSize
  );

  @Query("""
    SELECT c.id as id,
           c.post.id as postId,
           c.usuario.id as usuarioId,
           c.usuario.nome as usuarioNome,
           c.texto as texto,
           c.totalUpVotes as totalUpVotes,
           c.totalSuperVotes as totalSuperVotes,
           c.comentarioPai.id as comentarioPaiId,
           c.dataCriacao as dataCriacao
    FROM Comentario c
    WHERE c.usuario.id = :usuarioId
      AND (:lastComentarioId IS NULL OR c.id < :lastComentarioId)
    ORDER BY c.dataCriacao DESC, c.id DESC
    """)
  List<ComentarioProjection> findComentariosByUsuarioId(
    @Param("usuarioId") BigInteger usuarioId,
    @Param("lastComentarioId") BigInteger lastComentarioId,
    @Param("pageSize") Integer pageSize
  );
}
