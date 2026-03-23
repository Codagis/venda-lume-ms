package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.costcontrol.AccountPayableCreateRequest;
import com.vendalume.vendalume.api.dto.costcontrol.AccountPayableResponse;
import com.vendalume.vendalume.api.dto.payroll.GeneratePayrollBatchRequest;
import com.vendalume.vendalume.api.dto.payroll.GeneratedPayrollDto;
import com.vendalume.vendalume.api.dto.payroll.PayrollReportItemDto;
import com.vendalume.vendalume.domain.entity.AccountPayable;
import com.vendalume.vendalume.domain.entity.Employee;
import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.repository.AccountPayableRepository;
import com.vendalume.vendalume.repository.EmployeeRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gera contas a pagar recorrentes mensais (folha) a partir dos funcionários ativos.
 */
@Service
@RequiredArgsConstructor
public class PayrollService {

    private static final String CATEGORY_FOLHA = "Folha";

    private final EmployeeRepository employeeRepository;
    private final AccountPayableRepository apRepository;
    private final CostControlService costControlService;
    private final PayrollCalculationService payrollCalculationService;

    /**
     * Gera contas a pagar para o mês/ano para todos os funcionários ativos que ainda não possuem conta no mês.
     * Descrição: "Pagamento [Nome do funcionário]", vencimento no dia configurado do funcionário.
     */
    @Transactional
    public List<AccountPayableResponse> generateMonthlyPayables(UUID tenantIdParam, int year, int month) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        String payrollRef = String.format("%d-%02d", year, month);
        YearMonth ym = YearMonth.of(year, month);
        List<Employee> employees = employeeRepository.findByTenantIdAndActiveTrueOrderByName(tenantId);
        List<AccountPayableResponse> created = new java.util.ArrayList<>();
        UUID userId = SecurityUtils.getCurrentUserId();

        for (Employee emp : employees) {
            if (emp.getSalary() == null || emp.getSalary().compareTo(BigDecimal.ZERO) <= 0) continue;
            if ("PJ".equalsIgnoreCase(emp.getContractType() != null ? emp.getContractType() : "CLT")) continue;
            if (apRepository.existsByTenantIdAndEmployeeIdAndPayrollReference(tenantId, emp.getId(), payrollRef)) continue;

            var calc = payrollCalculationService.calculate(emp);
            BigDecimal valorLiquido = calc.getLiquido() != null ? calc.getLiquido() : emp.getSalary();

            int day = Math.min(emp.getPaymentDay() != null ? emp.getPaymentDay() : 5, ym.lengthOfMonth());
            LocalDate dueDate = ym.atDay(day);

            AccountPayableCreateRequest req = AccountPayableCreateRequest.builder()
                    .tenantId(tenantId)
                    .employeeId(emp.getId())
                    .payrollReference(payrollRef)
                    .description("Pagamento " + emp.getName())
                    .category(CATEGORY_FOLHA)
                    .dueDate(dueDate)
                    .amount(valorLiquido)
                    .build();
            AccountPayableResponse resp = costControlService.createPayable(req);
            created.add(resp);
        }
        return created;
    }

    /**
     * Gera contas a pagar para os funcionários e meses especificados.
     * Não gera duplicatas para o mesmo funcionário/mês.
     */
    @Transactional
    public List<AccountPayableResponse> generatePayablesBatch(UUID tenantIdParam, GeneratePayrollBatchRequest request) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        if (request.getMonths() == null || request.getMonths().isEmpty()) {
            return Collections.emptyList();
        }
        List<Employee> employees;
        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            employees = employeeRepository.findAllById(request.getEmployeeIds()).stream()
                    .filter(e -> e.getTenantId().equals(tenantId))
                    .collect(Collectors.toList());
        } else {
            employees = employeeRepository.findByTenantIdAndActiveTrueOrderByName(tenantId);
        }
        List<AccountPayableResponse> created = new ArrayList<>();
        for (Employee emp : employees) {
            if (emp.getSalary() == null || emp.getSalary().compareTo(BigDecimal.ZERO) <= 0) continue;
            if ("PJ".equalsIgnoreCase(emp.getContractType() != null ? emp.getContractType() : "CLT")) continue;
            var calc = payrollCalculationService.calculate(emp);
            BigDecimal valorLiquido = calc.getLiquido() != null ? calc.getLiquido() : emp.getSalary();
            for (GeneratePayrollBatchRequest.MonthRef m : request.getMonths()) {
                String payrollRef = String.format("%d-%02d", m.getYear(), m.getMonth());
                YearMonth ym = YearMonth.of(m.getYear(), m.getMonth());
                if (apRepository.existsByTenantIdAndEmployeeIdAndPayrollReference(tenantId, emp.getId(), payrollRef)) continue;
                int day = Math.min(emp.getPaymentDay() != null ? emp.getPaymentDay() : 5, ym.lengthOfMonth());
                LocalDate dueDate = ym.atDay(day);
                AccountPayableCreateRequest req = AccountPayableCreateRequest.builder()
                        .tenantId(tenantId)
                        .employeeId(emp.getId())
                        .payrollReference(payrollRef)
                        .description("Pagamento " + emp.getName())
                        .category(CATEGORY_FOLHA)
                        .dueDate(dueDate)
                        .amount(valorLiquido)
                        .build();
                AccountPayableResponse resp = costControlService.createPayable(req);
                created.add(resp);
            }
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<PayrollReportItemDto> getPayrollReport(UUID tenantIdParam, int year, int month) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        String payrollRef = String.format("%d-%02d", year, month);
        YearMonth ym = YearMonth.of(year, month);
        List<Employee> employees = employeeRepository.findByTenantIdAndActiveTrueOrderByName(tenantId);
        List<AccountPayable> payables = apRepository.findByTenantIdAndPayrollReference(tenantId, payrollRef);
        Map<UUID, AccountPayable> payableByEmployee = payables.stream()
                .filter(p -> p.getEmployeeId() != null)
                .collect(Collectors.toMap(AccountPayable::getEmployeeId, p -> p, (a, b) -> a));

        List<PayrollReportItemDto> result = new ArrayList<>();
        for (Employee emp : employees) {
            AccountPayable ap = payableByEmployee.get(emp.getId());
            int day = Math.min(emp.getPaymentDay() != null ? emp.getPaymentDay() : 5, ym.lengthOfMonth());
            LocalDate dueDate = ym.atDay(day);
            result.add(PayrollReportItemDto.builder()
                    .employeeId(emp.getId())
                    .employeeName(emp.getName())
                    .document(emp.getDocument())
                    .role(emp.getRole())
                    .salary(emp.getSalary() != null ? emp.getSalary() : BigDecimal.ZERO)
                    .paymentDay(emp.getPaymentDay())
                    .payableId(ap != null ? ap.getId() : null)
                    .dueDate(ap != null ? ap.getDueDate() : dueDate)
                    .status(ap != null ? ap.getStatus() : null)
                    .paidAmount(ap != null ? ap.getPaidAmount() : BigDecimal.ZERO)
                    .paymentDate(ap != null ? ap.getPaymentDate() : null)
                    .build());
        }
        return result;
    }

    private static final String[] MESES_PT = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };

    @Transactional(readOnly = true)
    public List<GeneratedPayrollDto> listGeneratedPayrolls(UUID tenantIdParam) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        List<String> refs = apRepository.findDistinctPayrollReferencesByTenantId(tenantId);
        List<GeneratedPayrollDto> result = new ArrayList<>();
        for (String ref : refs) {
            String[] parts = ref.split("-");
            if (parts.length != 2) continue;
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            if (month < 1 || month > 12) continue;
            String label = MESES_PT[month - 1] + " / " + year;
            result.add(GeneratedPayrollDto.builder()
                    .payrollReference(ref)
                    .year(year)
                    .month(month)
                    .label(label)
                    .build());
        }
        return result;
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) {
                throw new IllegalArgumentException("Selecione a empresa para gerar a folha.");
            }
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }
}
