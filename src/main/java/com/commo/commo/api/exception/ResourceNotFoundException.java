package com.commo.commo.api.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s não encontrado: %s", resource, id));
    }
}
