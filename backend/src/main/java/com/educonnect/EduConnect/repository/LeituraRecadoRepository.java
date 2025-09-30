package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.LeituraRecado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeituraRecadoRepository extends JpaRepository<LeituraRecado, Long> {
    Optional<LeituraRecado> findByRecadoIdAndUsuarioId(Long recadoId, Long usuarioId);
    List<LeituraRecado> findByUsuarioId(Long usuarioId);
    List<LeituraRecado> findByRecadoId(Long recadoId);
    boolean existsByRecadoIdAndUsuarioId(Long recadoId, Long usuarioId);
}

