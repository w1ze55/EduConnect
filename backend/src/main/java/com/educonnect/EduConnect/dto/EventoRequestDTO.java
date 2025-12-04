package com.educonnect.EduConnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoRequestDTO {
    @NotBlank
    private String titulo;

    private String descricao;

    @NotNull
    private LocalDateTime data;

    @NotBlank
    private String tipo; // EVENTO, PROVA, REUNIAO, ATIVIDADE

    // Campos opcionais para futura extensao (turma/aluno/escola)
    private Long turmaId;
    private Long alunoId;
    private Long escolaId;
}
