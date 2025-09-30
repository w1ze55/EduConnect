package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Recado;
import com.educonnect.EduConnect.model.enums.CategoriaRecado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecadoRepository extends JpaRepository<Recado, Long> {
    List<Recado> findByCategoria(CategoriaRecado categoria);
    List<Recado> findByImportanteTrue();
    List<Recado> findByRemetenteIdOrderByDataEnvioDesc(Long remetenteId);
    List<Recado> findAllByOrderByDataEnvioDesc();
}

