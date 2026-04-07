package com.vendalume.vendalume.api.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de transferência (DTO) GeneratedPayrollDto.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
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
