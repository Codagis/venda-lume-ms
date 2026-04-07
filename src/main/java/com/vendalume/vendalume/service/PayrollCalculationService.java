package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.payroll.PayrollCalculationDto;
import com.vendalume.vendalume.domain.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Serviço de negócio PayrollCalculationService.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Service
@RequiredArgsConstructor
public class PayrollCalculationService {

    private final IrrfCalculatorService irrfCalculator;

    public PayrollCalculationDto calculate(Employee emp) {
        BigDecimal salary = emp.getSalary() != null ? emp.getSalary() : BigDecimal.ZERO;
        BigDecimal periculosidade = BigDecimal.ZERO;
        if (emp.getHazardousPayPercent() != null && emp.getHazardousPayPercent().compareTo(BigDecimal.ZERO) > 0) {
            periculosidade = salary.multiply(emp.getHazardousPayPercent()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        }
        BigDecimal overtimeVal = emp.getOvertimeValue() != null ? emp.getOvertimeValue() : BigDecimal.ZERO;
        BigDecimal dsrVal = emp.getDsrValue() != null ? emp.getDsrValue() : BigDecimal.ZERO;
        BigDecimal healthDed = emp.getHealthPlanDeduction() != null ? emp.getHealthPlanDeduction() : BigDecimal.ZERO;

        BigDecimal totalProventos = salary.add(periculosidade).add(overtimeVal).add(dsrVal);
        BigDecimal baseInss = totalProventos;

        BigDecimal inssVal = BigDecimal.ZERO;
        if (emp.getInssPercent() != null && emp.getInssPercent().compareTo(BigDecimal.ZERO) > 0) {
            inssVal = baseInss.multiply(emp.getInssPercent()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        }

        BigDecimal baseIrrf = totalProventos.subtract(inssVal);
        int dependentes = emp.getDependentes() != null ? Math.max(0, emp.getDependentes()) : 0;
        BigDecimal irrfVal = irrfCalculator.calculate(baseIrrf, dependentes);

        BigDecimal totalDescontos = healthDed.add(inssVal).add(irrfVal);
        BigDecimal liquido = totalProventos.subtract(totalDescontos);

        return PayrollCalculationDto.builder()
                .salary(salary)
                .periculosidade(periculosidade)
                .overtimeVal(overtimeVal)
                .dsrVal(dsrVal)
                .healthDed(healthDed)
                .totalProventos(totalProventos)
                .inssVal(inssVal)
                .irrfVal(irrfVal)
                .totalDescontos(totalDescontos)
                .liquido(liquido)
                .build();
    }
}
