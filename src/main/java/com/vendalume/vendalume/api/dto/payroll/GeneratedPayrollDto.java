package com.vendalume.vendalume.api.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa uma folha de pagamento já gerada (existe ao menos uma conta a pagar com essa referência).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedPayrollDto {

    private String payrollReference;
    private int year;
    private int month;
    private String label;
}
