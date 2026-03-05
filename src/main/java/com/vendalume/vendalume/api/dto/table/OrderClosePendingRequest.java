package com.vendalume.vendalume.api.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderClosePendingRequest {

    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String notes;
}
