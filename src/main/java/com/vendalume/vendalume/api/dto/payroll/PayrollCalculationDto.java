package com.vendalume.vendalume.api.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Objeto de transferência (DTO) PayrollCalculationDto.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollCalculationDto {

    private BigDecimal salary;
    private BigDecimal periculosidade;
    private BigDecimal overtimeVal;
    private BigDecimal dsrVal;
    private BigDecimal healthDed;
    private BigDecimal totalProventos;
    private BigDecimal inssVal;
    private BigDecimal irrfVal;
    private BigDecimal totalDescontos;
    private BigDecimal liquido;
}
