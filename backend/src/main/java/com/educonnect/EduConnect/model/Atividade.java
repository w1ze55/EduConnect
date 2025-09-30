package com.educonnect.EduConnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atividades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atividade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @NotBlank(message = "Disciplina é obrigatória")
    @Column(nullable = false)
    private String disciplina;
    
    @Column(nullable = false)
    private String turma;
    
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;
    
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_entrega", nullable = false)
    private LocalDateTime dataEntrega;
    
    @Column(nullable = false)
    private Double valor = 10.0;
    
    @Column(nullable = false)
    private Integer peso = 1;
    
    @Column(nullable = false)
    private Integer tentativasPermitidas = 1;
    
    @ElementCollection
    @CollectionTable(name = "atividade_anexos", joinColumns = @JoinColumn(name = "atividade_id"))
    @Column(name = "url_anexo")
    private List<String> anexos = new ArrayList<>();
    
    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL)
    private List<RespostaAtividade> respostas = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}

