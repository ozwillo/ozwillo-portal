package org.oasis_eu.portal.services.geoarea;

import org.oasis_eu.portal.core.mongo.dao.geo.GeographicalAreaCache;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeographicalAreaService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeographicalAreaService.class);
    


    @Value("${application.geoarea.fallbackLanguage:en}")
    private String fallbackLanguage = "en";


//    @Value("${application.geoarea.replication_query_batch_limit:5}")
//    private int queryBatchLimit = 5; // fetch at most five batches for a query - note that queryBatchLimit / batchSize MUST be lower than the thresholds used in the data core
    

    @Autowired
    GeographicalAreaCache cache;

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

//
//        q = q.substring(0, 1).toUpperCase() + q.substring(1); // TODO better HACK (using keywords) barcelona => Barcelona
//
//        List<DCResource> res = datacore.findResources(storageModel,
//                new DCQueryParameters(nameField, DCOperator.REGEX, q + ".*"),
//                // , use current i18n
//                start, limit);
//        String preferredLanguage = RequestContextUtils.getLocale(request).getLanguage();
//        return res.stream().map(r -> toGeographicalArea(r, preferredLanguage)).filter(a -> a != null).collect(Collectors.toList());
    }

}
