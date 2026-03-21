package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.GoogleCalendarConnectResponseDTO;
import com.educonnect.EduConnect.dto.GoogleCalendarStatusDTO;
import com.educonnect.EduConnect.dto.GoogleCalendarSyncResponseDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<GoogleCalendarStatusDTO> status(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(googleCalendarService.getStatus(usuario));
    }

    @PostMapping("/connect")
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<GoogleCalendarConnectResponseDTO> connect(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new GoogleCalendarConnectResponseDTO(googleCalendarService.criarUrlAutorizacao(usuario)));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) String error
    ) {
        URI redirectUri = googleCalendarService.processarCallback(code, state, error);
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @PostMapping("/disconnect")
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<Void> disconnect(@AuthenticationPrincipal Usuario usuario) {
        googleCalendarService.desconectar(usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<GoogleCalendarSyncResponseDTO> sync(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(googleCalendarService.sincronizarEventosDoCriador(usuario));
    }
}
