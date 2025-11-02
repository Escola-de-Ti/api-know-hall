package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.conquista.UsuarioConquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface UsuarioConquistaRepository extends JpaRepository<UsuarioConquista, BigInteger> {

  List<UsuarioConquista> findByUsuarioId(BigInteger usuarioId);

  List<UsuarioConquista> findByConquistaId(BigInteger conquistaId);

  @Query(nativeQuery = true, value = "SELECT uc.* " +
    "FROM USUARIO_CONQUISTA uc " +
    "INNER JOIN CONQUISTA c ON uc.conquista_id = c.id " +
    "WHERE uc.usuario_id = :usuarioId " +
    " AND c.tipo_conquista = :tipo")
  List<UsuarioConquista> findByUsuarioIdAndTipo(
    @Param("usuarioId") BigInteger usuarioId,
    @Param("tipo") String tipo
  );

  boolean existsByUsuarioIdAndConquistaTierId(BigInteger usuarioId, BigInteger conquistaTierId);

  @Query(nativeQuery = true,
          value = "SELECT uc.* " +
                  "FROM USUARIO_CONQUISTA uc " +
                  "INNER JOIN CONQUISTA c ON uc.conquista_id = c.id " +
                  "INNER JOIN CONQUISTA_TIER ct ON uc.conquista_tier_id = ct.id " +
                  "WHERE uc.usuario_id = :usuarioId"
        )
  List<UsuarioConquista> findByUsuarioIdWithDetails(@Param("usuarioId") BigInteger usuarioId);

  @Query(value = "SELECT uc.* " +
    "FROM USUARIO_CONQUISTA uc " +
    "INNER JOIN CONQUISTA_TIER ct ON uc.conquista_tier_id = ct.id " +
    "WHERE uc.usuario_id = :usuarioId " +
    "AND uc.conquista_id = :conquistaId " +
    "ORDER BY CASE ct.tier " +
    "WHEN 'BRONZE' THEN 1 " +
    "WHEN 'PRATA' THEN 2 " +
    "WHEN 'OURO' THEN 3 " +
    "WHEN 'PLATINA' THEN 4 " +
    "WHEN 'DIAMANTE' THEN 5 " +
    "END ASC",
    nativeQuery = true)
  List<UsuarioConquista> findByUsuarioIdAndConquistaId(
    @Param("usuarioId") BigInteger usuarioId,
    @Param("conquistaId") BigInteger conquistaId
  );
}
