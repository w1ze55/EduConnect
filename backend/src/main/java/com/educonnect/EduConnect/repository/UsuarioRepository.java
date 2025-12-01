package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Usuario;
import com.educonnect.EduConnect.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCpf(String cpf);
    List<Usuario> findByRole(Role role);
    List<Usuario> findByAtivoTrue();
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.escola WHERE u.role = :role AND u.escola.id = :escolaId")
    List<Usuario> findAlunosByEscolaId(@Param("role") Role role, @Param("escolaId") Long escolaId);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.escola WHERE u.role = :role AND u.escola.id = :escolaId")
    List<Usuario> findUsuariosByRoleAndEscolaId(@Param("role") Role role, @Param("escolaId") Long escolaId);
}

