package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoResponseDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private String tipo;
    private LocalDateTime data;

    private Long criadorId;
    private String criadorNome;

    private Long escolaId;
    private String escolaNome;

    // Placeholders para compatibilidade futura com o front
    private Long turmaId;
    private String turmaNome;
    private Long alunoId;
    private String alunoNome;
}
