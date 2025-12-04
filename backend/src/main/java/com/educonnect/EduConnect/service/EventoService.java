package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.EventoRequestDTO;
import com.educonnect.EduConnect.dto.EventoResponseDTO;
import com.educonnect.EduConnect.model.Evento;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listar(Usuario usuarioLogado) {
        // Regra de visibilidade: todos autenticados veem todos os eventos;
        // diretores podem futuramente filtrar pela escola se desejado.
        return eventoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventoResponseDTO buscarPorId(Long id, Usuario usuarioLogado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento nao encontrado"));
        return toResponse(evento);
    }

    @Transactional
    public EventoResponseDTO criar(EventoRequestDTO dto, Usuario usuarioLogado) {
        validarPermissaoCriar(usuarioLogado);

        Evento evento = new Evento();
        aplicarRequestNoEvento(dto, evento);
        evento.setCriador(usuarioLogado);

        evento = eventoRepository.save(evento);
        return toResponse(evento);
    }

    @Transactional
    public EventoResponseDTO atualizar(Long id, EventoRequestDTO dto, Usuario usuarioLogado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento nao encontrado"));

        validarPermissaoAlterar(evento, usuarioLogado);
        aplicarRequestNoEvento(dto, evento);

        evento = eventoRepository.save(evento);
        return toResponse(evento);
    }

    @Transactional
    public void deletar(Long id, Usuario usuarioLogado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento nao encontrado"));

        validarPermissaoAlterar(evento, usuarioLogado);
        eventoRepository.delete(evento);
    }

    private void validarPermissaoCriar(Usuario usuario) {
        Role role = usuario.getRole();
        if (role != Role.ADMINISTRADOR && role != Role.DIRETORIA && role != Role.PROFESSOR) {
            throw new RuntimeException("Apenas professores, diretores e administradores podem criar eventos");
        }
    }

    private void validarPermissaoAlterar(Evento evento, Usuario usuario) {
        Role role = usuario.getRole();
        if (role == Role.ADMINISTRADOR || role == Role.DIRETORIA) {
            return;
        }
        if (role == Role.PROFESSOR) {
            if (evento.getCriador() == null || !evento.getCriador().getId().equals(usuario.getId())) {
                throw new RuntimeException("Professores so podem alterar os eventos que criaram");
            }
            return;
        }
        throw new RuntimeException("Voce nao tem permissao para alterar este evento");
    }

    private void aplicarRequestNoEvento(EventoRequestDTO dto, Evento evento) {
        evento.setTitulo(dto.getTitulo());
        evento.setDescricao(dto.getDescricao());
        evento.setTipo(dto.getTipo());
        // Front envia campo "data"; persistimos em dataInicio, e dataFim fica igual para compatibilidade.
        evento.setDataInicio(dto.getData());
        evento.setDataFim(dto.getData());
    }

    private EventoResponseDTO toResponse(Evento evento) {
        EventoResponseDTO dto = new EventoResponseDTO();
        dto.setId(evento.getId());
        dto.setTitulo(evento.getTitulo());
        dto.setDescricao(evento.getDescricao());
        dto.setTipo(evento.getTipo());
        dto.setData(evento.getDataInicio());

        if (evento.getCriador() != null) {
            dto.setCriadorId(evento.getCriador().getId());
            dto.setCriadorNome(evento.getCriador().getNome());
            if (evento.getCriador().getEscola() != null) {
                dto.setEscolaId(evento.getCriador().getEscola().getId());
                dto.setEscolaNome(evento.getCriador().getEscola().getNome());
            }
        }

        // Campos opcionais para o front; mantidos nulos por enquanto
        dto.setTurmaId(null);
        dto.setTurmaNome(null);
        dto.setAlunoId(null);
        dto.setAlunoNome(null);
        return dto;
    }
}
