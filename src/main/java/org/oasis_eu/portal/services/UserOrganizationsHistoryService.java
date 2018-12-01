package org.oasis_eu.portal.services;

import org.oasis_eu.portal.dao.UserOrganizationsHistoryRepository;
import org.oasis_eu.portal.model.organization.UIOrganization;
import org.oasis_eu.portal.model.history.OrganizationHistory;
import org.oasis_eu.portal.model.history.UserOrganizationsHistory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;

@Service
public class UserOrganizationsHistoryService {

    @Autowired
    private UserOrganizationsHistoryRepository userOrganizationsHistoryRepository;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private OrganizationService organizationService;

    private Integer MAX_ORGANIZATION_HISTORY = 10;

    public List<OrganizationHistory> getLastVistited() {
        String userId = userInfoService.currentUser().getUserId();
        Optional<UserOrganizationsHistory> userOrganizationsHistories = userOrganizationsHistoryRepository.findById(userId);

        return userOrganizationsHistories
                .map(UserOrganizationsHistory::getOrganizationsHistory)
                .orElse(new ArrayList<>());
    }

    public List<OrganizationHistory> addLastVisited(String organizationId){
            UIOrganization uiOrganization = organizationService.getOrganizationFromKernel(organizationId);
            String userId = userInfoService.currentUser().getUserId();
            UserOrganizationsHistory userOrganizationsHistories  = userOrganizationsHistoryRepository
                    .findById(userId).orElse(new UserOrganizationsHistory(userId));

            List<OrganizationHistory> organizationHistories = userOrganizationsHistories.getOrganizationsHistory();
            Collections.sort(organizationHistories);

            //check this line of history exist and update the date
            for (OrganizationHistory organizationHistory : organizationHistories) {
                if (organizationHistory.getDcOrganizationId().equals(organizationId)) {
                    organizationHistory.setDate(new Date());
                    userOrganizationsHistoryRepository.save(userOrganizationsHistories);
                    return organizationHistories;
                }
            }

            organizationHistories = updateOrganizationHistories(organizationId, uiOrganization.getName(), organizationHistories);
            userOrganizationsHistoryRepository.save(userOrganizationsHistories);
            return organizationHistories;
    }

    public List<OrganizationHistory> deleteOrganizationHistory(String organizationId){
        String userId = userInfoService.currentUser().getUserId();
        UserOrganizationsHistory userOrganizationsHistories  = userOrganizationsHistoryRepository
                .findById(userId).orElse(new UserOrganizationsHistory(userId));
        List<OrganizationHistory> organizationHistories = userOrganizationsHistories.getOrganizationsHistory();

        organizationHistories.remove(new OrganizationHistory(organizationId));
        userOrganizationsHistoryRepository.save(userOrganizationsHistories);
        return organizationHistories;
    }

    private List<OrganizationHistory> updateOrganizationHistories(String organizationId, String name, List<OrganizationHistory> organizationHistories){
        OrganizationHistory newOrganizationHistory = new OrganizationHistory(organizationId, name, new Date());
        //list is smaller than MAX_ORGANIZATION_HISTORY so we add a new line of history
        if (organizationHistories.size() < MAX_ORGANIZATION_HISTORY) {
            organizationHistories.add(newOrganizationHistory);
            //list is full == MAX_ORGANIZATION_HISTORY so we set the oldest one  by this new one
        } else {
            organizationHistories.set(0, newOrganizationHistory);
        }

        return organizationHistories;
    }


}
