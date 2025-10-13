-- Script para criar tabela de escolas
-- Este script será executado automaticamente na inicialização do MySQL

USE educonnect;

-- Criar tabela de escolas
CREATE TABLE IF NOT EXISTS escolas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    telefone VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diretor_id BIGINT,
    CONSTRAINT fk_escola_diretor FOREIGN KEY (diretor_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Adicionar coluna escola_id na tabela usuarios se não existir
-- Verificar e adicionar coluna escola_id
SET @dbname = DATABASE();
SET @tablename = 'usuarios';
SET @columnname = 'escola_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Adicionar constraint se não existir
SET @constraintname = 'fk_usuario_escola';
SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (constraint_name = @constraintname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD CONSTRAINT ', @constraintname, ' FOREIGN KEY (escola_id) REFERENCES escolas(id) ON DELETE SET NULL')
));
PREPARE alterConstraintIfNotExists FROM @preparedStatement2;
EXECUTE alterConstraintIfNotExists;
DEALLOCATE PREPARE alterConstraintIfNotExists;

-- Log de sucesso
SELECT 'Tabela escolas criada com sucesso!' AS message;
