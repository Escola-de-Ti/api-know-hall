package br.com.escoladeti.api_know_hall.repository;

import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface ImagemRepository extends JpaRepository<Imagem, BigInteger> {

  Optional<Imagem> findByIdImagem(String id_imagem);

}
