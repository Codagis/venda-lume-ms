package com.vendalume.vendalume.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Entidade que representa RegisterOperatorId no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class RegisterOperatorId implements Serializable {

    private UUID registerId;
    private UUID userId;
}
