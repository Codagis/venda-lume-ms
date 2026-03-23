-- Número de dependentes para dedução no cálculo do IRRF (Lei brasileira)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS dependentes INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN employees.dependentes IS 'Número de dependentes para dedução na base de cálculo do IRRF (R$ 189,59/dependente)';
