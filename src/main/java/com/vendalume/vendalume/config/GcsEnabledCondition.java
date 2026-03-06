package com.vendalume.vendalume.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condição para criar beans GCS apenas quando vendalume.gcs.enabled=true.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
public class GcsEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return "true".equalsIgnoreCase(context.getEnvironment().getProperty("vendalume.gcs.enabled", "false"));
    }
}
