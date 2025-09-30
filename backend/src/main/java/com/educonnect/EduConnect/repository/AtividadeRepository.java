package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    List<Atividade> findByTurma(String turma);
    List<Atividade> findByDisciplina(String disciplina);
    List<Atividade> findByProfessorId(Long professorId);
    List<Atividade> findByTurmaOrderByDataEntregaDesc(String turma);
}

