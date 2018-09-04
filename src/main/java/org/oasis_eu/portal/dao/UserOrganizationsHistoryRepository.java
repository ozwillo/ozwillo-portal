package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.store.UserOrganizationsHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserOrganizationsHistoryRepository extends MongoRepository<UserOrganizationsHistory, String> {
}
