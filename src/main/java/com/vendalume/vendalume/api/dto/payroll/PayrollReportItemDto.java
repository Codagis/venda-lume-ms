package com.vendalume.vendalume.api.dto.payroll;

import com.vendalume.vendalume.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollReportItemDto {

    private UUID employeeId;
    private String employeeName;
    private String document;
    private String role;
    private BigDecimal salary;
    private Integer paymentDay;
    private UUID payableId;
    private LocalDate dueDate;
    private AccountStatus status;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
}
