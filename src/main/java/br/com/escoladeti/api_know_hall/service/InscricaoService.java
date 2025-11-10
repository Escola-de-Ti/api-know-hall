package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.inscricao.InscricaoResponseDTO;
import br.com.escoladeti.api_know_hall.entity.Inscricao;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.StatusInscricao;
import br.com.escoladeti.api_know_hall.enums.StatusUsuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.exception.DuplicateResourceException;
import br.com.escoladeti.api_know_hall.exception.TokenInsuficienteException;
import br.com.escoladeti.api_know_hall.exception.UsuarioInativoException;
import br.com.escoladeti.api_know_hall.exception.ValidationException;
import br.com.escoladeti.api_know_hall.repository.InscricaoRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
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

  @Transactional
  public InscricaoResponseDTO inscrever(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));

    // Validação: Usuário deve estar ativo
    if (usuario.getStatusUsuario() != StatusUsuario.ATIVO) {
      throw new UsuarioInativoException("Apenas usuários ativos podem se inscrever em workshops.");
    }

    // Validação: Não pode estar já inscrito
    if (inscricaoRepository.existsByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())) {
      throw new DuplicateResourceException("Usuário já está inscrito neste workshop.");
    }

    // Validação: Instrutor não pode se inscrever no próprio workshop
    if (workshop.getInstrutor().getId().equals(usuario.getId())) {
      throw new ValidationException("Instrutor não pode se inscrever em seu próprio workshop.");
    }

    // Validação: Tokens suficientes
    if (usuario.getQntdToken() < workshop.getCusto()) {
      throw new TokenInsuficienteException("Usuário não possui tokens suficientes para se inscrever neste workshop.");
    }

    // Validação: Workshop deve estar ABERTO
    if (workshop.getStatus() != StatusWorkshop.ABERTO) {
      throw new ValidationException("Inscrições só podem ser feitas em workshops com status ABERTO.");
    }

    // Validação: Workshop não pode ter iniciado
    Timestamp agora = Timestamp.from(Instant.now());
    if (workshop.getDataInicio().before(agora)) {
      throw new ValidationException("Não é possível se inscrever em um workshop que já começou.");
    }

    // Criar inscrição
    Inscricao inscricao = new Inscricao();
    inscricao.setUsuario(usuario);
    inscricao.setWorkshop(workshop);
    inscricao.setStatus(StatusInscricao.INSCRITO);

    Inscricao inscricaoSalva = inscricaoRepository.save(inscricao);
    
    // Transferir tokens: usuário -> instrutor
    usuario.setQntdToken(usuario.getQntdToken() - workshop.getCusto());
    usuarioRepository.save(usuario);
    
    Usuario instrutor = workshop.getInstrutor();
    instrutor.setQntdToken(instrutor.getQntdToken() + workshop.getCusto());
    usuarioRepository.save(instrutor);

    return toResponseDTO(inscricaoSalva);
  }

  @Transactional
  public void cancelarInscricao(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));

    Inscricao inscricao = inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())
      .orElseThrow(() -> new EntityNotFoundException("Inscrição não encontrada para este usuário nesse workshop."));

    // Validação: Só pode cancelar se status for INSCRITO
    if (inscricao.getStatus() != StatusInscricao.INSCRITO) {
      throw new ValidationException("A inscrição não pode ser cancelada no status atual.");
    }

    // Validação: Não pode cancelar workshop que já começou
    Timestamp agora = Timestamp.from(Instant.now());
    if (workshop.getDataInicio().before(agora)) {
      throw new ValidationException("Não é possível cancelar inscrição em um workshop que já começou.");
    }

    // Atualizar status da inscrição
    inscricao.setStatus(StatusInscricao.CANCELADO);
    inscricaoRepository.save(inscricao);

    // Devolver tokens: instrutor -> usuário
    usuario.setQntdToken(usuario.getQntdToken() + workshop.getCusto());
    usuarioRepository.save(usuario);
    
    Usuario instrutor = workshop.getInstrutor();
    instrutor.setQntdToken(instrutor.getQntdToken() - workshop.getCusto());
    usuarioRepository.save(instrutor);
  }  public InscricaoResponseDTO buscarInscricao(String email, BigInteger workshopId) {
    Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("Email ou senha inválidos"));
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop com ID " + workshopId + " não encontrado"
      ));
    Inscricao inscricao = inscricaoRepository.findByUsuarioIdAndWorkshopId(usuario.getId(), workshop.getId())
      .orElseThrow(() -> new EntityNotFoundException("Inscrição não encontrada para este usuário nesse workshop."));
    
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
