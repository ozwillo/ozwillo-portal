package org.oasis_eu.portal.services;

import org.oasis_eu.portal.dao.UserOrganizationsHistoryRepository;
import org.oasis_eu.portal.model.authority.UIOrganization;
import org.oasis_eu.portal.model.store.OrganizationHistory;
import org.oasis_eu.portal.model.store.UserOrganizationsHistory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        UserOrganizationsHistory userOrganizationsHistory = new UserOrganizationsHistory();
        if (userOrganizationsHistories.isPresent()) {
            userOrganizationsHistory = userOrganizationsHistories.get();
            return userOrganizationsHistory.getOrganizationsHistory();
        }else{
            return null;
        }

    }

    public List<OrganizationHistory> addLastVisited(String organizationId) {
        try {
            UIOrganization uiOrganization = organizationService.getOrganizationFromKernel(organizationId);
            String userId = userInfoService.currentUser().getUserId();
            UserOrganizationsHistory userOrganizationsHistories = new UserOrganizationsHistory();
            Optional<UserOrganizationsHistory> optionalUserOrganizationsHistory = userOrganizationsHistoryRepository.findById(userId);
            if (optionalUserOrganizationsHistory.isPresent()) {
                userOrganizationsHistories = optionalUserOrganizationsHistory.get();
            } else {
                userOrganizationsHistories.setUserId(userId);
            }

            List<OrganizationHistory> organizationHistories = userOrganizationsHistories.getOrganizationsHistory();
            Collections.sort(organizationHistories);
            OrganizationHistory newOrganizationHistory = new OrganizationHistory();

            //check this line of history exist and update the date
            for (OrganizationHistory organizationHistory : organizationHistories) {
                if (organizationHistory.getOrganizationId().equals(organizationId)) {
                    organizationHistory.setDate(new Date());
                    userOrganizationsHistoryRepository.save(userOrganizationsHistories);
                    return organizationHistories;
                }
            }

            newOrganizationHistory.setOrganizationId(organizationId);
            newOrganizationHistory.setDate(new Date());
            newOrganizationHistory.setName(uiOrganization.getName());

            //list is smaller than 10 so we add a new line of history
            if (organizationHistories.size() < MAX_ORGANIZATION_HISTORY) {
                organizationHistories.add(newOrganizationHistory);
                //list is full == 10 so we set the oldest one  by this new one
            } else {
                organizationHistories.set(0, newOrganizationHistory);
            }
            userOrganizationsHistoryRepository.save(userOrganizationsHistories);
            return organizationHistories;
        }catch(Exception e){
            return null;
        }


    }


}
