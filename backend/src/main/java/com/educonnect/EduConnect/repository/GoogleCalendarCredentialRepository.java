package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.GoogleCalendarCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleCalendarCredentialRepository extends JpaRepository<GoogleCalendarCredential, Long> {
    Optional<GoogleCalendarCredential> findByUsuario_Id(Long usuarioId);
    void deleteByUsuario_Id(Long usuarioId);
}
