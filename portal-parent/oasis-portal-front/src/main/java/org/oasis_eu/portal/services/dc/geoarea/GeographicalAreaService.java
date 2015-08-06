package org.oasis_eu.portal.services.dc.geoarea;

import org.oasis_eu.portal.core.mongo.dao.geo.GeographicalAreaCache;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

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
     * @return
     */
    public List<GeographicalArea> find(String country_uri, String q, int start, int limit) {
        return cache.search(country_uri, RequestContextUtils.getLocale(request).getLanguage(), q, start, limit)
                    .collect(Collectors.toList());
    }
    public GeographicalArea getAncestorsFromGeographicalArea(List<String> geographicalAreas){
        if(geographicalAreas == null || geographicalAreas.isEmpty()){return null;}

        List<GeographicalArea> geoList = this.find(null, geographicalAreas.get(0), 0,1);
        if (geoList != null && !geoList.isEmpty()){
            return geoList.get(0);
        }
        return null;
    }
    
    /**
     * @param q
     * @param start
     * @param limit ex. 11 then return 10
     * @return
     */
    public List<GeographicalArea> findCities(String queryTerms, String country, int start, int limit) {
        queryTerms = queryTerms.substring(0,1).toUpperCase() + queryTerms.substring(1,queryTerms.length()).toLowerCase();
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

}
