package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, BigInteger> {

  boolean existsByUsuarioIdAndWorkshopId(BigInteger usuarioId, BigInteger workshopId);

  Optional<Inscricao> findByUsuarioIdAndWorkshopId(BigInteger bigInteger, BigInteger workshopId);

  Optional<List<Inscricao>> findByWorkshopId(BigInteger workshopId);

  Optional<List<Inscricao>> findByUsuarioId(BigInteger usuarioId);
}
