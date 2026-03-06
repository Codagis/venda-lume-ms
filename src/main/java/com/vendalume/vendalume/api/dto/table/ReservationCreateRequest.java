package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.ReservationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de requisição para criar reserva.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequest {
    private UUID tenantId;

    @NotNull(message = "Mesa é obrigatória")
    private UUID tableId;

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(max = 255)
    private String customerName;

    @Size(max = 20)
    private String customerPhone;

    @Size(max = 255)
    private String customerEmail;

    @NotNull(message = "Data/hora agendada é obrigatória")
    private Instant scheduledAt;

    @NotNull(message = "Número de pessoas é obrigatório")
    @Min(value = 1, message = "Número de pessoas deve ser pelo menos 1")
    private Integer numberOfGuests;

    private ReservationStatus status;

    private String notes;
}
