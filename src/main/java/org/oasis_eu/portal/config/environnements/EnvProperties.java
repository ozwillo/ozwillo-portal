package org.oasis_eu.portal.config.environnements;

import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "")
public class EnvProperties {

    private Map<String, EnvConfig> confs;

    public Map<String, EnvConfig> getConfs() {
        return confs;
    }

    public void setConfs(Map<String, EnvConfig> confs) {
        this.confs = confs;
    }
}
