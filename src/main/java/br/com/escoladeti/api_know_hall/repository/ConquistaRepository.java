package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.conquista.Conquista;
import br.com.escoladeti.api_know_hall.enums.TipoConquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConquistaRepository extends JpaRepository<Conquista, BigInteger> {

  List<Conquista> findByTipoConquista(TipoConquista tipo);

  @Query(nativeQuery = true,
          value = "SELECT * " +
                  "FROM CONQUISTA " +
                  "WHERE campo_validacao = :campo")
  List<Conquista> findByCampoValidacao(@Param("campo") String campo);

  @Query(nativeQuery = true,
          value = "SELECT DISTINCT c.* " +
                  "FROM CONQUISTA c " +
                  "LEFT JOIN CONQUISTA_TIER ct ON c.id = ct.conquista_id " +
                  "WHERE c.id = :id")
  Optional<Conquista> findByIdWithTiers(@Param("id") BigInteger id);
}
