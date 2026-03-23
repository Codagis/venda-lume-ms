package com.vendalume.vendalume.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cálculo do IRRF (Imposto de Renda Retido na Fonte) conforme tabela vigente no Brasil.
 * Tabela progressiva mensal - Lei vigente (2025, jan-abr).
 * Base: total de rendimentos - INSS - (dependentes × R$ 189,59).
 */
@Service
public class IrrfCalculatorService {

    /** Dedução por dependente (R$ 189,59) - valor mensal conforme Receita Federal */
    public static final BigDecimal DEDUCAO_POR_DEPENDENTE = new BigDecimal("189.59");

    /**
     * Calcula o IRRF mensal sobre a base de cálculo.
     * Base de cálculo = rendimentos tributáveis - INSS - (dependentes × 189,59).
     *
     * @param baseTributavel base já descontada do INSS (proventos - INSS)
     * @param dependentes    número de dependentes para dedução
     * @return valor do IRRF (zero se base após dedução for negativa ou isenta)
     */
    public BigDecimal calculate(BigDecimal baseTributavel, int dependentes) {
        if (baseTributavel == null || baseTributavel.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal deducaoDependentes = DEDUCAO_POR_DEPENDENTE.multiply(BigDecimal.valueOf(dependentes));
        BigDecimal base = baseTributavel.subtract(deducaoDependentes);
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return applyTable(base);
    }

    /**
     * Aplica a tabela progressiva do IRRF mensal (vigente 2025 - jan a abr).
     * Faixas: até 2.259,20 isento; 7,5% - 169,44; 15% - 381,44; 22,5% - 662,77; 27,5% - 896,00.
     */
    private BigDecimal applyTable(BigDecimal base) {
        // Até 2.259,20: isento
        if (base.compareTo(new BigDecimal("2259.20")) <= 0) {
            return BigDecimal.ZERO;
        }
        // 2.259,21 a 2.826,65: 7,5% - 169,44
        if (base.compareTo(new BigDecimal("2826.65")) <= 0) {
            return base.multiply(new BigDecimal("0.075")).subtract(new BigDecimal("169.44")).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        }
        // 2.826,66 a 3.751,05: 15% - 381,44
        if (base.compareTo(new BigDecimal("3751.05")) <= 0) {
            return base.multiply(new BigDecimal("0.15")).subtract(new BigDecimal("381.44")).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        }
        // 3.751,06 a 4.664,68: 22,5% - 662,77
        if (base.compareTo(new BigDecimal("4664.68")) <= 0) {
            return base.multiply(new BigDecimal("0.225")).subtract(new BigDecimal("662.77")).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        }
        // Acima de 4.664,68: 27,5% - 896,00
        return base.multiply(new BigDecimal("0.275")).subtract(new BigDecimal("896.00")).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }
}
