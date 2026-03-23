-- Campos completos de folha/custos do funcionário para recibo profissional
ALTER TABLE employees ADD COLUMN cbo VARCHAR(20);
ALTER TABLE employees ADD COLUMN hazardous_pay_percent NUMERIC(5, 2);
ALTER TABLE employees ADD COLUMN overtime_hours NUMERIC(8, 2);
ALTER TABLE employees ADD COLUMN overtime_value NUMERIC(19, 4);
ALTER TABLE employees ADD COLUMN dsr_value NUMERIC(19, 4);
ALTER TABLE employees ADD COLUMN health_plan_deduction NUMERIC(19, 4);
ALTER TABLE employees ADD COLUMN inss_percent NUMERIC(5, 2);
ALTER TABLE employees ADD COLUMN irrf_value NUMERIC(19, 4);

COMMENT ON COLUMN employees.cbo IS 'CBO - Classificação Brasileira de Ocupações';
COMMENT ON COLUMN employees.hazardous_pay_percent IS 'Adicional de periculosidade (%)';
COMMENT ON COLUMN employees.overtime_hours IS 'Horas extraordinárias (50%)';
COMMENT ON COLUMN employees.overtime_value IS 'Valor das horas extras';
COMMENT ON COLUMN employees.dsr_value IS 'Descanso semanal remunerado';
COMMENT ON COLUMN employees.health_plan_deduction IS 'Desconto plano de saúde';
COMMENT ON COLUMN employees.inss_percent IS 'Alíquota INSS (%) para exibição';
COMMENT ON COLUMN employees.irrf_value IS 'IRRF valor descontado';
