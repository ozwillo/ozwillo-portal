package org.oasis_eu.portal.core.config;

import com.google.common.base.Strings;
import com.mongodb.*;
import org.oasis_eu.portal.core.mongo.MongoPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
@EnableConfigurationProperties(MongoConfiguration.PersistenceProperties.class)
@EnableMongoRepositories(basePackageClasses = MongoPackage.class)
public class MongoConfiguration extends AbstractMongoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfiguration.class);


    @ConfigurationProperties(prefix = "persistence")
    public static class PersistenceProperties {
        String databaseName;
        List<String> databaseHosts;
        String login;
        String password;

        public void setMongodatabase(String databaseName) {
            this.databaseName = databaseName;
        }

        public void setMongohosts(List<String> databaseHosts) {
            this.databaseHosts = databaseHosts;
        }

        public List<String> getMongohosts() {
            return databaseHosts;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Autowired
    private PersistenceProperties persistenceProperties;


//    @Value("${persistence.mongodatabase}")
//    private String databaseName;
//
//    @Value("${persistence.mongohost}")
//    private Collection<String> databaseHosts;
//
//    @Value("${persistence.login:}")
//    private String login;
//
//    @Value("${persistence.password:}")
//    private String password;

    @Override
    protected String getDatabaseName() {
        return persistenceProperties.databaseName;
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.oasis_eu.portal.core.mongo";
    }

    @Bean
    @Override
    public Mongo mongo() throws Exception {
        List<ServerAddress> serverAddresses = persistenceProperties.databaseHosts.stream()
                .map(hostname -> {
                    try {
                        return new ServerAddress(hostname);
                    } catch (UnknownHostException e) {
                        logger.error("Cannot resolve host {}", hostname);
                        return null;
                    }
                })
                .filter(s -> s != null)
                .collect(Collectors.toList());

        if (serverAddresses.size() == 0) {
            logger.error("No valid Mongo hosts, aborting startup");
            throw new IllegalStateException("No valid Mongo hosts, aborting startup");
        }

        MongoClient client;


        if (serverAddresses.size() > 1) {
            if (!Strings.isNullOrEmpty(persistenceProperties.login)) {

                client = new MongoClient(serverAddresses, Arrays.asList(MongoCredential.createMongoCRCredential(persistenceProperties.login, persistenceProperties.databaseName, persistenceProperties.password.toCharArray())));
            } else {
                client = new MongoClient(serverAddresses);
            }


            client.setWriteConcern(WriteConcern.MAJORITY);
            client.setReadPreference(ReadPreference.primaryPreferred());
        } else {
            if (!Strings.isNullOrEmpty(persistenceProperties.login)) {
                client = new MongoClient(serverAddresses.get(0), Arrays.asList(MongoCredential.createMongoCRCredential(persistenceProperties.login, persistenceProperties.databaseName, persistenceProperties.password.toCharArray())));
            } else {
                client = new MongoClient(serverAddresses.get(0));
            }

            client.setWriteConcern(WriteConcern.JOURNALED);
        }
        return client;
    }

    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = super.mongoTemplate();
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return mongoTemplate;
    }
}
