-- Script para inserir usuário DIRETORIA
-- Senha: dir123 (será criptografada pelo BCrypt no backend)
-- Este script insere o usuário caso não exista

USE educonnect;

-- Inserir usuário DIRETORIA
-- Nota: A senha 'dir123' criptografada com BCrypt é:
-- $2a$10$YourHashHere (deve ser gerado pelo backend)

-- Como o BCrypt precisa ser gerado pelo Spring Security,
-- vamos inserir através do DataInitializer que já foi atualizado.
-- Este arquivo serve apenas como documentação.

-- Para forçar a recriação dos dados, você pode:
-- 1. Parar os containers: docker-compose down
-- 2. Remover o volume do MySQL: docker volume rm educonnect_mysql_data
-- 3. Subir novamente: docker-compose up -d

-- Ou inserir manualmente com senha já criptografada:
/*
INSERT INTO usuarios (nome, email, password, cpf, telefone, role, ativo, data_cadastro)
SELECT 'Patricia Direção', 'diretoria@educonnect.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '888.888.888-88', '(11) 98999-9999', 'DIRETORIA', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'diretoria@educonnect.com'
);
*/

SELECT 'Para criar o usuário DIRETORIA, reinicie o backend ou use o DataInitializer' AS message;

