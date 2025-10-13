package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Escola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscolaRepository extends JpaRepository<Escola, Long> {
    
    List<Escola> findByAtivoTrue();
    
    List<Escola> findByAtivoOrderByNomeAsc(Boolean ativo);
    
    boolean existsByEmail(String email);
}
