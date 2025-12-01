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
@Table(name = "documentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do documento é obrigatório")
    @Column(nullable = false)
    private String nome;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(nullable = false)
    private String nomeArquivo;
    
    @Column(nullable = false)
    private String caminhoArquivo;
    
    @Column(nullable = false)
    private String tipoArquivo;
    
    @Column(nullable = false)
    private Long tamanhoArquivo; // em bytes
    
    @ManyToOne
    @JoinColumn(name = "criador_id", nullable = false)
    private Usuario criador;
    
    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;
    
    @Column(nullable = false)
    private Boolean requerAssinatura = false;
    
    @Column(nullable = false)
    private Boolean assinado = false;
    
    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssinaturaDocumento> assinaturas = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "documento_anexos", joinColumns = @JoinColumn(name = "documento_id"))
    @Column(name = "caminho_anexo")
    private List<String> anexos = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "documento_anexos_nomes", joinColumns = @JoinColumn(name = "documento_id"))
    @Column(name = "nome_anexo")
    private List<String> nomesAnexos = new ArrayList<>();
    
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

