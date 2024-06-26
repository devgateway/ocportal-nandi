package org.devgateway.toolkit.persistence.mongo.spring;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * Created by mpostelnicu on 6/12/17.
 */
@Configuration
@Profile("!integration")
public class MongoTemplateConfig {

    public static final String SHADOW_POSTFIX = "-shadow";

    @Autowired
    private MongoProperties properties;

    @Autowired
    private MongoCustomConversions customConversions;

    @Bean(autowire = Autowire.BY_NAME, name = "mongoTemplate")
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate template = new MongoTemplate(new SimpleMongoClientDbFactory(properties.getUri()));
        ((MappingMongoConverter) template.getConverter()).setCustomConversions(customConversions);

        // CALL THIS MANULLY, so that all the default converters will be registered!
        ((MappingMongoConverter) template.getConverter()).afterPropertiesSet();

        return template;
    }

//    /**
//     * Creates a shadow template configuration by adding "-shadow" as postfix of database name.
//     * This is used to replicate the entire database structure in a shadow/temporary database location
//     *
//     * @return
//     * @throws Exception
//     */
//    @Bean(autowire = Autowire.BY_NAME, name = "shadowMongoTemplate")
//    public MongoTemplate shadowMongoTemplate() throws Exception {
//        MongoTemplate template = new
//                MongoTemplate(new SimpleMongoDbFactory(new MongoClientURI(properties.getUri() + SHADOW_POSTFIX)));
//        ((MappingMongoConverter) template.getConverter()).setCustomConversions(customConversions);
//        return template;
//    }

}
