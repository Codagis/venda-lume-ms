package com.vendalume.vendalume.service;

import com.vendalume.vendalume.domain.entity.CardMachine;
import com.vendalume.vendalume.domain.entity.Customer;
import com.vendalume.vendalume.domain.entity.Product;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.SaleItem;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.integration.fiscalsimplify.FiscalSimplifyClient;
import com.vendalume.vendalume.repository.CardMachineRepository;
import com.vendalume.vendalume.repository.CustomerRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço que integra Venda Lume com Fiscal Simplify.
 * Mapeia Tenant para empresa fiscal e Sale para NFC-e.
 *
 * @author VendaLume
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiscalSimplifyService {

    /** NCM válido na tabela SEFAZ - 22021000 = refrigerantes (padrão quando produto sem NCM) */
    private static final String NCM_PADRAO = "22021000";
    private static final String CFOP_PADRAO = "5102";
    private static final int CRT_PADRAO = 3;
    private static final int SERIE_PADRAO = 1;

    private final FiscalSimplifyClient fiscalSimplifyClient;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final CardMachineRepository cardMachineRepository;

    @Value("${vendalume.fiscal-simplify.enabled:true}")
    private boolean enabled;

    /**
     * Cadastra ou atualiza empresa no Fiscal Simplify a partir do Tenant.
     * Opcionalmente configura NFC-e (CSC) e cadastra certificado PFX.
     */
    public void syncTenantToFiscalSimplify(Tenant tenant, String certificadoPfxBase64, String certificadoPassword) {
        if (!enabled) return;
        if (tenant == null || tenant.getDocument() == null || tenant.getDocument().isBlank()) return;

        String cnpj = tenant.getDocument().replaceAll("\\D", "");
        if (cnpj.length() != 14) return;

        boolean ieOk = tenant.getStateRegistration() != null && !tenant.getStateRegistration().isBlank();
        boolean imOk = tenant.getMunicipalRegistration() != null && !tenant.getMunicipalRegistration().isBlank();
        if (!ieOk && !imOk) return;

        String uf = tenant.getAddressState();
        String codigoMunicipio = tenant.getCodigoMunicipio();
        if (uf == null || uf.isBlank() || codigoMunicipio == null || codigoMunicipio.isBlank()) {
            log.warn("Tenant {} sem UF ou codigoMunicipio - não é possível cadastrar no Fiscal Simplify", tenant.getId());
            return;
        }

        int crt = tenant.getCrt() != null && tenant.getCrt() >= 1 && tenant.getCrt() <= 4 ? tenant.getCrt() : CRT_PADRAO;
        int idCsc = tenant.getIdCsc() != null ? tenant.getIdCsc() : 0;
        String csc = tenant.getCsc() != null && !tenant.getCsc().isBlank() ? tenant.getCsc() : null;
        String ambiente = tenant.getAmbienteFiscal() != null && tenant.getAmbienteFiscal().equalsIgnoreCase("producao")
                ? "producao" : "homologacao";

        Map<String, Object> companyReq = new LinkedHashMap<>();
        companyReq.put("cnpj", cnpj);
        companyReq.put("razaoSocial", tenant.getName() != null ? tenant.getName() : "Empresa");
        companyReq.put("nomeFantasia", tenant.getTradeName() != null ? tenant.getTradeName() : tenant.getName());
        companyReq.put("inscricaoEstadual", tenant.getStateRegistration() != null && !tenant.getStateRegistration().isBlank()
                ? tenant.getStateRegistration() : "ISENTO");
        companyReq.put("uf", uf.toUpperCase());
        companyReq.put("codigoMunicipio", codigoMunicipio.trim());
        companyReq.put("nomeMunicipio", tenant.getAddressCity() != null && !tenant.getAddressCity().isBlank()
                ? tenant.getAddressCity().trim() : null);
        companyReq.put("crt", crt);

        if (fiscalSimplifyClient.companyExistsByCnpj(cnpj)) {
            Map<String, Object> updateReq = new LinkedHashMap<>();
            updateReq.put("uf", uf.toUpperCase());
            updateReq.put("codigoMunicipio", codigoMunicipio.trim());
            updateReq.put("nomeMunicipio", tenant.getAddressCity() != null && !tenant.getAddressCity().isBlank()
                    ? tenant.getAddressCity().trim() : null);
            updateReq.put("crt", crt);
            fiscalSimplifyClient.updateCompany(cnpj, updateReq);
            log.info("Empresa atualizada no Fiscal Simplify: CNPJ {}", cnpj);
        } else {
            fiscalSimplifyClient.createCompany(companyReq);
            log.info("Empresa cadastrada no Fiscal Simplify: CNPJ {}", cnpj);
        }

        if (csc != null && !csc.isBlank()) {
            Map<String, Object> nfcConfig = new LinkedHashMap<>();
            nfcConfig.put("crt", crt);
            nfcConfig.put("idCsc", idCsc);
            nfcConfig.put("csc", csc);
            nfcConfig.put("ambiente", ambiente);
            fiscalSimplifyClient.configurarNfce(cnpj, nfcConfig);
        }

        int crtNfe = tenant.getCrtNfe() != null && tenant.getCrtNfe() >= 1 && tenant.getCrtNfe() <= 4
                ? tenant.getCrtNfe() : crt;
        String ambienteNfe = tenant.getAmbienteNfe() != null && !tenant.getAmbienteNfe().isBlank()
                ? (tenant.getAmbienteNfe().equalsIgnoreCase("producao") ? "producao" : "homologacao")
                : ambiente;
        Map<String, Object> nfeConfig = new LinkedHashMap<>();
        nfeConfig.put("crt", crtNfe);
        nfeConfig.put("ambiente", ambienteNfe);
        fiscalSimplifyClient.configurarNfe(cnpj, nfeConfig);

        if (certificadoPfxBase64 != null && !certificadoPfxBase64.isBlank() && certificadoPassword != null && !certificadoPassword.isBlank()) {
            String base64Limpo = certificadoPfxBase64.replaceAll("^data:[^;]+;base64,", "").trim();
            fiscalSimplifyClient.cadastrarCertificado(cnpj, base64Limpo, certificadoPassword);
        }
    }

    /**
     * Emite NFC-e para a venda e retorna o PDF do cupom fiscal.
     */
    public byte[] emitirNfceEPdf(Sale sale, List<SaleItem> items) {
        if (!enabled) {
            throw new IllegalStateException("Integração Fiscal Simplify está desabilitada.");
        }

        Tenant tenant = tenantRepository.findById(sale.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));

        String cnpj = tenant.getDocument();
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("Empresa sem CNPJ cadastrado para emissão fiscal.");
        }
        cnpj = cnpj.replaceAll("\\D", "");
        if (cnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ da empresa inválido.");
        }

        String codigoMunicipio = tenant.getCodigoMunicipio();
        if (codigoMunicipio == null || codigoMunicipio.isBlank()) {
            throw new IllegalArgumentException("Empresa sem código do município (IBGE). Configure em Configurações > Empresas.");
        }

        Map<String, Object> nfceReq = new LinkedHashMap<>();
        nfceReq.put("cnpjEmitente", cnpj);
        nfceReq.put("ieEmitente", tenant.getStateRegistration() != null && !tenant.getStateRegistration().isBlank()
                ? tenant.getStateRegistration() : "ISENTO");
        nfceReq.put("serie", tenant.getEcfSeries() != null && !tenant.getEcfSeries().isBlank()
                ? parseSerie(tenant.getEcfSeries()) : SERIE_PADRAO);
        nfceReq.put("naturezaOperacao", "VENDA");

        // Itens com dados do produto: código (SKU), descrição, NCM, quantidade e valor unitário.
        List<Map<String, Object>> itens = new ArrayList<>();
        for (SaleItem item : items) {
            Product p = item.getProduct();
            String descricao = (item.getProductName() != null && !item.getProductName().isBlank())
                    ? item.getProductName()
                    : (p != null && p.getName() != null ? p.getName() : "Produto");
            descricao = descricao.length() > 120 ? descricao.substring(0, 120) : descricao;
            String codigo = (item.getProductSku() != null && !item.getProductSku().isBlank())
                    ? item.getProductSku()
                    : (p != null && p.getSku() != null && !p.getSku().isBlank() ? p.getSku() : null);
            String ncm = (p != null && p.getNcm() != null && p.getNcm().length() == 8) ? p.getNcm() : NCM_PADRAO;
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
            BigDecimal valorUnitario = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) > 0 && valorUnitario.compareTo(BigDecimal.ZERO) == 0) {
                valorUnitario = item.getTotal().divide(qty, 4, java.math.RoundingMode.HALF_UP);
            }

            Map<String, Object> itemMap = new LinkedHashMap<>();
            if (codigo != null) itemMap.put("codigo", codigo.length() > 60 ? codigo.substring(0, 60) : codigo);
            itemMap.put("descricao", descricao);
            itemMap.put("ncm", ncm);
            itemMap.put("cfop", CFOP_PADRAO);
            itemMap.put("quantidade", qty);
            itemMap.put("valorUnitario", valorUnitario);
            itens.add(itemMap);
        }
        nfceReq.put("itens", itens);

        List<Map<String, Object>> pagamentos = new ArrayList<>();
        BigDecimal total = sale.getTotal() != null ? sale.getTotal() : BigDecimal.ZERO;
        Map<String, Object> pag = new LinkedHashMap<>();

        // PIX e demais formas que não são cartão: apenas forma + valor (nunca exige dados de cartão)
        if (sale.getPaymentMethod() == PaymentMethod.PIX || sale.getPaymentMethod() == PaymentMethod.CASH
                || sale.getPaymentMethod() == PaymentMethod.CHECK || sale.getPaymentMethod() == PaymentMethod.BANK_TRANSFER
                || sale.getPaymentMethod() == PaymentMethod.MEAL_VOUCHER || sale.getPaymentMethod() == PaymentMethod.FOOD_VOUCHER
                || sale.getPaymentMethod() == PaymentMethod.CREDIT || sale.getPaymentMethod() == PaymentMethod.OTHER
                || sale.getPaymentMethod() == null) {
            // PIX sempre como "17" (tPag SEFAZ) — sem nenhum dado de cartão
            String formaNfce = (sale.getPaymentMethod() == PaymentMethod.PIX) ? "17" : mapPaymentMethodToFiscalNfce(sale.getPaymentMethod());
            pag.put("forma", formaNfce);
            pag.put("valor", total);
            pagamentos.add(pag);
        } else {
            // Cartão (crédito/débito): só envia bloco de cartão (03/04) se tiver todos os dados obrigatórios
            boolean isCard = sale.getPaymentMethod() == PaymentMethod.CREDIT_CARD || sale.getPaymentMethod() == PaymentMethod.DEBIT_CARD;
            boolean hasCardData = isCard && sale.getCardBrand() != null && !sale.getCardBrand().isBlank()
                    && sale.getCardAuthorization() != null && !sale.getCardAuthorization().isBlank();
            if (hasCardData && sale.getCardMachineId() != null) {
                cardMachineRepository.findById(sale.getCardMachineId()).map(CardMachine::getAcquirerCnpj).ifPresent(acquirerCnpj -> {
                    if (acquirerCnpj != null && !acquirerCnpj.isBlank()) {
                        String cnpjLimpo = acquirerCnpj.replaceAll("\\D", "");
                        if (cnpjLimpo.length() == 14) pag.put("cnpjAdquirente", cnpjLimpo);
                    }
                });
            }
            boolean useCardBlock = hasCardData && pag.containsKey("cnpjAdquirente");
            String formaPag;
            if (useCardBlock) {
                formaPag = sale.getPaymentMethod() == PaymentMethod.CREDIT_CARD ? "03" : "04";
                int tpIntegracao = (sale.getCardIntegrationType() != null && (sale.getCardIntegrationType() == 1 || sale.getCardIntegrationType() == 2))
                        ? sale.getCardIntegrationType() : 2;
                pag.put("tpIntegracao", tpIntegracao);
                pag.put("tBand", sale.getCardBrand().trim());
                pag.put("cAut", sale.getCardAuthorization().trim().length() > 20 ? sale.getCardAuthorization().trim().substring(0, 20) : sale.getCardAuthorization().trim());
            } else {
                formaPag = mapPaymentMethodToFiscalNfce(sale.getPaymentMethod());
            }
            pag.put("forma", formaPag);
            pag.put("valor", total);
            pagamentos.add(pag);
        }
        nfceReq.put("pagamentos", pagamentos);

        if (sale.getCustomerDocument() != null && !sale.getCustomerDocument().isBlank()
                && sale.getCustomerName() != null && !sale.getCustomerName().isBlank()) {
            String doc = sale.getCustomerDocument().replaceAll("\\D", "");
            if (doc.length() == 11 || doc.length() == 14) {
                Map<String, Object> dest = new LinkedHashMap<>();
                dest.put("nome", sale.getCustomerName().trim().length() > 60 ? sale.getCustomerName().trim().substring(0, 60) : sale.getCustomerName().trim());
                dest.put("cpf", doc.length() == 11 ? doc : null);
                dest.put("cnpj", doc.length() == 14 ? doc : null);
                nfceReq.put("destinatario", dest);
            }
        }

        Map<String, Object> result = fiscalSimplifyClient.emitirNfce(nfceReq);
        Object idObj = result != null ? result.get("id") : null;
        if (idObj == null) {
            throw new IllegalStateException("Fiscal Simplify não retornou o ID da NFC-e. Verifique os logs.");
        }
        String nfceId = idObj.toString();
        return fiscalSimplifyClient.getNfcePdf(nfceId);
    }

    /**
     * Monta o payload e emite NF-e para a venda. Retorna o mapa de resposta (id, chave, numero) para o caller persistir na Sale.
     */
    public Map<String, Object> emitirNfe(Sale sale, List<SaleItem> items) {
        if (!enabled) {
            throw new IllegalStateException("Integração Fiscal Simplify está desabilitada.");
        }

        Tenant tenant = tenantRepository.findById(sale.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));

        String cnpj = tenant.getDocument();
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("Empresa sem CNPJ cadastrado para emissão fiscal.");
        }
        cnpj = cnpj.replaceAll("\\D", "");
        if (cnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ da empresa inválido.");
        }

        String codigoMunicipio = tenant.getCodigoMunicipio();
        if (codigoMunicipio == null || codigoMunicipio.isBlank()) {
            throw new IllegalArgumentException("Empresa sem código do município (IBGE). Configure em Configurações > Empresas.");
        }

        Map<String, Object> nfeReq = new LinkedHashMap<>();
        nfeReq.put("cnpjEmitente", cnpj);
        nfeReq.put("ieEmitente", tenant.getStateRegistration() != null && !tenant.getStateRegistration().isBlank()
                ? tenant.getStateRegistration() : "ISENTO");
        nfeReq.put("serie", tenant.getEcfSeries() != null && !tenant.getEcfSeries().isBlank()
                ? parseSerie(tenant.getEcfSeries()) : SERIE_PADRAO);
        nfeReq.put("naturezaOperacao", "VENDA");

        // Destinatário obrigatório em NF-e: nome + CPF/CNPJ + endereço (da venda ou do cadastro do cliente)
        String docCliente = resolveCustomerDocumentForNfe(sale);
        String nomeDest = (sale.getCustomerName() != null && !sale.getCustomerName().isBlank())
                ? sale.getCustomerName().trim() : resolveCustomerNameForNfe(sale);
        if (nomeDest == null || nomeDest.isBlank()) nomeDest = "Consumidor final";
        Map<String, Object> dest = new LinkedHashMap<>();
        dest.put("nome", nomeDest.length() > 60 ? nomeDest.substring(0, 60) : nomeDest);
        if (docCliente != null && !docCliente.isBlank()) {
            String doc = docCliente.replaceAll("\\D", "");
            if (doc.length() == 11) dest.put("cpf", doc);
            else if (doc.length() == 14) dest.put("cnpj", doc);
        }
        String logradouro = sale.getDeliveryAddress() != null && !sale.getDeliveryAddress().isBlank()
                ? sale.getDeliveryAddress().trim() : "Não informado";
        dest.put("logradouro", logradouro.length() > 60 ? logradouro.substring(0, 60) : logradouro);
        dest.put("numero", "S/N");
        String bairro = sale.getDeliveryNeighborhood() != null && !sale.getDeliveryNeighborhood().isBlank()
                ? sale.getDeliveryNeighborhood().trim() : "Centro";
        dest.put("bairro", bairro.length() > 60 ? bairro.substring(0, 60) : bairro);
        String municipio = sale.getDeliveryCity() != null && !sale.getDeliveryCity().isBlank()
                ? sale.getDeliveryCity().trim() : tenant.getAddressCity();
        dest.put("municipio", (municipio != null && !municipio.isBlank()) ? (municipio.length() > 60 ? municipio.substring(0, 60) : municipio) : "Não informado");
        String uf = sale.getDeliveryState() != null && !sale.getDeliveryState().isBlank()
                ? sale.getDeliveryState().trim().toUpperCase().substring(0, 2) : tenant.getAddressState();
        dest.put("uf", (uf != null && uf.length() == 2) ? uf : "CE");
        dest.put("codigoMunicipio", codigoMunicipio);
        if (sale.getDeliveryZipCode() != null && !sale.getDeliveryZipCode().isBlank()) {
            String cep = sale.getDeliveryZipCode().replaceAll("\\D", "");
            if (cep.length() == 8) dest.put("cep", cep);
        }
        nfeReq.put("destinatario", dest);

        List<Map<String, Object>> itens = new ArrayList<>();
        for (SaleItem item : items) {
            Product p = item.getProduct();
            String descricao = (item.getProductName() != null && !item.getProductName().isBlank())
                    ? item.getProductName()
                    : (p != null && p.getName() != null ? p.getName() : "Produto");
            descricao = descricao.length() > 120 ? descricao.substring(0, 120) : descricao;
            String codigo = (item.getProductSku() != null && !item.getProductSku().isBlank())
                    ? item.getProductSku()
                    : (p != null && p.getSku() != null && !p.getSku().isBlank() ? p.getSku() : null);
            String ncm = (p != null && p.getNcm() != null && p.getNcm().length() == 8) ? p.getNcm() : NCM_PADRAO;
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
            BigDecimal valorUnitario = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) > 0 && valorUnitario.compareTo(BigDecimal.ZERO) == 0) {
                valorUnitario = item.getTotal().divide(qty, 4, java.math.RoundingMode.HALF_UP);
            }
            Map<String, Object> itemMap = new LinkedHashMap<>();
            if (codigo != null) itemMap.put("codigo", codigo.length() > 60 ? codigo.substring(0, 60) : codigo);
            itemMap.put("descricao", descricao);
            itemMap.put("ncm", ncm);
            itemMap.put("cfop", CFOP_PADRAO);
            itemMap.put("quantidade", qty);
            itemMap.put("valorUnitario", valorUnitario);
            itens.add(itemMap);
        }
        nfeReq.put("itens", itens);

        return fiscalSimplifyClient.emitirNfe(nfeReq);
    }

    /** Documento do cliente: da venda, do cadastro (customerId) ou único cliente com mesmo nome no tenant. */
    private String resolveCustomerDocumentForNfe(Sale sale) {
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

    private String resolveCustomerNameForNfe(Sale sale) {
        if (sale == null || sale.getCustomerId() == null) return null;
        return customerRepository.findByIdAndTenantId(sale.getCustomerId(), sale.getTenantId())
                .map(Customer::getName)
                .filter(n -> n != null && !n.isBlank())
                .orElse(null);
    }

    private int parseSerie(String ecfSeries) {
        if (ecfSeries == null || ecfSeries.isBlank()) return SERIE_PADRAO;
        try {
            String num = ecfSeries.replaceAll("\\D", "");
            if (num.isEmpty()) return SERIE_PADRAO;
            int v = Integer.parseInt(num);
            return (v >= 1 && v <= 999) ? v : SERIE_PADRAO;
        } catch (Exception e) {
            return SERIE_PADRAO;
        }
    }

    /**
     * Forma de pagamento para NFC-e. Cartão crédito/débito é enviado como "90 - Outros"
     * para não exigir dados do cartão na SEFAZ (VendaLume não envia e não armazena dados de cartão).
     */
    private String mapPaymentMethodToFiscalNfce(PaymentMethod pm) {
        if (pm == null) return "99";
        return switch (pm) {
            case CASH -> "01";
            case CHECK -> "02";
            case CREDIT_CARD, DEBIT_CARD -> "90";
            case PIX -> "17";
            case BANK_TRANSFER -> "18";
            case MEAL_VOUCHER -> "11";
            case FOOD_VOUCHER -> "10";
            default -> "99";
        };
    }

    private String mapPaymentMethodToFiscal(PaymentMethod pm) {
        if (pm == null) return "99"; // Outros
        return switch (pm) {
            case CASH -> "01";
            case CHECK -> "02";
            case CREDIT_CARD -> "03";
            case DEBIT_CARD -> "04";
            case PIX -> "17";
            case BANK_TRANSFER -> "18";
            case MEAL_VOUCHER -> "11";
            case FOOD_VOUCHER -> "10";
            default -> "99";
        };
    }
}
