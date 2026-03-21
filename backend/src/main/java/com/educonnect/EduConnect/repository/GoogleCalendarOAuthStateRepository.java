package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.GoogleCalendarOAuthState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GoogleCalendarOAuthStateRepository extends JpaRepository<GoogleCalendarOAuthState, Long> {
    Optional<GoogleCalendarOAuthState> findByStateValue(String stateValue);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
