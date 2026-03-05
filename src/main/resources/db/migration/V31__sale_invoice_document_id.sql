-- ID interno do documento na Nuvem Fiscal (necessário para buscar o PDF da NF-e)
-- O endpoint GET /nfe/{id}/pdf exige o id retornado na emissão, não a chave de 44 dígitos
ALTER TABLE sales ADD COLUMN IF NOT EXISTS invoice_document_id VARCHAR(100);

COMMENT ON COLUMN sales.invoice_document_id IS 'ID interno do documento na Nuvem Fiscal para download do PDF da NF-e';
