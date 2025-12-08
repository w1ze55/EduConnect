package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.EventoRequestDTO;
import com.educonnect.EduConnect.dto.EventoResponseDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendario/eventos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventoResponseDTO>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.listar(usuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventoResponseDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.buscarPorId(id, usuario));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<EventoResponseDTO> criar(
            @Valid @RequestBody EventoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        EventoResponseDTO criado = eventoService.criar(dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<EventoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.atualizar(id, dto, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR','DIRETORIA','ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        eventoService.deletar(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
