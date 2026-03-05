-- Observações da comanda (ex.: pedidos especiais, solicitações do cliente)
ALTER TABLE table_order ADD COLUMN notes VARCHAR(500);
COMMENT ON COLUMN table_order.notes IS 'Observações da comanda (para cozinha, atendimento)';
