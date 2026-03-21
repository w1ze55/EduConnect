package com.educonnect.EduConnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;
    
    @Column(name = "data_fim")
    private LocalDateTime dataFim;
    
    private String tipo; // PROVA, REUNIAO, EVENTO, FERIADO

    @Column(name = "google_calendar_event_id")
    private String googleCalendarEventId;

    @Column(name = "google_calendar_last_synced_at")
    private LocalDateTime googleCalendarLastSyncedAt;

    @Column(name = "google_calendar_sync_error", columnDefinition = "TEXT")
    private String googleCalendarSyncError;
    
    @ManyToOne
    @JoinColumn(name = "criador_id")
    private Usuario criador;
}

