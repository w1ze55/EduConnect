package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    @Query("SELECT d FROM Documento d LEFT JOIN FETCH d.escola WHERE d.escola.id = :escolaId ORDER BY d.dataCriacao DESC")
    List<Documento> findByEscolaId(@Param("escolaId") Long escolaId);
    
    @Query("SELECT d FROM Documento d LEFT JOIN FETCH d.escola WHERE d.criador.id = :criadorId ORDER BY d.dataCriacao DESC")
    List<Documento> findByCriadorId(@Param("criadorId") Long criadorId);
    
    @Query("SELECT d FROM Documento d LEFT JOIN FETCH d.escola WHERE d.requerAssinatura = true AND d.assinado = false ORDER BY d.dataCriacao DESC")
    List<Documento> findDocumentosPendentesAssinatura();
    
    @Query("SELECT d FROM Documento d LEFT JOIN FETCH d.escola LEFT JOIN FETCH d.assinaturas a LEFT JOIN FETCH a.assinante WHERE d.id = :id")
    Documento findByIdWithAssinaturas(@Param("id") Long id);
}

