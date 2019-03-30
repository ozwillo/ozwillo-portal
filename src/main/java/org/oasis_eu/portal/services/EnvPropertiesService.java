package org.oasis_eu.portal.services;

import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class EnvPropertiesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvPropertiesService.class);

    private final EnvProperties envProperties;

    private final HttpServletRequest request;

    @Autowired
    public EnvPropertiesService(EnvProperties envProperties, HttpServletRequest request) {
        this.envProperties = envProperties;
        this.request = request;
    }

    @PostConstruct
    public void checkDefaultConfig() {
        if (getDefaultConfig() == null) {
            throw new RuntimeException("There is no default portal defined !");
        }
    }

    public String getCurrentKey() {
        return extractCurrentEnv().getKey();
    }

    public EnvConfig getCurrentConfig() {
        return extractCurrentEnv().getValue();
    }

    /**
     * Only used by background jobs that need to run in an authenticated context
     * with the system admin user's refresh token.
     */
    public EnvConfig getDefaultConfig() {
        return getFirstConfMatching(entry -> entry.getValue().getIsDefaultConf())
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private Map.Entry<String, EnvConfig> extractCurrentEnv() {
        Optional<String> hostname = extractHostname(request.getRequestURL().toString());
        return getFirstConfMatching(entry -> hostname.isPresent() && entry.getValue().getBaseUrl().contains(hostname.get()))
                .orElseThrow(() -> new RuntimeException("No configuration found for " + hostname));
    }

    private Optional<Map.Entry<String, EnvConfig>> getFirstConfMatching(Predicate<? super Map.Entry<String, EnvConfig>> predicate) {
        return envProperties.getConfs()
                .entrySet()
                .stream()
                .filter(predicate)
                .findFirst();
    }

    private Optional<String> extractHostname(String requestUrl) {
        try {
            URL url = new URL(requestUrl);
            return Optional.of(url.getHost());
        } catch (MalformedURLException e) {
            LOGGER.error("Really unexpected malformed URL : {}", requestUrl);
            return Optional.empty();
        }
    }
}
