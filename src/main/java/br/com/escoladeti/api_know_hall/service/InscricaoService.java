package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.entity.Inscricao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import br.com.escoladeti.api_know_hall.exception.TokenInsuficienteException;
import br.com.escoladeti.api_know_hall.repository.InscricaoRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class InscricaoService {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private WorkshopRepository workshopRepository;

  @Autowired
  private InscricaoRepository inscricaoRepository;

  public void inscrever(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));

    if (inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())) {
      throw new RuntimeException("Usuário já está inscrito neste workshop.");
    }

    if (usuario.getQntdToken() < workshop.getCusto()) {
      throw new TokenInsuficienteException("Usuário não possui tokens suficientes para se inscrever neste workshop.");
    }
    try {

      Inscricao inscricao = new Inscricao();
      inscricao.setUsuario(usuario);
      inscricao.setWorkshop(workshop);
      inscricao.setStatus(StatusInscricao.INSCRITO);

      inscricaoRepository.save(inscricao);
      usuario.setQntdToken(usuario.getQntdToken() - workshop.getCusto());
      usuarioRepository.save(usuario);
    } catch (Exception e) {
      throw new RuntimeException("Erro ao processar inscrição: " + e.getMessage(), e);
    }
  }

  public void cancelarInscricao(String email, BigInteger workshopId) {

    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));

    Inscricao inscricao = inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())
      .orElseThrow(() -> new RuntimeException("Inscrição não encontrada para este usuário nesse workshop."));

    if (inscricao.getStatus() != StatusInscricao.INSCRITO) {
      throw new RuntimeException("A inscrição não pode ser cancelada no status atual.");
    }

    inscricao.setStatus(StatusInscricao.CANCELADO);
    try {
      inscricaoRepository.save(inscricao);

      usuario.setQntdToken(usuario.getQntdToken() + workshop.getCusto());
      usuarioRepository.save(usuario);
    } catch (Exception e) {
      throw new RuntimeException("Erro ao cancelar inscrição: " + e.getMessage(), e);
    }
  }

  public Inscricao buscarInscricao(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));
    return inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())
      .orElseThrow(() -> new RuntimeException("Inscrição não encontrada para este usuário nesse workshop."));

  }

  public List<Inscricao> listarInscricoesPorUsuario(String email) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    return inscricaoRepository.findByUsuarioId(usuario.getId())
      .orElseThrow(() -> new RuntimeException("Nenhuma inscrição encontrada para este usuário."));
  }

  public List<Inscricao> listarInscricoesPorWorkshop(BigInteger workshopId) {
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));
    return inscricaoRepository.findByWorkshopId(workshop.getId())
      .orElseThrow(() -> new RuntimeException("Nenhuma inscrição encontrada para este workshop."));
  }

  public void atualizarStatusInscricao(BigInteger inscricaoId, StatusInscricao novoStatus) {
    Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
      .orElseThrow(() -> new RuntimeException("Inscrição não encontrada."));
    inscricao.setStatus(novoStatus);
    inscricaoRepository.save(inscricao);
  }
}
