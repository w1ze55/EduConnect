package com.educonnect.EduConnect.repository;

import com.educonnect.EduConnect.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    
    List<Turma> findByEscolaId(Long escolaId);
    
    List<Turma> findByAnoLetivoAndEscolaId(String anoLetivo, Long escolaId);
    
    @Query("SELECT t FROM Turma t JOIN t.professores p WHERE p.id = :professorId")
    List<Turma> findByProfessorId(@Param("professorId") Long professorId);
    
    @Query("SELECT t FROM Turma t JOIN t.alunos a WHERE a.id = :alunoId")
    List<Turma> findByAlunoId(@Param("alunoId") Long alunoId);
    
    List<Turma> findByAtivaTrue();
    
    @Query("SELECT t FROM Turma t WHERE t.escola.id = :escolaId AND t.ativa = true ORDER BY t.nome ASC")
    List<Turma> findAtivasByEscolaId(@Param("escolaId") Long escolaId);
}

