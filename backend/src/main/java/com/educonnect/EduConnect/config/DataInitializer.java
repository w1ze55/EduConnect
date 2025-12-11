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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final TurmaRepository turmaRepository;
    private final RecadoRepository recadoRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // Já tem dados
        }
        
        System.out.println("🚀 Iniciando carga de dados iniciais...");
        
        // ==================== CRIAR ESCOLAS ====================
        System.out.println("📚 Criando escolas...");
        
        Escola escola1 = new Escola();
        escola1.setNome("Escola Municipal João Paulo II");
        escola1.setEndereco("Rua das Flores, 123 - Centro");
        escola1.setTelefone("(11) 3456-7890");
        escola1.setEmail("contato@escolajoaopaulo.edu.br");
        escola1.setAtivo(true);
        escola1 = escolaRepository.save(escola1);
        
        Escola escola2 = new Escola();
        escola2.setNome("Colégio Estadual Maria Clara");
        escola2.setEndereco("Av. Principal, 456 - Jardim América");
        escola2.setTelefone("(11) 3789-0123");
        escola2.setEmail("secretaria@mariaclara.edu.br");
        escola2.setAtivo(true);
        escola2 = escolaRepository.save(escola2);
        
        System.out.println("✅ 2 escolas criadas");
        
        // ==================== CRIAR ADMINISTRADOR ====================
        System.out.println("👤 Criando administrador...");
        
        Usuario admin = new Usuario();
        admin.setNome("Carlos Administrador");
        admin.setEmail("admin@educonnect.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setCpf("111.111.111-11");
        admin.setTelefone("(11) 98888-8888");
        admin.setRole(Role.ADMINISTRADOR);
        admin.setAtivo(true);
        admin = usuarioRepository.save(admin);
        
        // ==================== CRIAR DIRETORES ====================
        System.out.println("👔 Criando diretores...");
        
        Usuario diretor1 = new Usuario();
        diretor1.setNome("Patricia Direção Silva");
        diretor1.setEmail("diretoria@educonnect.com");
        diretor1.setPassword(passwordEncoder.encode("dir123"));
        diretor1.setCpf("888.888.888-88");
        diretor1.setTelefone("(11) 98999-9999");
        diretor1.setRole(Role.DIRETORIA);
        diretor1.setAtivo(true);
        diretor1.setEscola(escola1);
        diretor1 = usuarioRepository.save(diretor1);
        
        // Atribuir diretor à escola
        escola1.setDiretor(diretor1);
        escola1 = escolaRepository.save(escola1);
        
        Usuario diretor2 = new Usuario();
        diretor2.setNome("Roberto Santos Costa");
        diretor2.setEmail("roberto.diretor@educonnect.com");
        diretor2.setPassword(passwordEncoder.encode("dir123"));
        diretor2.setCpf("999.999.999-99");
        diretor2.setTelefone("(11) 98777-7777");
        diretor2.setRole(Role.DIRETORIA);
        diretor2.setAtivo(true);
        diretor2.setEscola(escola2);
        diretor2 = usuarioRepository.save(diretor2);
        
        escola2.setDiretor(diretor2);
        escola2 = escolaRepository.save(escola2);
        
        System.out.println("✅ 2 diretores criados e vinculados às escolas");
        
        // ==================== CRIAR PROFESSORES ====================
        System.out.println("👨‍🏫 Criando professores...");
        
        // Professores Escola 1
        Usuario prof1 = new Usuario();
        prof1.setNome("Maria Silva Santos");
        prof1.setEmail("professor@educonnect.com");
        prof1.setPassword(passwordEncoder.encode("prof123"));
        prof1.setCpf("222.222.222-22");
        prof1.setTelefone("(11) 98777-7777");
        prof1.setRole(Role.PROFESSOR);
        prof1.setAtivo(true);
        prof1.setEscola(escola1);
        prof1.setDisciplinas(Arrays.asList("Matemática", "Física"));
        prof1 = usuarioRepository.save(prof1);
        
        Usuario prof2 = new Usuario();
        prof2.setNome("João Pedro Oliveira");
        prof2.setEmail("joao.professor@educonnect.com");
        prof2.setPassword(passwordEncoder.encode("prof123"));
        prof2.setCpf("333.333.333-33");
        prof2.setTelefone("(11) 98666-6666");
        prof2.setRole(Role.PROFESSOR);
        prof2.setAtivo(true);
        prof2.setEscola(escola1);
        prof2.setDisciplinas(Arrays.asList("Português", "Literatura"));
        prof2 = usuarioRepository.save(prof2);
        
        Usuario prof3 = new Usuario();
        prof3.setNome("Ana Paula Costa");
        prof3.setEmail("ana.professor@educonnect.com");
        prof3.setPassword(passwordEncoder.encode("prof123"));
        prof3.setCpf("444.444.444-44");
        prof3.setTelefone("(11) 98555-5555");
        prof3.setRole(Role.PROFESSOR);
        prof3.setAtivo(true);
        prof3.setEscola(escola1);
        prof3.setDisciplinas(Arrays.asList("História", "Geografia"));
        prof3 = usuarioRepository.save(prof3);
        
        // Professores Escola 2
        Usuario prof4 = new Usuario();
        prof4.setNome("Carlos Eduardo Mendes");
        prof4.setEmail("carlos.professor@educonnect.com");
        prof4.setPassword(passwordEncoder.encode("prof123"));
        prof4.setCpf("555.555.555-55");
        prof4.setTelefone("(11) 98444-4444");
        prof4.setRole(Role.PROFESSOR);
        prof4.setAtivo(true);
        prof4.setEscola(escola2);
        prof4.setDisciplinas(Arrays.asList("Química", "Biologia"));
        prof4 = usuarioRepository.save(prof4);
        
        Usuario prof5 = new Usuario();
        prof5.setNome("Juliana Ferreira Lima");
        prof5.setEmail("juliana.professor@educonnect.com");
        prof5.setPassword(passwordEncoder.encode("prof123"));
        prof5.setCpf("666.666.666-66");
        prof5.setTelefone("(11) 98333-3333");
        prof5.setRole(Role.PROFESSOR);
        prof5.setAtivo(true);
        prof5.setEscola(escola2);
        prof5.setDisciplinas(Arrays.asList("Inglês", "Artes"));
        prof5 = usuarioRepository.save(prof5);
        
        System.out.println("✅ 5 professores criados");
        
        // ==================== CRIAR TURMAS ====================
        System.out.println("📋 Criando turmas...");
        
        // Turmas Escola 1
        List<Turma> turmasEscola1 = new ArrayList<>();
        
        Turma turma1E1 = criarTurma("3º Ano A", "2025", "3º Ano", "MANHÃ", escola1);
        turmasEscola1.add(turma1E1);
        
        Turma turma2E1 = criarTurma("3º Ano B", "2025", "3º Ano", "TARDE", escola1);
        turmasEscola1.add(turma2E1);
        
        Turma turma3E1 = criarTurma("4º Ano A", "2025", "4º Ano", "MANHÃ", escola1);
        turmasEscola1.add(turma3E1);
        
        Turma turma4E1 = criarTurma("5º Ano A", "2025", "5º Ano", "TARDE", escola1);
        turmasEscola1.add(turma4E1);
        
        Turma turma5E1 = criarTurma("6º Ano A", "2025", "6º Ano", "MANHÃ", escola1);
        turmasEscola1.add(turma5E1);
        
        // Turmas Escola 2
        List<Turma> turmasEscola2 = new ArrayList<>();
        
        Turma turma1E2 = criarTurma("7º Ano A", "2025", "7º Ano", "MANHÃ", escola2);
        turmasEscola2.add(turma1E2);
        
        Turma turma2E2 = criarTurma("8º Ano A", "2025", "8º Ano", "TARDE", escola2);
        turmasEscola2.add(turma2E2);
        
        Turma turma3E2 = criarTurma("9º Ano A", "2025", "9º Ano", "MANHÃ", escola2);
        turmasEscola2.add(turma3E2);
        
        Turma turma4E2 = criarTurma("Ensino Médio 1º A", "2025", "Ensino Médio 1º", "TARDE", escola2);
        turmasEscola2.add(turma4E2);
        
        Turma turma5E2 = criarTurma("Ensino Médio 2º A", "2025", "Ensino Médio 2º", "MANHÃ", escola2);
        turmasEscola2.add(turma5E2);
        
        System.out.println("✅ 10 turmas criadas (5 por escola)");
        
        // ==================== ASSOCIAR PROFESSORES ÀS TURMAS ====================
        System.out.println("👨‍🏫 Associando professores às turmas...");
        
        // Professores da Escola 1
        turma1E1.getProfessores().add(prof1); // Maria - Matemática e Física
        turma1E1.getProfessores().add(prof2); // João - Português e Literatura
        
        turma2E1.getProfessores().add(prof1); // Maria - Matemática e Física
        turma2E1.getProfessores().add(prof3); // Ana - História e Geografia
        
        turma3E1.getProfessores().add(prof2); // João - Português e Literatura
        turma3E1.getProfessores().add(prof3); // Ana - História e Geografia
        
        turma4E1.getProfessores().add(prof1); // Maria - Matemática e Física
        turma4E1.getProfessores().add(prof2); // João - Português e Literatura
        turma4E1.getProfessores().add(prof3); // Ana - História e Geografia
        
        turma5E1.getProfessores().add(prof1); // Maria - Matemática e Física
        turma5E1.getProfessores().add(prof3); // Ana - História e Geografia
        
        // Professores da Escola 2
        turma1E2.getProfessores().add(prof4); // Carlos - Química e Biologia
        turma1E2.getProfessores().add(prof5); // Juliana - Inglês e Artes
        
        turma2E2.getProfessores().add(prof4); // Carlos - Química e Biologia
        turma2E2.getProfessores().add(prof5); // Juliana - Inglês e Artes
        
        turma3E2.getProfessores().add(prof4); // Carlos - Química e Biologia
        turma3E2.getProfessores().add(prof5); // Juliana - Inglês e Artes
        
        turma4E2.getProfessores().add(prof4); // Carlos - Química e Biologia
        turma4E2.getProfessores().add(prof5); // Juliana - Inglês e Artes
        
        turma5E2.getProfessores().add(prof4); // Carlos - Química e Biologia
        turma5E2.getProfessores().add(prof5); // Juliana - Inglês e Artes
        
        // Salvar turmas com professores
        turmaRepository.saveAll(turmasEscola1);
        turmaRepository.saveAll(turmasEscola2);
        
        System.out.println("✅ Professores associados às turmas");
        
        // ==================== CRIAR RESPONSÁVEIS ====================
        System.out.println("👨‍👩‍👧‍👦 Criando responsáveis...");
        
        Usuario resp1 = new Usuario();
        resp1.setNome("Mariana Souza Lima");
        resp1.setEmail("responsavel@educonnect.com");
        resp1.setPassword(passwordEncoder.encode("resp123"));
        resp1.setCpf("700.700.700-70");
        resp1.setTelefone("(11) 99100-1001");
        resp1.setRole(Role.RESPONSAVEL);
        resp1.setAtivo(true);
        resp1.setEscola(escola1);
        resp1 = usuarioRepository.save(resp1);
        
        Usuario resp2 = new Usuario();
        resp2.setNome("Ricardo Costa Almeida");
        resp2.setEmail("ricardo.responsavel@educonnect.com");
        resp2.setPassword(passwordEncoder.encode("resp123"));
        resp2.setCpf("800.800.800-80");
        resp2.setTelefone("(11) 99200-2002");
        resp2.setRole(Role.RESPONSAVEL);
        resp2.setAtivo(true);
        resp2.setEscola(escola1);
        resp2 = usuarioRepository.save(resp2);
        
        Usuario resp3 = new Usuario();
        resp3.setNome("Fernanda Oliveira Silva");
        resp3.setEmail("fernanda.responsavel@educonnect.com");
        resp3.setPassword(passwordEncoder.encode("resp123"));
        resp3.setCpf("900.900.900-90");
        resp3.setTelefone("(11) 99300-3003");
        resp3.setRole(Role.RESPONSAVEL);
        resp3.setAtivo(true);
        resp3.setEscola(escola2);
        resp3 = usuarioRepository.save(resp3);
        
        Usuario resp4 = new Usuario();
        resp4.setNome("Paulo Roberto Santos");
        resp4.setEmail("paulo.responsavel@educonnect.com");
        resp4.setPassword(passwordEncoder.encode("resp123"));
        resp4.setCpf("101.101.101-10");
        resp4.setTelefone("(11) 99400-4004");
        resp4.setRole(Role.RESPONSAVEL);
        resp4.setAtivo(true);
        resp4.setEscola(escola2);
        resp4 = usuarioRepository.save(resp4);
        
        System.out.println("✅ 4 responsáveis criados");
        
        // ==================== CRIAR ALUNOS ====================
        System.out.println("👨‍🎓 Criando alunos...");
        
        List<Usuario> alunos = new ArrayList<>();
        
        // Alunos da Escola 1
        Usuario aluno1 = criarAluno("Ana Carolina Souza", "aluno@educonnect.com", "201.201.201-20", "(11) 99101-0101", "2025001", resp1, escola1);
        alunos.add(aluno1);
        turma1E1.getAlunos().add(aluno1);
        
        Usuario aluno2 = criarAluno("Pedro Henrique Costa", "pedro.aluno@educonnect.com", "202.202.202-20", "(11) 99102-0102", "2025002", resp1, escola1);
        alunos.add(aluno2);
        turma1E1.getAlunos().add(aluno2);
        
        Usuario aluno3 = criarAluno("Lucas Gabriel Silva", "lucas.aluno@educonnect.com", "203.203.203-20", "(11) 99103-0103", "2025003", resp2, escola1);
        alunos.add(aluno3);
        turma2E1.getAlunos().add(aluno3);
        
        Usuario aluno4 = criarAluno("Mariana Beatriz Santos", "mariana.aluno@educonnect.com", "204.204.204-20", "(11) 99104-0104", "2025004", resp2, escola1);
        alunos.add(aluno4);
        turma3E1.getAlunos().add(aluno4);
        
        Usuario aluno5 = criarAluno("Rafael Eduardo Oliveira", "rafael.aluno@educonnect.com", "205.205.205-20", "(11) 99105-0105", "2025005", resp1, escola1);
        alunos.add(aluno5);
        turma4E1.getAlunos().add(aluno5);
        
        Usuario aluno6 = criarAluno("Sophia Vitória Lima", "sophia.aluno@educonnect.com", "206.206.206-20", "(11) 99106-0106", "2025006", resp2, escola1);
        alunos.add(aluno6);
        turma5E1.getAlunos().add(aluno6);
        
        // Alunos da Escola 2
        Usuario aluno7 = criarAluno("Gustavo Henrique Pereira", "gustavo.aluno@educonnect.com", "207.207.207-20", "(11) 99107-0107", "2025007", resp3, escola2);
        alunos.add(aluno7);
        turma1E2.getAlunos().add(aluno7);
        
        Usuario aluno8 = criarAluno("Isabella Cristina Alves", "isabella.aluno@educonnect.com", "208.208.208-20", "(11) 99108-0108", "2025008", resp3, escola2);
        alunos.add(aluno8);
        turma2E2.getAlunos().add(aluno8);
        
        Usuario aluno9 = criarAluno("Miguel Ângelo Rodrigues", "miguel.aluno@educonnect.com", "209.209.209-20", "(11) 99109-0109", "2025009", resp4, escola2);
        alunos.add(aluno9);
        turma3E2.getAlunos().add(aluno9);
        
        Usuario aluno10 = criarAluno("Laura Fernanda Costa", "laura.aluno@educonnect.com", "210.210.210-21", "(11) 99110-0110", "2025010", resp4, escola2);
        alunos.add(aluno10);
        turma4E2.getAlunos().add(aluno10);
        
        Usuario aluno11 = criarAluno("Davi Lucas Martins", "davi.aluno@educonnect.com", "211.211.211-21", "(11) 99111-0111", "2025011", resp3, escola2);
        alunos.add(aluno11);
        turma5E2.getAlunos().add(aluno11);
        
        Usuario aluno12 = criarAluno("Gabriela Sofia Oliveira", "gabriela.aluno@educonnect.com", "212.212.212-21", "(11) 99112-0112", "2025012", resp4, escola2);
        alunos.add(aluno12);
        turma3E2.getAlunos().add(aluno12);
        
        // Salvar turmas com alunos
        turmaRepository.saveAll(turmasEscola1);
        turmaRepository.saveAll(turmasEscola2);
        
        System.out.println("✅ 12 alunos criados e vinculados às turmas");
        
        // ==================== CRIAR RECADOS ====================
        System.out.println("✉️ Criando recados...");
        
        Recado recado1 = new Recado();
        recado1.setTitulo("Reunião de Pais e Mestres - 1º Bimestre");
        recado1.setConteudo("Informamos que a reunião de pais e mestres será realizada no próximo sábado, dia 15/02/2025, das 9h às 12h. É fundamental a presença de todos os responsáveis para discutirmos o desenvolvimento dos alunos.");
        recado1.setCategoria(CategoriaRecado.EVENTO);
        recado1.setImportante(true);
        recado1.setExigirConfirmacao(true);
        recado1.setRemetente(diretor1);
        recadoRepository.save(recado1);
        
        Recado recado2 = new Recado();
        recado2.setTitulo("Material para aula de Ciências");
        recado2.setConteudo("Os alunos do 5º Ano devem trazer para a próxima aula: sementes, algodão, copo plástico transparente e água. Faremos uma atividade sobre germinação.");
        recado2.setCategoria(CategoriaRecado.ACADEMICO);
        recado2.setImportante(false);
        recado2.setExigirConfirmacao(false);
        recado2.setRemetente(prof3);
        recadoRepository.save(recado2);
        
        Recado recado3 = new Recado();
        recado3.setTitulo("Feira Cultural - Inscrições Abertas");
        recado3.setConteudo("Estão abertas as inscrições para a Feira Cultural 2025! As turmas interessadas devem se inscrever até 20/02/2025. O evento acontecerá no dia 15/03/2025.");
        recado3.setCategoria(CategoriaRecado.EVENTO);
        recado3.setImportante(true);
        recado3.setExigirConfirmacao(false);
        recado3.setRemetente(diretor2);
        recadoRepository.save(recado3);
        
        Recado recado4 = new Recado();
        recado4.setTitulo("Aviso: Reposição de aulas");
        recado4.setConteudo("Informamos que haverá reposição de aulas no sábado dia 22/02/2025 para as turmas do período da manhã.");
        recado4.setCategoria(CategoriaRecado.GERAL);
        recado4.setImportante(false);
        recado4.setExigirConfirmacao(false);
        recado4.setRemetente(admin);
        recadoRepository.save(recado4);
        
        Recado recado5 = new Recado();
        recado5.setTitulo("Documentação Pendente");
        recado5.setConteudo("Alguns alunos ainda não entregaram a documentação completa para matrícula. Favor regularizar até o final do mês.");
        recado5.setCategoria(CategoriaRecado.GERAL);
        recado5.setImportante(true);
        recado5.setExigirConfirmacao(true);
        recado5.setRemetente(admin);
        recadoRepository.save(recado5);
        
        System.out.println("✅ 5 recados criados");
        
        // ==================== CRIAR ATIVIDADES ====================
        System.out.println("📝 Criando atividades...");
        
        Atividade ativ1 = new Atividade();
        ativ1.setTitulo("Trabalho de Matemática - Equações do 1º Grau");
        ativ1.setDescricao("Resolver a lista de exercícios do capítulo 3, páginas 45 a 50. Entregar em folha separada com todos os cálculos.");
        ativ1.setDisciplina("Matemática");
        ativ1.setTurma(turma1E1.getNome());
        ativ1.setProfessor(prof1);
        ativ1.setDataEntrega(LocalDateTime.now().plusDays(7));
        ativ1.setValor(10.0);
        ativ1.setPeso(2);
        ativ1.setTentativasPermitidas(1);
        atividadeRepository.save(ativ1);
        
        Atividade ativ2 = new Atividade();
        ativ2.setTitulo("Redação - Meu lugar preferido");
        ativ2.setDescricao("Escrever uma redação de no mínimo 20 linhas descrevendo seu lugar preferido. Atenção à gramática e pontuação.");
        ativ2.setDisciplina("Português");
        ativ2.setTurma(turma2E1.getNome());
        ativ2.setProfessor(prof2);
        ativ2.setDataEntrega(LocalDateTime.now().plusDays(5));
        ativ2.setValor(10.0);
        ativ2.setPeso(1);
        ativ2.setTentativasPermitidas(1);
        atividadeRepository.save(ativ2);
        
        Atividade ativ3 = new Atividade();
        ativ3.setTitulo("Pesquisa sobre Revolução Industrial");
        ativ3.setDescricao("Realizar pesquisa sobre as principais mudanças causadas pela Revolução Industrial. Mínimo 3 páginas.");
        ativ3.setDisciplina("História");
        ativ3.setTurma(turma3E1.getNome());
        ativ3.setProfessor(prof3);
        ativ3.setDataEntrega(LocalDateTime.now().plusDays(10));
        ativ3.setValor(15.0);
        ativ3.setPeso(2);
        ativ3.setTentativasPermitidas(1);
        atividadeRepository.save(ativ3);
        
        Atividade ativ4 = new Atividade();
        ativ4.setTitulo("Experimento de Química - Reações Químicas");
        ativ4.setDescricao("Realizar o experimento do roteiro e entregar relatório científico completo com introdução, materiais, métodos, resultados e conclusão.");
        ativ4.setDisciplina("Química");
        ativ4.setTurma(turma3E2.getNome());
        ativ4.setProfessor(prof4);
        ativ4.setDataEntrega(LocalDateTime.now().plusDays(14));
        ativ4.setValor(20.0);
        ativ4.setPeso(3);
        ativ4.setTentativasPermitidas(1);
        atividadeRepository.save(ativ4);
        
        Atividade ativ5 = new Atividade();
        ativ5.setTitulo("Reading comprehension - Short stories");
        ativ5.setDescricao("Read the text 'The Adventure' and answer the questions 1 to 10. Write a summary in English (minimum 10 lines).");
        ativ5.setDisciplina("Inglês");
        ativ5.setTurma(turma4E2.getNome());
        ativ5.setProfessor(prof5);
        ativ5.setDataEntrega(LocalDateTime.now().plusDays(6));
        ativ5.setValor(10.0);
        ativ5.setPeso(1);
        ativ5.setTentativasPermitidas(2);
        atividadeRepository.save(ativ5);
        
        Atividade ativ6 = new Atividade();
        ativ6.setTitulo("Exercícios de Física - Movimento Uniforme");
        ativ6.setDescricao("Resolver todos os exercícios das páginas 78 e 79. Mostrar todos os cálculos e unidades de medida.");
        ativ6.setDisciplina("Física");
        ativ6.setTurma(turma5E2.getNome());
        ativ6.setProfessor(prof1);
        ativ6.setDataEntrega(LocalDateTime.now().plusDays(4));
        ativ6.setValor(8.0);
        ativ6.setPeso(1);
        ativ6.setTentativasPermitidas(1);
        atividadeRepository.save(ativ6);
        
        System.out.println("✅ 6 atividades criadas");
        
        // ==================== CRIAR EVENTOS ====================
        System.out.println("📅 Criando eventos...");
        
        Evento evento1 = new Evento();
        evento1.setTitulo("Prova Bimestral de Matemática");
        evento1.setDescricao("Prova abrangendo equações, funções e geometria");
        evento1.setDataInicio(LocalDateTime.now().plusDays(12));
        evento1.setDataFim(LocalDateTime.now().plusDays(12).plusHours(2));
        evento1.setTipo("PROVA");
        evento1.setCriador(prof1);
        eventoRepository.save(evento1);
        
        Evento evento2 = new Evento();
        evento2.setTitulo("Feira de Ciências 2025");
        evento2.setDescricao("Apresentação de projetos científicos dos alunos");
        evento2.setDataInicio(LocalDateTime.now().plusDays(30));
        evento2.setDataFim(LocalDateTime.now().plusDays(30).plusHours(6));
        evento2.setTipo("EVENTO");
        evento2.setCriador(diretor1);
        eventoRepository.save(evento2);
        
        Evento evento3 = new Evento();
        evento3.setTitulo("Olimpíada de Matemática");
        evento3.setDescricao("Competição entre turmas - Fase escolar");
        evento3.setDataInicio(LocalDateTime.now().plusDays(20));
        evento3.setDataFim(LocalDateTime.now().plusDays(20).plusHours(3));
        evento3.setTipo("EVENTO");
        evento3.setCriador(prof1);
        eventoRepository.save(evento3);
        
        System.out.println("✅ 3 eventos criados");
        
        System.out.println("\n=================================================");
        System.out.println("✅ DADOS INICIAIS CARREGADOS COM SUCESSO!");
        System.out.println("=================================================\n");
        System.out.println("📊 Resumo:");
        System.out.println("   • 2 Escolas");
        System.out.println("   • 10 Turmas (5 por escola)");
        System.out.println("   • 1 Administrador");
        System.out.println("   • 2 Diretores");
        System.out.println("   • 5 Professores");
        System.out.println("   • 4 Responsáveis");
        System.out.println("   • 12 Alunos");
        System.out.println("   • 5 Recados");
        System.out.println("   • 6 Atividades");
        System.out.println("   • 3 Eventos\n");
        System.out.println("📧 Credenciais de teste:");
        System.out.println("   Admin: admin@educonnect.com / admin123");
        System.out.println("   Diretor 1: diretoria@educonnect.com / dir123");
        System.out.println("   Diretor 2: roberto.diretor@educonnect.com / dir123");
        System.out.println("   Professor: professor@educonnect.com / prof123");
        System.out.println("   Aluno: aluno@educonnect.com / aluno123");
        System.out.println("   Responsável: responsavel@educonnect.com / resp123");
        System.out.println("=================================================\n");
    }
    
    private Turma criarTurma(String nome, String anoLetivo, String serie, String turno, Escola escola) {
        Turma turma = new Turma();
        turma.setNome(nome);
        turma.setAnoLetivo(anoLetivo);
        turma.setSerie(serie);
        turma.setTurno(turno);
        turma.setEscola(escola);
        turma.setAtiva(true);
        turma.setDescricao("Turma " + nome + " do ano letivo " + anoLetivo);
        return turmaRepository.save(turma);
    }
    
    private Usuario criarAluno(String nome, String email, String cpf, String telefone, String matricula, Usuario responsavel, Escola escola) {
        Usuario aluno = new Usuario();
        aluno.setNome(nome);
        aluno.setEmail(email);
        aluno.setPassword(passwordEncoder.encode("aluno123"));
        aluno.setCpf(cpf);
        aluno.setTelefone(telefone);
        aluno.setRole(Role.ALUNO);
        aluno.setAtivo(true);
        aluno.setMatricula(matricula);
        aluno.setResponsavel(responsavel);
        aluno.setEscola(escola);
        return usuarioRepository.save(aluno);
    }
}

