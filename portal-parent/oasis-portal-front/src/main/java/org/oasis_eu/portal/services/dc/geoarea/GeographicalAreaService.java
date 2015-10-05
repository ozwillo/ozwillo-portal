package org.oasis_eu.portal.services.dc.geoarea;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.mongo.dao.geo.GeographicalAreaCache;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.services.dc.organization.DCRegActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

@Service
public class GeographicalAreaService {
    
    @Value("${application.geoarea.fallbackLanguage:en}")
    private String fallbackLanguage = "en";

    @Autowired
    private GeographicalAreaCache cache;

    @Autowired
    private GeographicalDAO geographicalDAO;

    /** to get the current locale */
    @Autowired
    private HttpServletRequest request;

    /**
     * TODO implement on top of locally cached / sync'd keywords to id map
     * rather than directly on DC 
     * @param q
     * @param start
     * @param limit ex. 11 then return 10 and loadMore=true
     * @param modelType is the type in DC model required, null if any
     * @return
     */
    public List<GeographicalArea> find(String country_uri, String modelType, String q, int start, int limit) {
        return cache.search(country_uri, modelType,RequestContextUtils.getLocale(request).getLanguage(), q, start, limit)
                    .collect(Collectors.toList());
    }

    /**
     * @param q
     * @param start
     * @param limit ex. 11 then return 10
     * @return
     */
    public List<GeographicalArea> findCities(String queryTerms, String country, int start, int limit) {
        return geographicalDAO.searchCities(RequestContextUtils.getLocale(request).getLanguage(), queryTerms, country, start, limit);
    }

    /**
     * @param q
     * @param start
     * @param limit ex. 11 then return 10
     * @return
     */
    public List<GeographicalArea> findCountries(String queryTerms, int start, int limit) {
        queryTerms = queryTerms.substring(0,1).toUpperCase() + queryTerms.substring(1,queryTerms.length()).toLowerCase();
        return geographicalDAO.searchCountries(RequestContextUtils.getLocale(request).getLanguage(), queryTerms, start, limit);
    }


    /** Search an RegActivity in DC and Kernel to validate its modification */
    public List<DCRegActivity> findTaxRegActivity(String term, String country_uri){
        List<DCRegActivity> dcRegActivity = geographicalDAO.searchTaxRegActivity(country_uri, term, 0, 10);

        return dcRegActivity;
    }

}
