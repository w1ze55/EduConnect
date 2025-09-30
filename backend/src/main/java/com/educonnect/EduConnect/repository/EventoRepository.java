package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByTipo(String tipo);
    
    @Query("SELECT e FROM Evento e WHERE e.dataInicio >= :inicio AND e.dataInicio <= :fim ORDER BY e.dataInicio")
    List<Evento> findByPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT e FROM Evento e WHERE e.dataInicio >= :hoje ORDER BY e.dataInicio")
    List<Evento> findProximosEventos(LocalDateTime hoje);
}

