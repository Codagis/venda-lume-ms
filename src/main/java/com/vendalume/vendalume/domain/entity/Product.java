package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.UnitOfMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
import java.util.UUID;

/**
 * Entidade que representa um produto no sistema VendaLume. Modelo completo para mercados,
 * restaurantes, PDV e delivery, com suporte a preços, estoque, categorização, dimensões,
 * informações nutricionais, promoções e regras de venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_sku", columnList = "sku"),
                @Index(name = "idx_product_barcode", columnList = "barcode"),
                @Index(name = "idx_product_tenant_active", columnList = "tenant_id, active"),
                @Index(name = "idx_product_tenant_sku", columnList = "tenant_id, sku"),
                @Index(name = "idx_product_tenant_name", columnList = "tenant_id, name"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_active", columnList = "active"),
                @Index(name = "idx_product_available_sale", columnList = "available_for_sale"),
                @Index(name = "idx_product_featured", columnList = "featured"),
                @Index(name = "idx_product_display_order", columnList = "display_order"),
                @Index(name = "idx_product_created_at", columnList = "created_at"),
                @Index(name = "idx_product_brand", columnList = "brand"),
                @Index(name = "idx_product_tenant_category_active", columnList = "tenant_id, category_id, active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_tenant_sku", columnNames = {"tenant_id", "sku"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Product extends BaseAuditableEntity {

    @Comment("Identificador único do produto")
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant ou organização, obrigatório no modelo multi-tenancy")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("SKU - código interno único do produto por tenant (Stock Keeping Unit)")
    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Comment("Código de barras EAN/GTIN para leitura em balanças e leitores")
    @Column(name = "barcode", length = 20)
    private String barcode;

    @Comment("Código alternativo ou de referência interna")
    @Column(name = "internal_code", length = 50)
    private String internalCode;

    @Comment("Nome comercial do produto exibido em listagens e PDV")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Comment("Descrição resumida para exibição em listas e cards")
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Comment("Descrição completa e detalhada do produto")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Comment("Preço unitário de venda do produto")
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Comment("Preço de custo para cálculo de margem e relatórios")
    @Column(name = "cost_price", precision = 19, scale = 4)
    private BigDecimal costPrice;

    @Comment("Preço promocional quando em oferta")
    @Column(name = "discount_price", precision = 19, scale = 4)
    private BigDecimal discountPrice;

    @Comment("Data e hora de início da vigência da promoção")
    @Column(name = "discount_start_at")
    private java.time.LocalDateTime discountStartAt;

    @Comment("Data e hora de término da vigência da promoção")
    @Column(name = "discount_end_at")
    private java.time.LocalDateTime discountEndAt;

    @Comment("Alíquota de imposto aplicável ao produto em percentual")
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Comment("Unidade de medida do produto (unidade, kg, litro, etc)")
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 10)
    @Builder.Default
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UN;

    @Comment("Indica se o produto é vendido por peso em balança")
    @Column(name = "sell_by_weight", nullable = false)
    @Builder.Default
    private Boolean sellByWeight = false;

    @Comment("Indica se o sistema deve controlar o estoque deste produto")
    @Column(name = "track_stock", nullable = false)
    @Builder.Default
    private Boolean trackStock = false;

    @Comment("Se true, dá baixa no estoque automaticamente nas vendas. Se false, a baixa deve ser feita manualmente.")
    @Column(name = "deduct_stock_on_sale", nullable = false)
    @Builder.Default
    private Boolean deductStockOnSale = true;

    @Comment("Quantidade atual disponível em estoque")
    @Column(name = "stock_quantity", precision = 19, scale = 4)
    private BigDecimal stockQuantity;

    @Comment("Estoque mínimo para alertas de reposição")
    @Column(name = "min_stock", precision = 19, scale = 4)
    private BigDecimal minStock;

    @Comment("Permite venda mesmo com quantidade em estoque negativa")
    @Column(name = "allow_negative_stock", nullable = false)
    @Builder.Default
    private Boolean allowNegativeStock = false;

    @Comment("ID da categoria do produto para agrupamento e filtros")
    @Column(name = "category_id", columnDefinition = "UUID")
    private UUID categoryId;

    @Comment("Marca ou fabricante do produto")
    @Column(name = "brand", length = 100)
    private String brand;

    @Comment("NCM - Nomenclatura Comum do Mercosul para fiscal")
    @Column(name = "ncm", length = 10)
    private String ncm;

    @Comment("CEST - Código Especificador da Substituição Tributária")
    @Column(name = "cest", length = 9)
    private String cest;

    @Comment("Peso do produto em quilogramas para frete e logística")
    @Column(name = "weight", precision = 10, scale = 4)
    private BigDecimal weight;

    @Comment("Largura da embalagem em centímetros")
    @Column(name = "width", precision = 10, scale = 2)
    private BigDecimal width;

    @Comment("Altura da embalagem em centímetros")
    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;

    @Comment("Profundidade da embalagem em centímetros")
    @Column(name = "depth", precision = 10, scale = 2)
    private BigDecimal depth;

    @Comment("Tempo estimado de preparo em minutos para restaurantes")
    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;

    @Comment("Tamanho da porção ou servindo (ex: 300g, 1 fatia)")
    @Column(name = "serve_size", length = 50)
    private String serveSize;

    @Comment("Calorias por porção em kcal")
    @Column(name = "calories")
    private Integer calories;

    @Comment("Lista de ingredientes em texto ou JSON")
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;

    @Comment("Informações sobre alérgenos presentes no produto")
    @Column(name = "allergens", length = 500)
    private String allergens;

    @Comment("Informações nutricionais em texto ou JSON")
    @Column(name = "nutritional_info", columnDefinition = "TEXT")
    private String nutritionalInfo;

    @Comment("Quantidade mínima permitida por pedido")
    @Column(name = "min_order_quantity", precision = 10, scale = 4)
    private BigDecimal minOrderQuantity;

    @Comment("Quantidade máxima permitida por pedido")
    @Column(name = "max_order_quantity", precision = 10, scale = 4)
    private BigDecimal maxOrderQuantity;

    @Comment("Múltiplo obrigatório de venda (ex: só em dúzias)")
    @Column(name = "sell_multiple", precision = 10, scale = 4)
    private BigDecimal sellMultiple;

    @Comment("Indica se o produto está ativo no cadastro")
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Comment("Indica se o produto está disponível para venda no PDV")
    @Column(name = "available_for_sale", nullable = false)
    @Builder.Default
    private Boolean availableForSale = true;

    @Comment("Indica se o produto está disponível para pedidos de delivery")
    @Column(name = "available_for_delivery", nullable = false)
    @Builder.Default
    private Boolean availableForDelivery = true;

    @Comment("Indica se o produto aparece em destaque nas listagens")
    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Comment("Indica se o produto é composto/kit de outros produtos")
    @Column(name = "is_composite", nullable = false)
    @Builder.Default
    private Boolean isComposite = false;

    @Comment("Ordem de exibição em listagens e categorias")
    @Column(name = "display_order")
    private Integer displayOrder;

    @Comment("URL da imagem principal do produto")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Comment("URLs de imagens adicionais em JSON array")
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Comment("URL de vídeo demonstrativo do produto")
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Comment("Produto deve aparecer em NFC-e (cupom fiscal)")
    @Column(name = "emits_nfce", nullable = false)
    @Builder.Default
    private Boolean emitsNfce = true;

    @Comment("Produto deve aparecer em NF-e")
    @Column(name = "emits_nfe", nullable = false)
    @Builder.Default
    private Boolean emitsNfe = false;

    @Comment("Produto deve aparecer no comprovante simples de venda")
    @Column(name = "emits_comprovante_simples", nullable = false)
    @Builder.Default
    private Boolean emitsComprovanteSimples = true;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
