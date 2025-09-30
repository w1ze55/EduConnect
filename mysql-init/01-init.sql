-- Script de inicialização do MySQL para EduConnect
-- Este script é executado apenas na primeira vez que o container é criado

-- Garantir que o banco existe
CREATE DATABASE IF NOT EXISTS educonnect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar o banco
USE educonnect;

-- Garantir privilégios ao usuário
GRANT ALL PRIVILEGES ON educonnect.* TO 'educonnect'@'%';
FLUSH PRIVILEGES;

-- Log de inicialização
SELECT 'Database educonnect initialized successfully!' AS message;

