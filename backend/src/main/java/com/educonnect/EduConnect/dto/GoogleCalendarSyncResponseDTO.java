package com.educonnect.EduConnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleCalendarSyncResponseDTO {
    private int totalEventos;
    private int sincronizados;
    private int falhas;
    private String mensagem;
}
