package com.vendalume.vendalume.api.controller.interfaces;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.product.ProductCreateRequest;
import com.vendalume.vendalume.api.dto.product.ProductFilterRequest;
import com.vendalume.vendalume.api.dto.product.ProductResponse;
import com.vendalume.vendalume.api.dto.product.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Interface do controller de produtos.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_PRODUCTS, description = "Cadastro, listagem, filtragem e gestão de produtos")
@DefaultApiResponses
public interface ProductControllerApi {

    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto no cadastro do tenant")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou SKU/código de barras já cadastrado")
    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request);

    @Operation(summary = "Buscar por ID", description = "Retorna o produto pelo identificador")
    @ApiResponse(responseCode = "200", description = "Produto encontrado", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Buscar por SKU", description = "Retorna o produto pelo código SKU")
    @ApiResponse(responseCode = "200", description = "Produto encontrado", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @GetMapping("/sku/{sku}")
    ResponseEntity<ProductResponse> findBySku(@PathVariable String sku);

    @Operation(summary = "Buscar por código de barras", description = "Retorna o produto pelo código de barras")
    @ApiResponse(responseCode = "200", description = "Produto encontrado", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @GetMapping("/barcode/{barcode}")
    ResponseEntity<ProductResponse> findByBarcode(@PathVariable String barcode);

    @Operation(summary = "Buscar com filtros", description = "Lista produtos paginados com filtros avançados. Root pode passar tenantId (query ou body) para filtrar por empresa.")
    @ApiResponse(responseCode = "200", description = "Lista de produtos", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @PostMapping("/search")
    ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody ProductFilterRequest filter);

    @Operation(summary = "Listar ativos", description = "Lista todos os produtos ativos do tenant")
    @ApiResponse(responseCode = "200", description = "Lista de produtos ativos")
    @GetMapping("/active")
    ResponseEntity<List<ProductResponse>> listActive();

    @Operation(summary = "Listar disponíveis para venda", description = "Produtos ativos e disponíveis no PDV")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    @GetMapping("/available-sale")
    ResponseEntity<List<ProductResponse>> listAvailableForSale();

    @Operation(summary = "Listar disponíveis para delivery", description = "Produtos ativos e disponíveis para delivery")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    @GetMapping("/available-delivery")
    ResponseEntity<List<ProductResponse>> listAvailableForDelivery();

    @Operation(summary = "Listar em destaque", description = "Produtos marcados como destaque")
    @ApiResponse(responseCode = "200", description = "Lista de produtos em destaque")
    @GetMapping("/featured")
    ResponseEntity<List<ProductResponse>> listFeatured();

    @Operation(summary = "Listar com estoque baixo", description = "Produtos com quantidade abaixo do mínimo")
    @ApiResponse(responseCode = "200", description = "Lista de produtos com estoque baixo")
    @GetMapping("/low-stock")
    ResponseEntity<List<ProductResponse>> listLowStock();

    @Operation(summary = "Atualizar produto", description = "Atualiza dados do produto")
    @ApiResponse(responseCode = "200", description = "Produto atualizado", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou SKU/código de barras duplicado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @PutMapping("/{id}")
    ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request);

    @Operation(summary = "Atualizar estoque", description = "Adiciona ou remove quantidade do estoque")
    @ApiResponse(responseCode = "200", description = "Estoque atualizado", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Produto sem controle de estoque ou quantidade insuficiente")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @PatchMapping("/{id}/stock")
    ResponseEntity<ProductResponse> updateStock(
            @PathVariable UUID id,
            @Parameter(description = "Quantidade a adicionar (positivo) ou remover (negativo)") @RequestParam BigDecimal quantity);

    @Operation(summary = "Excluir produto", description = "Remove o produto do cadastro")
    @ApiResponse(responseCode = "204", description = "Produto excluído")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
