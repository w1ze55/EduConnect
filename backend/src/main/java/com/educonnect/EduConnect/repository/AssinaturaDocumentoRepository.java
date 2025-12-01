package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.AssinaturaDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssinaturaDocumentoRepository extends JpaRepository<AssinaturaDocumento, Long> {
    
    List<AssinaturaDocumento> findByDocumentoId(Long documentoId);
    
    @Query("SELECT a FROM AssinaturaDocumento a WHERE a.documento.id = :documentoId AND a.assinante.id = :usuarioId")
    Optional<AssinaturaDocumento> findByDocumentoIdAndUsuarioId(@Param("documentoId") Long documentoId, @Param("usuarioId") Long usuarioId);
}

