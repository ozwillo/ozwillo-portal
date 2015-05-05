package org.oasis_eu.portal.services.geoarea;

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
    public List<GeographicalArea> find(String q, int start, int limit) {

        return cache.search(RequestContextUtils.getLocale(request).getLanguage(), q, start, limit)
                .collect(Collectors.toList());

    }

}
