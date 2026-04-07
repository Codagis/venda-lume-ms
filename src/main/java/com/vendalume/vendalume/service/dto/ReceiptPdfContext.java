package com.vendalume.vendalume.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Objeto de transferência (DTO) interno ao serviço: ReceiptPdfContext.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Data
@Builder
public class ReceiptPdfContext {

    // Dados da empresa (Tenant)
    private String razaoSocialEmpresa;
    private String nomeFantasiaEmpresa;
    private String enderecoCompletoEmpresa;
    private String cnpjEmpresa;
    private String inscricaoEstadualEmpresa;
    private String inscricaoMunicipalEmpresa;

    // Dados do cupom
    private String dataEmissaoFormatada;
    private String horaEmissaoFormatada;
    private String contadorCupomFiscal;
    private String contadorOrdemOperacao;

    // Dados do consumidor
    private String cpfOuCnpjConsumidor;
    private String nomeConsumidor;
    private String enderecoConsumidor;

    // Itens
    private List<ReceiptItemDto> listaItensVenda;

    // Totais
    private BigDecimal valorSubtotalVenda;
    private BigDecimal valorDescontoVenda;
    private BigDecimal valorTotalFinalVenda;

    // Pagamento
    private String descricaoFormaPagamento;
    private String valorRecebidoPagamento;
    private BigDecimal valorTrocoPagamento;
    // Venda parcelada (cartão de crédito)
    private Integer quantidadeParcelas;
    private BigDecimal valorParcela;

    // Dados fiscais avançados
    private String hashMd5CupomFiscal;
    private String numeroSerieEquipamentoFiscal;
    private String modeloEquipamentoFiscal;
    private String versaoSoftwareAplicativoFiscal;

    // NFC-e
    private String chaveAcessoNfce;
}
