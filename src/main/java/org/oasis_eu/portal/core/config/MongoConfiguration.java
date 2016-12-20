package org.oasis_eu.portal.core.config;

import com.google.common.base.Strings;
import com.mongodb.*;
import org.oasis_eu.portal.core.mongo.MongoPackage;
import org.oasis_eu.portal.core.mongo.model.images.ImageDownloadAttempt;
import org.oasis_eu.portal.core.mongo.model.my.UserSubscription;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        int acceptableLatencyDifference = 150;
        int connectTimeout = 300;

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

        public void setAcceptableLatencyDifference(int acceptableLatencyDifference) {
            this.acceptableLatencyDifference = acceptableLatencyDifference;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }
    }

    @Autowired
    private PersistenceProperties persistenceProperties;

    @Value("${application.imageBlacklistTtl:900}")
    private int imageBlacklistTtl;
    @Value("${application.installedStatusTtl:86400}")
    private int installedStatusTtl;

    @Value("${application.catalogCacheTtl:600}")
    private int catalogCacheTtl;

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

        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        builder.localThreshold(persistenceProperties.acceptableLatencyDifference);
        builder.connectTimeout(persistenceProperties.connectTimeout);

        if (serverAddresses.size() > 1) {
            builder.writeConcern(WriteConcern.MAJORITY);
        } else {
            builder.writeConcern(WriteConcern.JOURNALED);
        }

        MongoClientOptions options = builder.build();

        if (serverAddresses.size() > 1) {
            if (!Strings.isNullOrEmpty(persistenceProperties.login)) {

                client = new MongoClient(serverAddresses,
                    Arrays.asList(MongoCredential.createMongoCRCredential(persistenceProperties.login, persistenceProperties.databaseName, persistenceProperties.password.toCharArray())),
                    options);
            } else {
                client = new MongoClient(serverAddresses, options);
            }


            client.setWriteConcern(WriteConcern.MAJORITY);
            client.setReadPreference(ReadPreference.primaryPreferred());
        } else {
            if (!Strings.isNullOrEmpty(persistenceProperties.login)) {
                client = new MongoClient(serverAddresses.get(0),
                    Arrays.asList(MongoCredential.createMongoCRCredential(persistenceProperties.login, persistenceProperties.databaseName, persistenceProperties.password.toCharArray())),
                    options);
            } else {
                client = new MongoClient(serverAddresses.get(0), options);
            }

            client.setWriteConcern(WriteConcern.JOURNALED);
        }
        return client;
    }

    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = super.mongoTemplate();
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);

        ensureTtlIndex(mongoTemplate, ImageDownloadAttempt.class, "time", imageBlacklistTtl);
        ensureTtlIndex(mongoTemplate, InstalledStatus.class, "computed", installedStatusTtl);

        return mongoTemplate;
    }


    private void ensureTtlIndex(MongoTemplate mongoTemplate, Class<?> objectClass, String ttlFieldName, int ttlSeconds) {
        IndexInfo index = mongoTemplate.indexOps(objectClass).getIndexInfo().stream().filter(indexInfo -> indexInfo.getName().equals(ttlFieldName)).findFirst().orElse(null);
        if (index == null) {
            mongoTemplate.indexOps(objectClass).ensureIndex(new Index().on(ttlFieldName, Sort.Direction.ASC).named(ttlFieldName).expire(ttlSeconds, TimeUnit.SECONDS));
        } else {
            logger.info("Index on {}.{} already exists", objectClass.getName(), ttlFieldName);
        }
    }

    @Override
    public CustomConversions customConversions() {

        Converter<String, UserSubscription> converter = new Converter<String, UserSubscription>() {
            @Override
            public UserSubscription convert(String source) {
                UserSubscription us = new UserSubscription();
                us.setId(source);
                return us;
            }
        };

        return new CustomConversions(Arrays.asList(converter));
    }
}
