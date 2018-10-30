package org.oasis_eu.portal.services;

import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class EnvPropertiesService {

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    private HttpServletRequest request;

    public EnvConfig getConfig(String domain_name) {
        String sanitized_domain_name = sanitizedDomaineName(domain_name);
        EnvConfig envConfig = envProperties.getConfs().get(sanitized_domain_name);
        if (envConfig != null) {
            return envConfig;
        } else {
            return envProperties.getConfs().get("ozwillo");
        }
    }

    public String sanitizedDomaineName(String domain_name){
        return domain_name.replaceAll(".*\\.(?=.*\\.)", "").replaceAll("\\..*", "");
    }

    public EnvConfig getCurrentConfig(){
        String website = sanitizedDomaineName(request.getServerName());
        return getConfig(website);
    }


}
