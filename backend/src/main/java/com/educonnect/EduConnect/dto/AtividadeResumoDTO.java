package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AtividadeResumoDTO {
    private Long id;
    private String titulo;
    private String disciplina;
    private String turma;
    private Long professorId;
    private String professorNome;
    private LocalDateTime dataEntrega;
}
