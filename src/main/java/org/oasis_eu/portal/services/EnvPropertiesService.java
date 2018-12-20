package org.oasis_eu.portal.services;

import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EnvPropertiesService {
    private static final Logger logger = LoggerFactory.getLogger(EnvPropertiesService.class);

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    private HttpServletRequest request;

    public EnvConfig getConfig() {
        String website = extractEnvKey();
        return envProperties.getConfs().get(website);
    }

    public String extractEnvKey() {
        try {
            String URL = extractURL(request.getRequestURL().toString());
            Map<String, EnvConfig> envConfigMap = envProperties.getConfs();

            for (Map.Entry<String, EnvConfig> entry : envConfigMap.entrySet()) {
                String key = entry.getKey();
                EnvConfig envConfig = entry.getValue();

                if (envConfig.getBaseUrl().contains(URL)) {
                    return key;
                }
            }
        } catch (Exception e) {
            logger.trace("No request defined, so extract default conf");
        }
        return extractDefaultConfKey();
    }


        public EnvConfig getCurrentConfig () {
            try {
                return getConfig();
            } catch (Exception e) {
                return null;
            }
        }

        private String extractURL (String URL){
            Pattern p = Pattern.compile("((https|http)?:\\/\\/)(.*?)(?=\\/)");
            Matcher m = p.matcher(URL);
            m.find();
            return m.group(0);
        }


        public String extractDefaultConfKey () {
            String defaultConfKey = envProperties
                    .getConfs()
                    .entrySet()
                    .stream()
                    .filter(elem -> elem.getValue().getIsDefaultConf())
                    .map(defaultConfElem -> defaultConfElem.getKey())
                    .findFirst()
                    .orElse(null);

            return defaultConfKey;
        }

    }
