package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Customer;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.SaleItem;
import com.vendalume.vendalume.integration.fiscalsimplify.FiscalSimplifyClient;
import com.vendalume.vendalume.repository.CustomerRepository;
import com.vendalume.vendalume.repository.SaleItemRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço para emissão de NF-e (Nota Fiscal Eletrônica) via Fiscal Simplify.
 * Emite a nota, persiste chave/número na venda e retorna o PDF (DANFE).
 */
@Service
@RequiredArgsConstructor
public class SaleFiscalNfeService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleService saleService;
    private final FiscalSimplifyService fiscalSimplifyService;
    private final FiscalSimplifyClient fiscalSimplifyClient;
    private final CustomerRepository customerRepository;

    @Transactional
    public byte[] emitirNfeEPdf(UUID saleId) {
        var saleResponse = saleService.getById(saleId);
        if (!Boolean.TRUE.equals(saleResponse.getCanEmitNfe())) {
            throw new IllegalArgumentException("Nenhum produto desta venda está configurado para NF-e ou empresa sem configuração fiscal.");
        }
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
            throw new IllegalArgumentException("Venda sem itens para emitir NF-e.");
        }

        String docCliente = resolveCustomerDocument(sale);
        String doc = docCliente != null ? docCliente.replaceAll("\\D", "") : "";
        if (doc.length() != 11 && doc.length() != 14) {
            throw new IllegalArgumentException("A NF-e exige um destinatário com CPF ou CNPJ. Informe o documento do cliente na venda ou vincule a um cliente cadastrado com CPF/CNPJ.");
        }

        if (sale.getInvoiceKey() != null && !sale.getInvoiceKey().isBlank()) {
            // O PDF da Nuvem Fiscal exige o id interno do documento, não a chave (44 dígitos)
            String docId = sale.getInvoiceDocumentId();
            if (docId == null || docId.isBlank()) {
                throw new IllegalStateException(
                        "PDF indisponível para esta NF-e. Notas emitidas antes da atualização do sistema não têm o identificador necessário. A chave da nota é: " + sale.getInvoiceKey());
            }
            return fiscalSimplifyClient.getNfePdf(docId);
        }

        Map<String, Object> result = fiscalSimplifyService.emitirNfe(sale, items);
        Object idObj = result != null ? result.get("id") : null;
        if (idObj == null) {
            throw new IllegalStateException("Fiscal Simplify não retornou o ID da NF-e. Verifique os logs.");
        }
        String nfeId = idObj.toString();

        String chave = result != null && result.containsKey("chave") && result.get("chave") != null
                ? result.get("chave").toString() : nfeId;
        String numero = result != null && result.containsKey("numero") && result.get("numero") != null
                ? result.get("numero").toString() : null;
        if (numero == null && result != null && result.containsKey("nfe_numero")) {
            numero = result.get("nfe_numero").toString();
        }

        sale.setInvoiceKey(chave);
        sale.setInvoiceNumber(numero);
        sale.setInvoiceDocumentId(nfeId);
        saleRepository.save(sale);

        return fiscalSimplifyClient.getNfePdf(nfeId);
    }

    private String resolveCustomerDocument(Sale sale) {
        if (sale == null) return null;
        if (sale.getCustomerDocument() != null && !sale.getCustomerDocument().isBlank()) return sale.getCustomerDocument();
        if (sale.getCustomerId() != null) {
            String fromCustomer = customerRepository.findByIdAndTenantId(sale.getCustomerId(), sale.getTenantId())
                    .map(Customer::getDocument)
                    .filter(d -> d != null && !d.isBlank())
                    .orElse(null);
            if (fromCustomer != null) return fromCustomer;
        }
        if (sale.getCustomerName() != null && !sale.getCustomerName().isBlank() && sale.getTenantId() != null) {
            var byName = customerRepository.findByTenantIdAndNameIgnoreCaseAndDocumentNotNull(sale.getTenantId(), sale.getCustomerName().trim());
            if (byName.size() == 1) return byName.get(0).getDocument();
        }
        return null;
    }
}
