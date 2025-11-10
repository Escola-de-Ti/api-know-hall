package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Inscricao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.exception.TokenInsuficienteException;
import br.com.escoladeti.api_know_hall.repository.InscricaoRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InscricaoService {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private WorkshopRepository workshopRepository;

  @Autowired
  private InscricaoRepository inscricaoRepository;

  public InscricaoResponseDTO inscrever(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));

    if (inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())) {
      throw new RuntimeException("Usuário já está inscrito neste workshop.");
    }

    if(workshop.getInstrutor().getId().equals(usuario.getId())) {
      throw new RuntimeException("Instrutor não pode se inscrever em seu próprio workshop.");
    }

    if (usuario.getQntdToken() < workshop.getCusto()) {
      throw new TokenInsuficienteException("Usuário não possui tokens suficientes para se inscrever neste workshop.");
    }

    if(workshop.getStatus() != StatusWorkshop.ABERTO) {
      throw new RuntimeException("Inscrições só podem ser feitas em workshops com status ABERTO.");
    }

    try {

      Inscricao inscricao = new Inscricao();
      inscricao.setUsuario(usuario);
      inscricao.setWorkshop(workshop);
      inscricao.setStatus(StatusInscricao.INSCRITO);

      Inscricao inscricaoSalva = inscricaoRepository.save(inscricao);
      usuario.setQntdToken(usuario.getQntdToken() - workshop.getCusto());
      usuarioRepository.save(usuario);
      Usuario instrutor = workshop.getInstrutor();
      instrutor.setQntdToken(instrutor.getQntdToken() + workshop.getCusto());
      usuarioRepository.save(instrutor);

      return toResponseDTO(inscricaoSalva);
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
      Usuario instrutor = workshop.getInstrutor();
      instrutor.setQntdToken(instrutor.getQntdToken() - workshop.getCusto());
      usuarioRepository.save(instrutor);
      
    } catch (Exception e) {
      throw new RuntimeException("Erro ao cancelar inscrição: " + e.getMessage(), e);
    }
  }

  public InscricaoResponseDTO buscarInscricao(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));
    Inscricao inscricao = inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())
      .orElseThrow(() -> new RuntimeException("Inscrição não encontrada para este usuário nesse workshop."));
    
    return toResponseDTO(inscricao);
  }

  public List<InscricaoResponseDTO> listarInscricoesPorUsuario(String email) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    List<Inscricao> inscricoes = inscricaoRepository.findByUsuarioId(usuario.getId())
      .orElseThrow(() -> new RuntimeException("Nenhuma inscrição encontrada para este usuário."));
    
    return inscricoes.stream()
      .map(this::toResponseDTO)
      .collect(Collectors.toList());
  }

  public List<InscricaoResponseDTO> listarInscricoesPorWorkshop(BigInteger workshopId) {
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));
    List<Inscricao> inscricoes = inscricaoRepository.findByWorkshopId(workshop.getId())
      .orElseThrow(() -> new RuntimeException("Nenhuma inscrição encontrada para este workshop."));
    
    return inscricoes.stream()
      .map(this::toResponseDTO)
      .collect(Collectors.toList());
  }

  public InscricaoResponseDTO atualizarStatusInscricao(BigInteger inscricaoId, StatusInscricao novoStatus) {
    Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
      .orElseThrow(() -> new RuntimeException("Inscrição não encontrada."));
    inscricao.setStatus(novoStatus);
    Inscricao inscricaoAtualizada = inscricaoRepository.save(inscricao);
    return toResponseDTO(inscricaoAtualizada);
  }
  
  private InscricaoResponseDTO toResponseDTO(Inscricao inscricao) {
    return InscricaoResponseDTO.builder()
      .id(inscricao.getId())
      .usuarioId(inscricao.getUsuario().getId())
      .usuarioNome(inscricao.getUsuario().getNome())
      .workshopId(inscricao.getWorkshop().getId())
      .workshopTitulo(inscricao.getWorkshop().getTitulo())
      .status(inscricao.getStatus())
      .dataInscricao(LocalDateTime.ofInstant(
        inscricao.getDataInscricao().toInstant(), 
        ZoneId.systemDefault()
      ))
      .build();
  }
}
