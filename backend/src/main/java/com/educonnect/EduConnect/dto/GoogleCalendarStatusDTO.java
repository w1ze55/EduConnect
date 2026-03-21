package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoogleCalendarStatusDTO {
    private boolean configured;
    private boolean connected;
    private String calendarId;
    private LocalDateTime connectedAt;
    private long eventosCriados;
    private long eventosSincronizados;
}
