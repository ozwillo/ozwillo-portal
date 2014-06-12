package org.oasis_eu.portal.core.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import org.oasis_eu.portal.core.mongo.MongoPackage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
@EnableMongoRepositories(basePackageClasses = MongoPackage.class)
public class MongoConfiguration extends AbstractMongoConfiguration {

    @Value("${persistence.mongodatabase}")
    private String databaseName;

    @Value("${persistence.mongohost}")
    private String databaseHost;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.oasis_eu.portal.core.mongo";
    }

    @Override
    public Mongo mongo() throws Exception {
        MongoClient client = new MongoClient(databaseHost);

        client.setWriteConcern(WriteConcern.JOURNALED);
        client.setReadPreference(ReadPreference.primaryPreferred());
        return client;
    }

    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = super.mongoTemplate();
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return mongoTemplate;
    }
}
