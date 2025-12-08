package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.RespostaAtividadeDTO;
import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.service.RespostaAtividadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RespostaAtividadeController {

    private final RespostaAtividadeService respostaAtividadeService;

    @PostMapping("/{id}/resposta")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<RespostaAtividadeDTO> enviarResposta(
            @PathVariable("id") Long atividadeId,
            @RequestParam(value = "comentario", required = false) String comentario,
            @RequestParam(value = "links", required = false) List<String> links,
            @RequestParam(value = "arquivos", required = false) MultipartFile[] arquivos,
            @AuthenticationPrincipal Usuario aluno) {
        RespostaAtividadeDTO resposta = respostaAtividadeService.enviarResposta(
                atividadeId,
                comentario,
                links,
                arquivos,
                aluno
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/{id}/respostas")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<RespostaAtividadeDTO> enviarRespostaAlias(
            @PathVariable("id") Long atividadeId,
            @RequestParam(value = "comentario", required = false) String comentario,
            @RequestParam(value = "links", required = false) List<String> links,
            @RequestParam(value = "arquivos", required = false) MultipartFile[] arquivos,
            @AuthenticationPrincipal Usuario aluno) {
        RespostaAtividadeDTO resposta = respostaAtividadeService.enviarResposta(
                atividadeId,
                comentario,
                links,
                arquivos,
                aluno
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{id}/respostas")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR')")
    public ResponseEntity<List<RespostaAtividadeDTO>> listarRespostas(
            @PathVariable("id") Long atividadeId,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(respostaAtividadeService.listarRespostasPorAtividade(atividadeId, usuario));
    }

    @GetMapping("/{id}/minha-resposta")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<RespostaAtividadeDTO> buscarRespostaDoAluno(
            @PathVariable("id") Long atividadeId,
            @AuthenticationPrincipal Usuario aluno) {
        return ResponseEntity.ok(respostaAtividadeService.buscarRespostaDoAluno(atividadeId, aluno));
    }
}
