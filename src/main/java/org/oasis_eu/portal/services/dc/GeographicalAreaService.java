package org.oasis_eu.portal.services.dc;

import org.oasis_eu.portal.dao.GeographicalAreaCache;
import org.oasis_eu.portal.model.geo.GeographicalArea;
import org.oasis_eu.portal.model.search.Tokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeographicalAreaService {

    @Value("${application.geoarea.fallbackLanguage:en}")
    private String fallbackLanguage = "en";

    @Value("${application.geoarea.countryModel:geoco:Country_0}")
    private String countryModel;
    @Value("${application.geoarea.countryModelHier:geohier:Hierarchical_0}")
    private String countryModelHier;
    @Value("${application.geoarea.cityModel:geoci:City_0}")
    private String cityModel;

    @Autowired
    private GeographicalAreaCache cache;

    @Autowired
    private Tokenizer tokenizer;

    /**
     * to get the current locale
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * TODO implement on top of locally cached / sync'd keywords to id map
     * rather than directly on DC
     *
     * @param q
     * @param start
     * @param limit     ex. 11 then return 10 and loadMore=true
     * @param modelType is the type in DC model required, null if any
     * @return
     */
    public List<GeographicalArea> find(String country_uri, String modelType, String q, int start, int limit) {
        return cache.search(country_uri, modelType, RequestContextUtils.getLocale(request).getLanguage(), q, start, limit)
            .collect(Collectors.toList());
    }

    /**
     * @param queryTerms
     * @param start
     * @param limit ex. 11 then return 10
     * @return
     */
    public List<GeographicalArea> findCities(String queryTerms, String country_uri, int start, int limit) {
        //return geographicalDAO.searchCities(RequestContextUtils.getLocale(request).getLanguage(), queryTerms, country, start, limit);
        return cache.search(country_uri, cityModel, RequestContextUtils.getLocale(request).getLanguage(), queryTerms, start, limit)
            .collect(Collectors.toList());
    }

    public List<GeographicalArea> findCountries(String q) {
        String[] queryTerms = null;
        if (q != null && !q.isEmpty()) {
            List<String> termsLst = new ArrayList<>(tokenizer.tokenize(q));
            queryTerms = termsLst.toArray(new String[0]);
        }
        return cache.findOneToken(null, new String[] { countryModel, countryModelHier },
                RequestContextUtils.getLocale(request).getLanguage(), queryTerms)
            .collect(Collectors.toList());
    }
}
