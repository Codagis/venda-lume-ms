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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequest {

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
