package br.com.escoladeti.api_know_hall.service;

import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopCreateDTO;
import br.com.escoladeti.api_know_hall.dto.workshop.WorkshopUpdateDTO;
import br.com.escoladeti.api_know_hall.entity.Imagem;
import br.com.escoladeti.api_know_hall.entity.Usuario;
import br.com.escoladeti.api_know_hall.entity.workshop.DescricaoWorkshop;
import br.com.escoladeti.api_know_hall.entity.workshop.Workshop;
import br.com.escoladeti.api_know_hall.enums.TipoUsuario;
import br.com.escoladeti.api_know_hall.enums.workshop.StatusWorkshop;
import br.com.escoladeti.api_know_hall.repository.DescricaoWorkshopRepository;
import br.com.escoladeti.api_know_hall.repository.UsuarioRepository;
import br.com.escoladeti.api_know_hall.repository.WorkshopRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {

  private final WorkshopRepository workshopRepository;
  private final DescricaoWorkshopRepository descricaoWorkshopRepository;
  private final UsuarioRepository usuarioRepository;

  @Transactional
  public Workshop criarWorkshop(WorkshopCreateDTO dto, String emailInstrutor) {

    Usuario instrutor = usuarioRepository.findByEmail(emailInstrutor)
      .orElseThrow(() -> new EntityNotFoundException(
        "Usuário não encontrado"
      ));

    if (instrutor.getTipoUsuario() != TipoUsuario.INSTRUTOR) {
      throw new IllegalArgumentException(
        "Apenas usuários do tipo INSTRUTOR podem criar workshops"
      );
    }

    if (!dto.getDataTermino().after(dto.getDataInicio())) {
      throw new IllegalArgumentException(
        "Data de término deve ser maior que data de início"
      );
    }

    if (dto.getCusto() == null || dto.getCusto() < 0) {
      throw new IllegalArgumentException("Custo do workshop é obrigatório e não pode ser negativo");
    }

    Workshop workshop = new Workshop();
    workshop.setTitulo(dto.getTitulo());
    workshop.setLinkMeet(dto.getLinkMeet());
    workshop.setInstrutor(instrutor);
    workshop.setDataInicio(dto.getDataInicio());
    workshop.setDataTermino(dto.getDataTermino());
    workshop.setCapacidade(dto.getCapacidade());

    workshop.setCusto(dto.getCusto());

    workshop.setStatus(determinarStatusInicial(dto.getDataInicio()));

    workshop = workshopRepository.save(workshop);

    if (dto.getDescricao() != null) {
      DescricaoWorkshop descricao = new DescricaoWorkshop();
      descricao.setTema(dto.getDescricao().getTema());
      descricao.setDescricao(dto.getDescricao().getDescricao());
      descricao.setWorkshop(workshop);

      descricao = descricaoWorkshopRepository.save(descricao);
      workshop.setDescricao(descricao);
    }

    return workshop;
  }

  private StatusWorkshop determinarStatusInicial(Timestamp dataInicio) {
    Timestamp agora = Timestamp.from(Instant.now());

    if (dataInicio.before(agora) || dataInicio.equals(agora)) {
      return StatusWorkshop.EM_ANDAMENTO;
    }

    return StatusWorkshop.ABERTO;
  }

  @Transactional(readOnly = true)
  public Workshop buscarPorId(BigInteger id) {
    return workshopRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException(
        "Workshop não encontrado"
      ));
  }

  @Transactional(readOnly = true)
  public List<Workshop> listarTodos() {
    return workshopRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Workshop> listarPorInstrutor(BigInteger instrutorId) {
    return workshopRepository.findByInstrutorId(instrutorId.longValue());
  }

  @Transactional(readOnly = true)
  public List<Workshop> listarPorStatus(StatusWorkshop status) {
    return workshopRepository.findByStatus(status.name());
  }

  @Transactional(readOnly = true)
  public List<Workshop> listarWorkshopsAbertos() {
    return workshopRepository.findWorkshopsAbertos();
  }

  @Transactional(readOnly = true)
  public List<Workshop> buscarPorTitulo(String termo) {
    return workshopRepository.findByTituloContaining(termo);
  }

  @Transactional
  public Workshop atualizarWorkshop(BigInteger id, WorkshopUpdateDTO dto, String emailInstrutor) {
    Workshop workshop = buscarPorId(id);

    Usuario instrutor = usuarioRepository.findByEmail(emailInstrutor)
      .orElseThrow(() -> new EntityNotFoundException(
        "Usuário não encontrado"
      ));

    if (!workshop.getInstrutor().getId().equals(instrutor.getId())) {
      throw new IllegalArgumentException(
        "Apenas o instrutor que criou o workshop pode atualizá-lo"
      );
    }

    if (dto.getTitulo() != null) {
      workshop.setTitulo(dto.getTitulo());
    }
    if (dto.getLinkMeet() != null) {
      workshop.setLinkMeet(dto.getLinkMeet());
    }
    if (dto.getCapacidade() != null) {
      workshop.setCapacidade(dto.getCapacidade());
    }

    if (dto.getDataInicio() != null || dto.getDataTermino() != null) {
      atualizarDatas(workshop, dto);
    }

    if (dto.getStatus() != null) {
      validarEAtualizarStatus(workshop, dto.getStatus());
    }

    if (dto.getDescricao() != null) {
      if (workshop.getDescricao() != null) {
        DescricaoWorkshop descricao = workshop.getDescricao();
        descricao.setTema(dto.getDescricao().getTema());
        descricao.setDescricao(dto.getDescricao().getDescricao());
        descricaoWorkshopRepository.save(descricao);
      } else {
        DescricaoWorkshop descricao = new DescricaoWorkshop();
        descricao.setTema(dto.getDescricao().getTema());
        descricao.setDescricao(dto.getDescricao().getDescricao());
        descricao.setWorkshop(workshop);
        descricao = descricaoWorkshopRepository.save(descricao);
        workshop.setDescricao(descricao);
      }
    }

    if (dto.getCusto() != null) {
      if (dto.getCusto() < 0) {
        throw new IllegalArgumentException("Custo não pode ser negativo");
      }
      workshop.setCusto(dto.getCusto());
    }

    return workshopRepository.save(workshop);
  }

  private void atualizarDatas(Workshop workshop, WorkshopUpdateDTO dto) {
    Timestamp novaDataInicio = dto.getDataInicio() != null ? dto.getDataInicio() : workshop.getDataInicio();
    Timestamp novaDataTermino = dto.getDataTermino() != null ? dto.getDataTermino() : workshop.getDataTermino();

    if (!novaDataTermino.after(novaDataInicio)) {
      throw new IllegalArgumentException(
        "Data de término deve ser maior que data de início"
      );
    }

    if (dto.getDataInicio() != null) {
      workshop.setDataInicio(dto.getDataInicio());

      Timestamp agora = Timestamp.from(Instant.now());
      if (dto.getDataInicio().before(agora) || dto.getDataInicio().equals(agora)) {
        if (workshop.getStatus() == StatusWorkshop.ABERTO) {
          workshop.setStatus(StatusWorkshop.EM_ANDAMENTO);
        }
      }
    }

    if (dto.getDataTermino() != null) {
      workshop.setDataTermino(dto.getDataTermino());
    }
  }


  private void validarEAtualizarStatus(Workshop workshop, StatusWorkshop novoStatus) {
    if (workshop.getStatus() == StatusWorkshop.CONCLUIDO && novoStatus != StatusWorkshop.CONCLUIDO) {
      throw new IllegalArgumentException(
        "Não é possível reabrir um workshop já concluído"
      );
    }

    Timestamp agora = Timestamp.from(Instant.now());

    if (novoStatus == StatusWorkshop.EM_ANDAMENTO) {
      if (workshop.getDataInicio().after(agora)) {
        throw new IllegalArgumentException(
          "Não é possível iniciar workshop antes da data de início prevista. " +
            "Data de início: " + workshop.getDataInicio()
        );
      }
    }

    if (novoStatus == StatusWorkshop.CONCLUIDO) {
      if (workshop.getStatus() != StatusWorkshop.EM_ANDAMENTO) {
        throw new IllegalArgumentException(
          "Apenas workshops EM_ANDAMENTO podem ser concluídos"
        );
      }
    }

    workshop.setStatus(novoStatus);
  }

  @Transactional
  public void deletarWorkshop(BigInteger id) {
    Workshop workshop = buscarPorId(id);
    workshopRepository.delete(workshop);
  }

  @Transactional(readOnly = true)
  public Long contarWorkshopsPorInstrutor(BigInteger instrutorId) {
    return workshopRepository.countByInstrutorId(instrutorId.longValue());
  }

  @Transactional
  public void atualizarImagemWorkshop(Imagem imagem, BigInteger workshopId) {
    Workshop workshop = workshopRepository.findById(workshopId)
      .orElseThrow(() -> new EntityNotFoundException("Workshop não encontrado"));

    DescricaoWorkshop descricaoWorkshop = workshop.getDescricao();
    if (descricaoWorkshop == null) {
      throw new EntityNotFoundException("Descrição do workshop não encontrada");
    }

    descricaoWorkshop.setImagemWorkshop(imagem);
    workshopRepository.save(workshop);
  }

  @Transactional
  public void removerImagemWorkshop(BigInteger imagemId) {
    descricaoWorkshopRepository.findByImagemWorkshopId(imagemId)
      .ifPresent(descricao -> {
        descricao.setImagemWorkshop(null);
        descricaoWorkshopRepository.save(descricao);
      });
  }

}
