package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.dao.portal.store.RatingRepository;
import org.oasis_eu.portal.model.store.AvgRating;
import org.oasis_eu.portal.model.store.Rating;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * User: schambon
 * Date: 10/31/14
 */
@Service
public class RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserInfoService userInfoHelper;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void rate(String appType, String appId, double score) {

        if (score < 0 || score > 5) {
            throw new IllegalArgumentException("Scores must be between 0 and 5");
        }

        CatalogEntryType type = CatalogEntryType.of(appType);
        String userId = userInfoHelper.currentUser().getUserId();

        if (!isRateable(type, appId)) {
            logger.warn("User {} is attempting to multiple-rate application {}:{}", userId, type, appId);

            throw new IllegalArgumentException("User already rated this application");
        }

        Rating rating = new Rating();
        rating.setAppId(appId);
        rating.setAppType(type);
        rating.setUserId(userId);
        rating.setRating(score);

        ratingRepository.save(rating);
    }

    public double getRating(String appType, String appId) {
        // mild fun with the mongodb aggregation framework...
        AggregationResults<AvgRating> aggregate = mongoTemplate.aggregate(
            newAggregation(
                match(where("appType").is(CatalogEntryType.of(appType).toString()).and("appId").is(appId)),
                group("appType", "appId").avg("rating").as("rating")
            ), "rating", AvgRating.class);

        AvgRating uniqueMappedResult = aggregate.getUniqueMappedResult();
        if (uniqueMappedResult == null) {
            // there are no ratings for this app...
            return 0;
        }
        float rating = uniqueMappedResult.getRating();

        return Math.round(rating * 2) / 2.;
    }

    public boolean isRateable(CatalogEntryType appType, String appId) {
        if (userInfoHelper.currentUser() == null) {
            return false;
        }
        return ratingRepository.findByAppTypeAndAppIdAndUserId(appType, appId, userInfoHelper.currentUser().getUserId()).size() == 0;
    }
}
