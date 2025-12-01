package com.educonnect.EduConnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "assinaturas_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssinaturaDocumento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario assinante;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String assinaturaBase64; // Assinatura em formato base64 (imagem)
    
    @Column(name = "data_assinatura", nullable = false, updatable = false)
    private LocalDateTime dataAssinatura;
    
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    
    @PrePersist
    protected void onCreate() {
        dataAssinatura = LocalDateTime.now();
    }
}

