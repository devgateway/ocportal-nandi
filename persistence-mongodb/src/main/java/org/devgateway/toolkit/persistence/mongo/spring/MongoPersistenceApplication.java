/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.toolkit.persistence.mongo.spring;

import com.mongodb.DBObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.MONGO_DECIMAL_SCALE;

/**
 * Run this application only when you need access to Spring Data JPA but without
 * Wicket frontend
 *
 * @author mpostelnicu
 */
@SpringBootApplication
@ComponentScan("org.devgateway")
@PropertySource("classpath:/org/devgateway/toolkit/persistence/mongo/application.properties")
@EnableCaching
public class MongoPersistenceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MongoPersistenceApplication.class, args);
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays
                .asList(BigDecimalToDoubleConverter.INSTANCE, DoubleToBigDecimalConverter.INSTANCE,
                        DbObjectToGeoJsonPointConverter.INSTANCE,
                        ZonedDateTimeReadConverter.INSTANCE, ZonedDateTimeWriteConverter.INSTANCE,
                        URIToStringConverter.INSTANCE));
    }

    public enum URIToStringConverter implements Converter<URI, String> {
        INSTANCE;

        @Override
        public String convert(final URI source) {
            return source == null ? null : source.toString();
        }
    }

    @ReadingConverter
    public enum ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {
        INSTANCE;

        @Override
        public ZonedDateTime convert(Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }

    @WritingConverter
    public enum ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(ZonedDateTime zonedDateTime) {
            return Date.from(zonedDateTime.toInstant());
        }
    }


    public enum BigDecimalToDoubleConverter implements Converter<BigDecimal, Double> {
        INSTANCE;

        @Override
        public Double convert(final BigDecimal source) {
            return source == null ? null : source.doubleValue();
        }
    }

    public enum DoubleToBigDecimalConverter implements Converter<Double, BigDecimal> {
        INSTANCE;

        @Override
        public BigDecimal convert(final Double source) {
            return source != null ? new BigDecimal(source).setScale(MONGO_DECIMAL_SCALE, RoundingMode.HALF_UP) : null;
        }
    }

    public enum DbObjectToGeoJsonPointConverter implements Converter<DBObject, GeoJsonPoint> {

        INSTANCE;

        /*
         * (non-Javadoc)
         * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
         */
        @Override
        @SuppressWarnings("unchecked")
        public GeoJsonPoint convert(DBObject source) {

            if (source == null) {
                return null;
            }

            if (source.get("type") == null) {
                return null;
            }

            Assert.isTrue(ObjectUtils.nullSafeEquals(source.get("type"), "Point"),
                    String.format("Cannot convert type '%s' to Point.", source.get("type")));

            List<Number> dbl = (List<Number>) source.get("coordinates");
            return new GeoJsonPoint(dbl.get(0).doubleValue(), dbl.get(1).doubleValue());
        }
    }
}