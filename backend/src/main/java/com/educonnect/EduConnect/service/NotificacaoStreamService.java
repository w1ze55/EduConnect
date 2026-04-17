package com.educonnect.EduConnect.service;

import com.educonnect.EduConnect.dto.NotificacaoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class NotificacaoStreamService {

    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersPorUsuario = new ConcurrentHashMap<>();

    public SseEmitter registrar(Long usuarioId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emittersPorUsuario.computeIfAbsent(usuarioId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable remover = () -> removerEmitter(usuarioId, emitter);
        emitter.onCompletion(remover);
        emitter.onTimeout(remover);
        emitter.onError(error -> removerEmitter(usuarioId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            removerEmitter(usuarioId, emitter);
        }

        return emitter;
    }

    public void enviar(Long usuarioId, NotificacaoDTO notificacao) {
        List<SseEmitter> emitters = emittersPorUsuario.get(usuarioId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notificacao").data(notificacao));
            } catch (IOException | IllegalStateException e) {
                log.debug("Removendo SSE de notificacao inativo para usuario {}", usuarioId);
                removerEmitter(usuarioId, emitter);
            }
        }
    }

    private void removerEmitter(Long usuarioId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersPorUsuario.get(usuarioId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersPorUsuario.remove(usuarioId);
        }
    }
}
