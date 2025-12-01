package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoResumoDTO {
    private Long id;
    private String titulo;
    private String tipo;
    private Long criadorId;
    private String criadorNome;
    private Long escolaId;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
