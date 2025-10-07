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
@Table(name = "turmas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Turma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da turma é obrigatório")
    @Column(nullable = false)
    private String nome;
    
    @NotBlank(message = "Ano letivo é obrigatório")
    @Column(nullable = false)
    private String anoLetivo;
    
    @Column(nullable = false)
    private String serie;
    
    @Column(nullable = false)
    private String turno; // MANHÃ, TARDE, NOITE, INTEGRAL
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(nullable = false)
    private Boolean ativa = true;
    
    // Relacionamento com Escola
    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;
    
    // Professores vinculados à turma
    @ManyToMany
    @JoinTable(
        name = "turma_professores",
        joinColumns = @JoinColumn(name = "turma_id"),
        inverseJoinColumns = @JoinColumn(name = "professor_id")
    )
    private List<Usuario> professores = new ArrayList<>();
    
    // Alunos vinculados à turma
    @ManyToMany
    @JoinTable(
        name = "turma_alunos",
        joinColumns = @JoinColumn(name = "turma_id"),
        inverseJoinColumns = @JoinColumn(name = "aluno_id")
    )
    private List<Usuario> alunos = new ArrayList<>();
    
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}

