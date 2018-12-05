package org.oasis_eu.portal.services;

import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EnvPropertiesService {

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    private HttpServletRequest request;

    public EnvConfig getConfig() {
        String website = extractEnvKey();
        EnvConfig envConfig = envProperties.getConfs().get(website);
        if (envConfig != null) {
            return envConfig;
        } else {
            return envProperties.getConfs().get("ozwillo");
        }
    }

    public String extractEnvKey(){
        String URL = extractURL(request.getRequestURL().toString());
        Map<String, EnvConfig> envConfigMap = envProperties.getConfs();

        for(Map.Entry<String,EnvConfig> entry : envConfigMap.entrySet()){
            String key = entry.getKey();
            EnvConfig envConfig = entry.getValue();

            if(envConfig.getBaseUrl().equals(URL)){
                return key;
            }
        }

        return "ozwillo";
    }


    public EnvConfig getCurrentConfig(){
        try {
            return getConfig();
        }catch(Exception e){
            return null;
        }
    }

    private String extractURL(String URL){
        Pattern p = Pattern.compile("((https|http)?:\\/\\/)(.*?)(?=\\/)");
        Matcher m = p.matcher(URL);
        m.find();
        return m.group(0);
    }


}
