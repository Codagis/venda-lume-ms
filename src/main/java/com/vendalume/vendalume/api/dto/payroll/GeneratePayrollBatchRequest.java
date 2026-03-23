package com.vendalume.vendalume.api.dto.payroll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para gerar contas a pagar de folha para funcionários e meses específicos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePayrollBatchRequest {

    /**
     * IDs dos funcionários. Se vazio, usa todos os ativos com salário.
     */
    private List<UUID> employeeIds;

    /**
     * Meses no formato year-month. Ex: [{"year": 2025, "month": 1}, {"year": 2025, "month": 2}]
     */
    @NotNull(message = "Informe os meses")
    @NotEmpty(message = "Informe pelo menos um mês")
    @Valid
    private List<MonthRef> months;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthRef {
        @NotNull
        @Min(2020)
        @Max(2100)
        private Integer year;

        @NotNull
        @Min(1)
        @Max(12)
        private Integer month;
    }
}
