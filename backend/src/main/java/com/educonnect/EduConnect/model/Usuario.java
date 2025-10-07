package com.educonnect.EduConnect.model;

import com.educonnect.EduConnect.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;
    
    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true)
    private String cpf;
    
    private String telefone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(nullable = false)
    private Boolean ativo = true;
    
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
    
    // Campos específicos para ALUNO
    private String turma;
    private String matricula;
    
    // Campos específicos para PROFESSOR
    @ElementCollection
    @CollectionTable(name = "professor_disciplinas", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "disciplina")
    private List<String> disciplinas;
    
    @ElementCollection
    @CollectionTable(name = "professor_turmas", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "turma")
    private List<String> turmas;
    
    // Relacionamento com Escola
    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;
    
    // Relacionamento RESPONSAVEL <-> ALUNO
    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;
    
    @OneToMany(mappedBy = "responsavel")
    private List<Usuario> alunosVinculados;
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
    
    // UserDetails Implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return ativo;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return ativo;
    }
}

