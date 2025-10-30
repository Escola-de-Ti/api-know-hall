package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ImagemRepository extends JpaRepository<Imagem, BigInteger> {
}
