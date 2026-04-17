package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Notificacao;
import com.educonnect.EduConnect.model.enums.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioIdAndOcultaFalseOrderByDataCriacaoDesc(Long usuarioId);

    Optional<Notificacao> findByIdAndUsuarioIdAndOcultaFalse(Long id, Long usuarioId);

    Optional<Notificacao> findByUsuarioIdAndTipoAndReferenciaTipoAndReferenciaId(
        Long usuarioId,
        TipoNotificacao tipo,
        String referenciaTipo,
        Long referenciaId
    );

    List<Notificacao> findByUsuarioIdAndOcultaFalse(Long usuarioId);

    List<Notificacao> findByReferenciaTipoAndReferenciaIdAndOcultaFalse(String referenciaTipo, Long referenciaId);
}
