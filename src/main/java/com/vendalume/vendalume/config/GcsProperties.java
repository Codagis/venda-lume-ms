package com.vendalume.vendalume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades para integração com Google Cloud Storage (uploads de imagens).
 *
 * @author VendaLume
 */
@Component
@ConfigurationProperties(prefix = "vendalume.gcs")
public class GcsProperties {

    private boolean enabled = false;
    private String bucketName = "";
    private String projectId = "";
    /** Caminho do arquivo JSON da conta de serviço (alternativa a credentialsJson). */
    private String credentialsPath = "";
    /** JSON completo da chave da conta de serviço (uma única string). Prioridade sobre credentialsPath. */
    private String credentialsJson = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName != null ? bucketName : "";
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId != null ? projectId : "";
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath != null ? credentialsPath : "";
    }

    public String getCredentialsJson() {
        return credentialsJson;
    }

    public void setCredentialsJson(String credentialsJson) {
        this.credentialsJson = credentialsJson != null ? credentialsJson : "";
    }
}
