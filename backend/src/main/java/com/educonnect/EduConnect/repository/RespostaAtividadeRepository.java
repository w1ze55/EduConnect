package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.RespostaAtividade;
import com.educonnect.EduConnect.model.enums.StatusAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RespostaAtividadeRepository extends JpaRepository<RespostaAtividade, Long> {
    Optional<RespostaAtividade> findByAtividadeIdAndAlunoId(Long atividadeId, Long alunoId);
    List<RespostaAtividade> findByAlunoId(Long alunoId);
    List<RespostaAtividade> findByAtividadeId(Long atividadeId);
    List<RespostaAtividade> findByStatus(StatusAtividade status);
}

