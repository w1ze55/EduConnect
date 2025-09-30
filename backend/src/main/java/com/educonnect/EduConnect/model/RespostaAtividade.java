package com.educonnect.EduConnect.model;

import com.educonnect.EduConnect.model.enums.StatusAtividade;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "respostas_atividade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespostaAtividade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "atividade_id", nullable = false)
    private Atividade atividade;
    
    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;
    
    @Column(columnDefinition = "TEXT")
    private String comentario;
    
    @ElementCollection
    @CollectionTable(name = "resposta_anexos", joinColumns = @JoinColumn(name = "resposta_id"))
    @Column(name = "url_anexo")
    private List<String> anexos = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtividade status = StatusAtividade.PENDENTE;
    
    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;
    
    private Double nota;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "data_avaliacao")
    private LocalDateTime dataAvaliacao;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (status == StatusAtividade.ENTREGUE && dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
        if (status == StatusAtividade.AVALIADA && dataAvaliacao == null) {
            dataAvaliacao = LocalDateTime.now();
        }
    }
}

