package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.DeliveryPriority;
import com.vendalume.vendalume.domain.enums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma entrega de delivery no sistema VendaLume. Controla o ciclo completo
 * desde a atribuição do entregador até a confirmação da entrega, com endereço, rota, taxas
 * e comprovantes.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Entity
@Table(
        name = "deliveries",
        indexes = {
                @Index(name = "idx_delivery_tenant_date", columnList = "tenant_id, created_at"),
                @Index(name = "idx_delivery_tenant_status", columnList = "tenant_id, status"),
                @Index(name = "idx_delivery_tenant_number", columnList = "tenant_id, delivery_number"),
                @Index(name = "idx_delivery_sale", columnList = "sale_id"),
                @Index(name = "idx_delivery_person", columnList = "delivery_person_id"),
                @Index(name = "idx_delivery_scheduled", columnList = "scheduled_at"),
                @Index(name = "idx_delivery_delivered_at", columnList = "delivered_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_delivery_tenant_number", columnNames = {"tenant_id", "delivery_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"sale", "deliveryPerson"})
public class Delivery extends BaseAuditableEntity {

    @Comment("Identificador único da entrega")
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant ou organização")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Número sequencial da entrega por tenant para identificação")
    @Column(name = "delivery_number", nullable = false, length = 20)
    private String deliveryNumber;

    @Comment("Venda associada à entrega")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Comment("Entregador responsável pela corrida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id")
    private User deliveryPerson;

    @Comment("Status atual da entrega no fluxo")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Comment("Prioridade da entrega para ordenação")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    @Builder.Default
    private DeliveryPriority priority = DeliveryPriority.NORMAL;

    @Comment("Nome do destinatário para identificação na entrega")
    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Comment("Telefone do destinatário para contato durante a entrega")
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Comment("Endereço completo de entrega")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Comment("Complemento ou referência do endereço")
    @Column(name = "complement", length = 255)
    private String complement;

    @Comment("CEP do endereço de entrega")
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Comment("Bairro do endereço de entrega")
    @Column(name = "neighborhood", length = 100)
    private String neighborhood;

    @Comment("Cidade do endereço de entrega")
    @Column(name = "city", length = 100)
    private String city;

    @Comment("Estado/UF do endereço de entrega")
    @Column(name = "state", length = 2)
    private String state;

    @Comment("Coordenada de latitude para roteirização")
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Comment("Coordenada de longitude para roteirização")
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Comment("Instruções específicas para o entregador")
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Comment("Data e hora prevista para a entrega")
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Comment("Data e hora em que o entregador aceitou a corrida")
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Comment("Data e hora em que o pedido foi retirado no estabelecimento")
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Comment("Data e hora em que o entregador iniciou o deslocamento")
    @Column(name = "departed_at")
    private LocalDateTime departedAt;

    @Comment("Data e hora em que o entregador chegou no endereço")
    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Comment("Data e hora em que a entrega foi concluída")
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Comment("Distância estimada em quilômetros")
    @Column(name = "estimated_distance_km", precision = 8, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Comment("Distância real percorrida em quilômetros")
    @Column(name = "actual_distance_km", precision = 8, scale = 2)
    private BigDecimal actualDistanceKm;

    @Comment("Tempo estimado de entrega em minutos")
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Comment("Tempo real de entrega em minutos")
    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Comment("Valor da taxa de entrega cobrada")
    @Column(name = "delivery_fee", precision = 19, scale = 4)
    private BigDecimal deliveryFee;

    @Comment("Valor da gorjeta para o entregador")
    @Column(name = "tip_amount", precision = 19, scale = 4)
    private BigDecimal tipAmount;

    @Comment("URL ou identificador do comprovante de entrega (foto/assinatura)")
    @Column(name = "proof_of_delivery_url", length = 500)
    private String proofOfDeliveryUrl;

    @Comment("Nome ou descrição de quem recebeu a entrega")
    @Column(name = "received_by", length = 150)
    private String receivedBy;

    @Comment("Observações do entregador ao concluir a entrega")
    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Comment("Motivo da falha quando status FAILED")
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Comment("Motivo da devolução quando status RETURNED")
    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Comment("Data e hora do cancelamento quando aplicável")
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Comment("ID do usuário que cancelou a entrega")
    @Column(name = "cancelled_by", columnDefinition = "UUID")
    private UUID cancelledBy;

    @Comment("Quantidade de tentativas de entrega realizadas")
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 1;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
