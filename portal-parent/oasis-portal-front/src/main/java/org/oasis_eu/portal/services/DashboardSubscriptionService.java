package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.mongo.dao.my.DashboardOrderingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Service
public class DashboardSubscriptionService {

    @Autowired
    private DashboardOrderingRepository orderingRepository;



}
