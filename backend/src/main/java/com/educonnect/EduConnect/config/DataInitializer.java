package com.educonnect.EduConnect.config;

import com.educonnect.EduConnect.model.*;
import com.educonnect.EduConnect.model.enums.CategoriaRecado;
import com.educonnect.EduConnect.model.enums.Role;
import com.educonnect.EduConnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    private final RecadoRepository recadoRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // Já tem dados
        }
        
        // Criar Administrador
        Usuario admin = new Usuario();
        admin.setNome("Carlos Administrador");
        admin.setEmail("admin@educonnect.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setCpf("111.111.111-11");
        admin.setTelefone("(11) 98888-8888");
        admin.setRole(Role.ADMINISTRADOR);
        admin.setAtivo(true);
        admin = usuarioRepository.save(admin);
        
        // Criar Diretoria
        Usuario diretoria = new Usuario();
        diretoria.setNome("Patricia Direção");
        diretoria.setEmail("diretoria@educonnect.com");
        diretoria.setPassword(passwordEncoder.encode("dir123"));
        diretoria.setCpf("888.888.888-88");
        diretoria.setTelefone("(11) 98999-9999");
        diretoria.setRole(Role.DIRETORIA);
        diretoria.setAtivo(true);
        diretoria = usuarioRepository.save(diretoria);
        
        // Criar Professores
        Usuario professor1 = new Usuario();
        professor1.setNome("Maria Silva Santos");
        professor1.setEmail("professor@educonnect.com");
        professor1.setPassword(passwordEncoder.encode("prof123"));
        professor1.setCpf("222.222.222-22");
        professor1.setTelefone("(11) 98777-7777");
        professor1.setRole(Role.PROFESSOR);
        professor1.setAtivo(true);
        professor1.setDisciplinas(Arrays.asList("Matemática", "Física"));
        professor1.setTurmas(Arrays.asList("9º A", "9º B", "1º Ano"));
        professor1 = usuarioRepository.save(professor1);
        
        Usuario professor2 = new Usuario();
        professor2.setNome("João Pedro Oliveira");
        professor2.setEmail("joao.professor@educonnect.com");
        professor2.setPassword(passwordEncoder.encode("123456"));
        professor2.setCpf("333.333.333-33");
        professor2.setTelefone("(11) 98666-6666");
        professor2.setRole(Role.PROFESSOR);
        professor2.setAtivo(true);
        professor2.setDisciplinas(Arrays.asList("Português", "Literatura"));
        professor2.setTurmas(Arrays.asList("8º A", "9º A"));
        professor2 = usuarioRepository.save(professor2);
        
        // Criar Responsáveis
        Usuario responsavel1 = new Usuario();
        responsavel1.setNome("Mariana Souza");
        responsavel1.setEmail("responsavel@educonnect.com");
        responsavel1.setPassword(passwordEncoder.encode("resp123"));
        responsavel1.setCpf("666.666.666-66");
        responsavel1.setTelefone("(11) 98333-3333");
        responsavel1.setRole(Role.RESPONSAVEL);
        responsavel1.setAtivo(true);
        responsavel1 = usuarioRepository.save(responsavel1);
        
        Usuario responsavel2 = new Usuario();
        responsavel2.setNome("Roberto Costa");
        responsavel2.setEmail("roberto.responsavel@educonnect.com");
        responsavel2.setPassword(passwordEncoder.encode("123456"));
        responsavel2.setCpf("777.777.777-77");
        responsavel2.setTelefone("(11) 98222-2222");
        responsavel2.setRole(Role.RESPONSAVEL);
        responsavel2.setAtivo(true);
        responsavel2 = usuarioRepository.save(responsavel2);
        
        // Criar Alunos
        Usuario aluno1 = new Usuario();
        aluno1.setNome("Ana Carolina Souza");
        aluno1.setEmail("aluno@educonnect.com");
        aluno1.setPassword(passwordEncoder.encode("aluno123"));
        aluno1.setCpf("444.444.444-44");
        aluno1.setTelefone("(11) 98555-5555");
        aluno1.setRole(Role.ALUNO);
        aluno1.setAtivo(true);
        aluno1.setTurma("9º A");
        aluno1.setMatricula("2024001");
        aluno1.setResponsavel(responsavel1);
        aluno1 = usuarioRepository.save(aluno1);
        
        Usuario aluno2 = new Usuario();
        aluno2.setNome("Pedro Henrique Costa");
        aluno2.setEmail("pedro.aluno@educonnect.com");
        aluno2.setPassword(passwordEncoder.encode("123456"));
        aluno2.setCpf("555.555.555-55");
        aluno2.setTelefone("(11) 98444-4444");
        aluno2.setRole(Role.ALUNO);
        aluno2.setAtivo(true);
        aluno2.setTurma("8º B");
        aluno2.setMatricula("2024002");
        aluno2.setResponsavel(responsavel2);
        aluno2 = usuarioRepository.save(aluno2);
        
        // Criar Recados
        Recado recado1 = new Recado();
        recado1.setTitulo("Reunião de Pais e Mestres");
        recado1.setConteudo("Informamos que a reunião de pais e mestres será realizada no próximo sábado, dia 05/10/2025, das 9h às 12h.");
        recado1.setCategoria(CategoriaRecado.EVENTO);
        recado1.setImportante(true);
        recado1.setExigirConfirmacao(true);
        recado1.setRemetente(admin);
        recadoRepository.save(recado1);
        
        Recado recado2 = new Recado();
        recado2.setTitulo("Material necessário para aula prática");
        recado2.setConteudo("Para a próxima aula prática de Química, os alunos devem trazer jaleco, óculos de proteção e caderno de anotações.");
        recado2.setCategoria(CategoriaRecado.ACADEMICO);
        recado2.setImportante(false);
        recado2.setRemetente(professor1);
        recadoRepository.save(recado2);
        
        // Criar Atividades
        Atividade atividade1 = new Atividade();
        atividade1.setTitulo("Trabalho de Matemática - Equações");
        atividade1.setDescricao("Resolver os exercícios do capítulo 5 sobre equações do segundo grau.");
        atividade1.setDisciplina("Matemática");
        atividade1.setTurma("9º A");
        atividade1.setProfessor(professor1);
        atividade1.setDataEntrega(LocalDateTime.now().plusDays(3));
        atividade1.setValor(10.0);
        atividade1.setPeso(2);
        atividadeRepository.save(atividade1);
        
        // Criar Eventos
        Evento evento1 = new Evento();
        evento1.setTitulo("Prova de Matemática");
        evento1.setDescricao("Prova sobre equações e funções");
        evento1.setDataInicio(LocalDateTime.now().plusDays(5));
        evento1.setTipo("PROVA");
        evento1.setCriador(professor1);
        eventoRepository.save(evento1);
        
        Evento evento2 = new Evento();
        evento2.setTitulo("Feira de Ciências");
        evento2.setDescricao("Apresentação de projetos científicos");
        evento2.setDataInicio(LocalDateTime.now().plusDays(10));
        evento2.setDataFim(LocalDateTime.now().plusDays(10).plusHours(4));
        evento2.setTipo("EVENTO");
        evento2.setCriador(admin);
        eventoRepository.save(evento2);
        
        System.out.println("✅ Dados iniciais carregados com sucesso!");
        System.out.println("📧 Credenciais de teste:");
        System.out.println("   Admin: admin@educonnect.com / admin123");
        System.out.println("   Diretoria: diretoria@educonnect.com / dir123");
        System.out.println("   Professor: professor@educonnect.com / prof123");
        System.out.println("   Aluno: aluno@educonnect.com / aluno123");
        System.out.println("   Responsável: responsavel@educonnect.com / resp123");
    }
}

