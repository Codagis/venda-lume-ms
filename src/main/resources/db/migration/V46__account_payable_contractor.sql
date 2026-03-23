-- Vincular conta a pagar a prestador PJ e à nota fiscal (conformidade legal)
ALTER TABLE account_payable ADD COLUMN contractor_id UUID;
ALTER TABLE account_payable ADD COLUMN contractor_invoice_id UUID;

ALTER TABLE account_payable
    ADD CONSTRAINT fk_ap_contractor FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE SET NULL;
ALTER TABLE account_payable
    ADD CONSTRAINT fk_ap_contractor_invoice FOREIGN KEY (contractor_invoice_id) REFERENCES contractor_invoices(id) ON DELETE SET NULL;

CREATE INDEX idx_ap_contractor ON account_payable(contractor_id);

COMMENT ON COLUMN account_payable.contractor_id IS 'Prestador PJ quando a conta é pagamento de serviço';
COMMENT ON COLUMN account_payable.contractor_invoice_id IS 'Nota fiscal do prestador vinculada ao pagamento';
