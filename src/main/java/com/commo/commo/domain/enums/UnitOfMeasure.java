package com.commo.commo.domain.enums;

import lombok.Getter;

/**
 * Enumeração das unidades de medida suportadas para produtos. Atende mercados, restaurantes,
 * PDV e delivery, incluindo unidades por peso, volume, embalagem e porção.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
public enum UnitOfMeasure {

    UN("Unidade", "un"),
    KG("Quilograma", "kg"),
    G("Grama", "g"),
    LT("Litro", "L"),
    ML("Mililitro", "ml"),
    CX("Caixa", "cx"),
    PC("Pacote", "pç"),
    EMB("Embalagem", "emb"),
    DZ("Dúzia", "dz"),
    MD("Meia dúzia", "1/2dz"),
    PORC("Porção", "porção"),
    FAT("Fatia", "fat"),
    M("Metro", "m"),
    MLN("Metro linear", "ml"),
    PAR("Par", "par"),
    KIT("Kit/Conjunto", "kit");

    private final String description;
    private final String abbreviation;

    UnitOfMeasure(String description, String abbreviation) {
        this.description = description;
        this.abbreviation = abbreviation;
    }
}
