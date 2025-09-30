package com.educonnect.EduConnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leituras_recado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeituraRecado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "recado_id", nullable = false)
    private Recado recado;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "data_leitura", nullable = false)
    private LocalDateTime dataLeitura;
    
    @Column(nullable = false)
    private Boolean confirmada = false;
    
    @PrePersist
    protected void onCreate() {
        if (dataLeitura == null) {
            dataLeitura = LocalDateTime.now();
        }
    }
}

