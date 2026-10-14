-- O recebimento continua sem edicao. So o CA do lote e a data da consulta dele mudam,
-- para a peca na prateleira passar a sair com o certificado impresso.
-- A ficha ja assinada guarda o numero daquele dia em fornecimento_item_ca.

DROP TRIGGER IF EXISTS lote_epi_no_update;

CREATE TRIGGER lote_epi_no_update
BEFORE UPDATE OF
    id,
    unit_id,
    epi_id,
    lot_code,
    manufacturer,
    size_label,
    piece_valid_until,
    unit_cost_cents,
    created_at
ON lote_epi
BEGIN
    SELECT RAISE(ABORT, 'lote_epi nao se edita');
END;
