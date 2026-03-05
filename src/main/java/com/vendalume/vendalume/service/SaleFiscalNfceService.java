package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.SaleItem;
import com.vendalume.vendalume.repository.SaleItemRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para emissão de NFC-e (cupom fiscal) via Fiscal Simplify.
 * Emite a nota fiscal eletrônica e retorna o PDF oficial da SEFAZ.
 *
 * @author VendaLume
 */
@Service
@RequiredArgsConstructor
public class SaleFiscalNfceService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final FiscalSimplifyService fiscalSimplifyService;

    @Transactional(readOnly = true)
    public byte[] emitirNfceEPdf(UUID saleId) {
        Sale sale;
        if (SecurityUtils.isCurrentUserRoot()) {
            sale = saleRepository.findById(saleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", saleId));
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            sale = saleRepository.findByIdAndTenantId(saleId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", saleId));
        }

        List<SaleItem> items = saleItemRepository.findBySaleIdOrderByItemOrderAsc(sale.getId());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Venda sem itens para emitir NFC-e.");
        }

        return fiscalSimplifyService.emitirNfceEPdf(sale, items);
    }
}
