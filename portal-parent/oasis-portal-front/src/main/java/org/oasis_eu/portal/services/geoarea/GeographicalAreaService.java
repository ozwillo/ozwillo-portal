package org.oasis_eu.portal.services.geoarea;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.front.store.GeographicalArea;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

@Service
public class GeographicalAreaService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeographicalAreaService.class);
    
    /** TODO rename prefix ? */
    @Value("${application.geoarea.storageModel:geo:City_0}")
    private String storageModel = "geo:City_0"; // "geo:Area_0"; // "geoci:City_0"
    
    /** TODO LATER rather odisp:name (or field shortcuts) OR RATHER IN CACHE */
    @Value("${application.geoarea.nameField:geo_city:name}")
    private String nameField = "geo_city:name"; // "geoci:name";

    @Value("${application.geoarea.fallbackLanguage:en}")
    private String fallbackLanguage = "en";
    
    @Autowired
    private DatacoreClient datacore;

    /** to get the current locale */
    @Autowired
    private HttpServletRequest request;
    
    /**
     * TODO implement on top of locally cached / sync'd keywords to id map
     * rather than directly on DC 
     * @param q
     * @param preferredLocale used to convert results ONLY IF SUCH LOCALE
     * @param start
     * @param limit ex. 11 then return 10 and loadMore=true
     * @return
     */
    public List<GeographicalArea> find(String q, int start, int limit) {
        q = q.substring(0, 1).toUpperCase() + q.substring(1); // TODO better HACK (using keywords) barcelona => Barcelona
        
        List<DCResource> res = datacore.findResources(storageModel,
                new DCQueryParameters(nameField, DCOperator.REGEX, q + ".*"),
                // , use current i18n
                start, limit);
        String preferredLanguage = RequestContextUtils.getLocale(request).getLanguage();
        return res.stream().map(r -> toGeographicalArea(r, preferredLanguage)).collect(Collectors.toList());
    }
    
    private GeographicalArea toGeographicalArea(DCResource r, String preferredLanguage) {
        GeographicalArea area = new GeographicalArea();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> nameMaps = (List<Map<String, String>>) r.get(nameField);
        String name = null;
        for (Map<String, String> nameMap : nameMaps) {
            String l = nameMap.get("l");
            if (l == null) {
                continue; // shouldn't happen
            }
            if (l.equals(preferredLanguage)) {
                name = nameMap.get("v");
                break; // can't find better
            }
            if (name == null || l.equals(fallbackLanguage)) { // only the first time or if fallback
                name = nameMap.get("v");
            }
        }
        area.setName(name);
        area.setUri(r.getUri());
        //area.setDetailedName(); // TODO fill in Datacore OR RATHER CACHE using names of NUTS3 or else 2 and country
        return area;
    }
            
}
