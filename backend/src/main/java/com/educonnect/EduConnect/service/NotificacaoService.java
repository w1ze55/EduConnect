package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.NotificacaoDTO;
import com.educonnect.EduConnect.model.Notificacao;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.TipoNotificacao;
import com.educonnect.EduConnect.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final NotificacaoStreamService notificacaoStreamService;

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> listar(Usuario usuario) {
        return notificacaoRepository.findByUsuarioIdAndOcultaFalseOrderByDataCriacaoDesc(usuario.getId())
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public NotificacaoDTO alterarLeitura(Long id, Boolean lida, Usuario usuario) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuarioIdAndOcultaFalse(id, usuario.getId())
            .orElseThrow(() -> new RuntimeException("Notificacao nao encontrada"));

        aplicarLeitura(notificacao, lida);
        return toDTO(notificacaoRepository.save(notificacao));
    }

    @Transactional
    public List<NotificacaoDTO> marcarTodasComoLidas(Usuario usuario) {
        List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioIdAndOcultaFalse(usuario.getId());
        notificacoes.forEach(notificacao -> aplicarLeitura(notificacao, true));
        return notificacaoRepository.saveAll(notificacoes)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void limpar(Usuario usuario) {
        List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioIdAndOcultaFalse(usuario.getId());
        notificacoes.forEach(notificacao -> notificacao.setOculta(true));
        notificacaoRepository.saveAll(notificacoes);
    }

    @Transactional
    public void notificarUsuarios(
            Collection<Usuario> usuarios,
            Long remetenteId,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String link,
            String referenciaTipo,
            Long referenciaId) {
        if (usuarios == null || usuarios.isEmpty() || referenciaId == null) {
            return;
        }

        Map<Long, Usuario> usuariosUnicos = new LinkedHashMap<>();
        for (Usuario usuario : usuarios) {
            if (usuario == null || usuario.getId() == null) {
                continue;
            }
            if (remetenteId != null && remetenteId.equals(usuario.getId())) {
                continue;
            }
            if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                continue;
            }
            usuariosUnicos.put(usuario.getId(), usuario);
        }

        for (Usuario usuario : usuariosUnicos.values()) {
            boolean jaExiste = notificacaoRepository
                .findByUsuarioIdAndTipoAndReferenciaTipoAndReferenciaId(
                    usuario.getId(),
                    tipo,
                    referenciaTipo,
                    referenciaId
                )
                .isPresent();

            if (jaExiste) {
                continue;
            }

            Notificacao notificacao = new Notificacao();
            notificacao.setUsuario(usuario);
            notificacao.setTipo(tipo);
            notificacao.setTitulo(titulo);
            notificacao.setMensagem(mensagem);
            notificacao.setLink(link);
            notificacao.setReferenciaTipo(referenciaTipo);
            notificacao.setReferenciaId(referenciaId);
            notificacao.setLida(false);
            notificacao.setOculta(false);

            Notificacao salva = notificacaoRepository.save(notificacao);
            notificacaoStreamService.enviar(usuario.getId(), toDTO(salva));
        }
    }

    @Transactional
    public void ocultarPorReferencia(String referenciaTipo, Long referenciaId) {
        if (referenciaTipo == null || referenciaId == null) {
            return;
        }

        List<Notificacao> notificacoes = notificacaoRepository
            .findByReferenciaTipoAndReferenciaIdAndOcultaFalse(referenciaTipo, referenciaId);

        notificacoes.forEach(notificacao -> notificacao.setOculta(true));
        notificacaoRepository.saveAll(notificacoes);
    }

    private void aplicarLeitura(Notificacao notificacao, Boolean lida) {
        boolean novaLeitura = Boolean.TRUE.equals(lida);
        notificacao.setLida(novaLeitura);
        notificacao.setDataLeitura(novaLeitura ? LocalDateTime.now() : null);
    }

    private NotificacaoDTO toDTO(Notificacao notificacao) {
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setId(notificacao.getId());
        dto.setTipo(notificacao.getTipo());
        dto.setTitulo(notificacao.getTitulo());
        dto.setMensagem(notificacao.getMensagem());
        dto.setLink(notificacao.getLink());
        dto.setReferenciaTipo(notificacao.getReferenciaTipo());
        dto.setReferenciaId(notificacao.getReferenciaId());
        dto.setLida(notificacao.getLida());
        dto.setDataCriacao(notificacao.getDataCriacao());
        dto.setDataLeitura(notificacao.getDataLeitura());
        return dto;
    }
}
