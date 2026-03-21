package com.educonnect.EduConnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "google_calendar_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "calendar_id", nullable = false)
    private String calendarId = "primary";

    @Lob
    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Lob
    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private LocalDateTime connectedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        connectedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
