package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.NotificacaoDTO;
import com.educonnect.EduConnect.dto.NotificacaoLeituraRequest;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.NotificacaoService;
import com.educonnect.EduConnect.service.NotificacaoStreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final NotificacaoStreamService notificacaoStreamService;

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacaoService.listar(usuario));
    }

    @PatchMapping("/{id}/leitura")
    public ResponseEntity<NotificacaoDTO> alterarLeitura(
            @PathVariable Long id,
            @Valid @RequestBody NotificacaoLeituraRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacaoService.alterarLeitura(id, request.getLida(), usuario));
    }

    @PatchMapping("/marcar-todas-lidas")
    public ResponseEntity<List<NotificacaoDTO>> marcarTodasComoLidas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacaoService.marcarTodasComoLidas(usuario));
    }

    @DeleteMapping
    public ResponseEntity<Void> limpar(@AuthenticationPrincipal Usuario usuario) {
        notificacaoService.limpar(usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal Usuario usuario) {
        return notificacaoStreamService.registrar(usuario.getId());
    }
}
