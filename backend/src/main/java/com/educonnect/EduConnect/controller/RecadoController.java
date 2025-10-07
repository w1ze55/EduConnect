package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.RecadoDTO;
import com.educonnect.EduConnect.model.Recado;
import com.educonnect.EduConnect.service.RecadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recados")
@RequiredArgsConstructor
public class RecadoController {
    
    private final RecadoService recadoService;
    
    @GetMapping
    public ResponseEntity<List<RecadoDTO>> getRecados() {
        List<RecadoDTO> recados = recadoService.getRecadosParaUsuario();
        return ResponseEntity.ok(recados);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecadoDTO> getRecadoById(@PathVariable Long id) {
        RecadoDTO recado = recadoService.getRecadoById(id);
        return ResponseEntity.ok(recado);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<RecadoDTO> criarRecado(@Valid @RequestBody Recado recado) {
        RecadoDTO novoRecado = recadoService.criarRecado(recado);
        return ResponseEntity.ok(novoRecado);
    }
    
    @PostMapping("/{id}/confirmar-leitura")
    public ResponseEntity<Void> confirmarLeitura(@PathVariable Long id) {
        recadoService.confirmarLeitura(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<RecadoDTO> atualizarRecado(
            @PathVariable Long id, 
            @Valid @RequestBody Recado recado) {
        RecadoDTO recadoAtualizado = recadoService.atualizarRecado(id, recado);
        return ResponseEntity.ok(recadoAtualizado);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<Void> deletarRecado(@PathVariable Long id) {
        recadoService.deletarRecado(id);
        return ResponseEntity.ok().build();
    }
}

