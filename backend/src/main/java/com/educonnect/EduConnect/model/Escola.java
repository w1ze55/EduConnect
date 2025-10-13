package com.educonnect.EduConnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "escolas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Escola {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da escola é obrigatório")
    @Column(nullable = false)
    private String nome;
    
    @NotBlank(message = "Endereço é obrigatório")
    @Column(nullable = false)
    private String endereco;
    
    @NotBlank(message = "Telefone é obrigatório")
    @Column(nullable = false)
    private String telefone;
    
    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private Boolean ativo = true;
    
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
    
    // Diretor principal da escola
    @ManyToOne
    @JoinColumn(name = "diretor_id")
    private Usuario diretor;
    
    // Usuários vinculados à escola
    @OneToMany(mappedBy = "escola")
    private List<Usuario> usuarios;
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}
