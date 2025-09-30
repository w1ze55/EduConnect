package com.educonnect.EduConnect.model;

import com.educonnect.EduConnect.model.enums.CategoriaRecado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false)
    private String titulo;
    
    @NotBlank(message = "Conteúdo é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaRecado categoria;
    
    @Column(nullable = false)
    private Boolean importante = false;
    
    @Column(nullable = false)
    private Boolean exigirConfirmacao = false;
    
    @ManyToOne
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;
    
    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio;
    
    @OneToMany(mappedBy = "recado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeituraRecado> leituras = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "recado_anexos", joinColumns = @JoinColumn(name = "recado_id"))
    @Column(name = "url_anexo")
    private List<String> anexos = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        dataEnvio = LocalDateTime.now();
    }
}

