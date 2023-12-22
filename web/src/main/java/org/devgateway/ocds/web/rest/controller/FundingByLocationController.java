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
package org.devgateway.ocds.web.rest.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.swagger.v3.oas.annotations.Operation;
import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.web.rest.controller.request.YearFilterPagingRequest;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomProjectionOperation;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.facet;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 *
 * @author mpostelnicu
 *
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class FundingByLocationController extends GenericOCDSController {

    public static final class Keys {
        public static final String ITEMS_DELIVERY_LOCATION = "deliveryLocation";
        public static final String TOTAL_TENDERS_AMOUNT = "totalTendersAmount";
        public static final String TOTAL_PROJECTS_AMOUNT = "totalProjectsAmount";
        public static final String TENDERS_COUNT = "tendersCount";
        public static final String PROJECTS_COUNT = "projectsCount";
        public static final String TOTAL_TENDERS_WITH_START_DATE_AND_LOCATION = "totalTendersWithStartDateAndLocation";
        public static final String TOTAL_TENDERS_WITH_START_DATE = "totalTendersWithStartDate";
        public static final String PERCENT_TENDERS_WITH_START_DATE_AND_LOCATION =
                "percentTendersWithStartDateAndLocation";
        public static final String YEAR = "year";
    }

    @Operation(summary = "Total estimated funding (tender.value) grouped by "
            + "tender.items.deliveryLocation and also grouped by year."
            + " The endpoint also returns the count of tenders for each location. "
            + "It responds to all filters. The year is calculated based on tender.tenderPeriod.startDate")
    @RequestMapping(value = "/api/fundingByTenderDeliveryLocation", method = { RequestMethod.POST,
            RequestMethod.GET }, produces = "application/json")
    public List<Document> fundingByTenderDeliveryLocation(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {

        DBObject project = new BasicDBObject();
        project.put("tender.items.deliveryLocation", 1);
        project.put(MongoConstants.FieldNames.TENDER_VALUE_AMOUNT, 1);
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));

        Aggregation agg = newAggregation(
                match(where("tender").exists(true).and(getTenderDateField()).exists(true)
                        .andOperator(getYearDefaultFilterCriteria(
                                filter, getTenderDateField()
                        ))),
                new CustomProjectionOperation(project), unwind("$tender.items"),
                unwind("$tender.items.deliveryLocation"),
                project(getYearlyMonthlyGroupingFields(filter)).and(MongoConstants.FieldNames.TENDER_VALUE_AMOUNT)
                        .as("tenderAmount")
                        .and("tender.items.deliveryLocation").as("deliveryLocation"),
                match(where("deliveryLocation.geometry.coordinates.0").exists(true)),
                group(getYearlyMonthlyGroupingFields(filter, Keys.ITEMS_DELIVERY_LOCATION))
                        .sum("tenderAmount").as(Keys.TOTAL_TENDERS_AMOUNT).count().as(Keys.TENDERS_COUNT),
                getSortByYearMonth(filter)
                // ,skip(filter.getSkip()), limit(filter.getPageSize())
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "Tenders by tender location at the tender level. The location can be at the ward or at "
            + "the subcounty levels. This location is attached directly to tender, unlike the OCDS extension, which "
            + "ties it to the items.")
    @RequestMapping(value = "/api/fundingByTenderLocation", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> fundingByTenderLocation(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {

        DBObject project = new BasicDBObject();
        project.put(MongoConstants.FieldNames.TENDER_LOCATIONS, 1);
        project.put(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID, 1);
        project.put(MongoConstants.FieldNames.PLANNING_BUDGET_AMOUNT, 1);
        project.put(MongoConstants.FieldNames.TENDER_VALUE_AMOUNT, 1);
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));

        Criteria filterCriteria = getTenderMapFilterCriteria(filter);

        Aggregation agg = newAggregation(
                match(where("tender").exists(true)
                        .and(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID).exists(true)
                        .and(getTenderDateField()).exists(true)
                        .andOperator(filterCriteria)),
                unwind(ref(MongoConstants.FieldNames.TENDER_LOCATIONS)),
                match(filterCriteria),
                new CustomProjectionOperation(project),
                facet(
                        project().and(
                                MongoConstants.FieldNames.TENDER_VALUE_AMOUNT)
                                .as("tenderAmount")
                                .and(MongoConstants.FieldNames.TENDER_LOCATIONS).as("location"),
                        match(where("location.geometry.coordinates.0").exists(true)),
                        group("location")
                                .sum("tenderAmount").as(Keys.TOTAL_TENDERS_AMOUNT).count().as(Keys.TENDERS_COUNT)
                ).as("tenderFacet").and(
                        project().and(
                                MongoConstants.FieldNames.PLANNING_BUDGET_AMOUNT)
                                .as("projectAmount").and(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID)
                                .as("projectID")
                                .and(MongoConstants.FieldNames.TENDER_LOCATIONS).as("location"),
                        match(where("location.geometry.coordinates.0").exists(true).and("projectID").exists(true)),
                        group("location", "projectID").first("projectAmount")
                                .as(Keys.TOTAL_PROJECTS_AMOUNT),
                        group("location").sum(Keys.TOTAL_PROJECTS_AMOUNT).as(Keys.TOTAL_PROJECTS_AMOUNT)
                                .count().as(Keys.PROJECTS_COUNT)

                ).as("projectFacet"),
                project().and("tenderFacet").concatArrays("projectFacet").as("res"),
                unwind("res"),
                group("res._id")
                        .first("res.totalTendersAmount").as(Keys.TOTAL_TENDERS_AMOUNT)
                        .first("res.tendersCount").as(Keys.TENDERS_COUNT)
                        .last("res.totalProjectsAmount").as(Keys.TOTAL_PROJECTS_AMOUNT)
                        .last("res.projectsCount").as(Keys.PROJECTS_COUNT)

        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Calculates percentage of releases with tender with at least one specified delivery location,"
            + " that is the array tender.items.deliveryLocation has to have items."
            + "Filters out stub tenders, therefore tender.tenderPeriod.startDate has to exist.")
    @RequestMapping(value = "/api/qualityFundingByTenderDeliveryLocation", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> qualityFundingByTenderDeliveryLocation(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {

        DBObject project = new BasicDBObject();
        project.putAll(filterProjectMap);
        project.put(Fields.UNDERSCORE_ID, "$tender._id");
        project.put("tenderItemsDeliveryLocation", new BasicDBObject("$cond",
                Arrays.asList(new BasicDBObject("$gt",
                        Arrays.asList("$tender.items.deliveryLocation", null)), 1, 0)));


        DBObject project1 = new BasicDBObject();
        project1.put(Fields.UNDERSCORE_ID, 0);
        project1.put(getTenderDateField(), 1);
        project1.put(Keys.TOTAL_TENDERS_WITH_START_DATE_AND_LOCATION, 1);
        project1.put(Keys.PERCENT_TENDERS_WITH_START_DATE_AND_LOCATION,
                new BasicDBObject("$multiply",
                        Arrays.asList(new BasicDBObject("$divide",
                                        Arrays.asList(
                                                "$totalTendersWithStartDateAndLocation", "$totalTendersWithStartDate")),
                                100)));

        Aggregation agg = newAggregation(
                match(where(getTenderDateField()).exists(true)
                        .andOperator(getYearDefaultFilterCriteria(filter, getTenderDateField()))),
                unwind("$tender.items"), new CustomProjectionOperation(project),
                group(Fields.UNDERSCORE_ID_REF).max("tenderItemsDeliveryLocation").as("hasTenderItemsDeliverLocation"),
                group().count().as("totalTendersWithStartDate").sum("hasTenderItemsDeliverLocation")
                        .as(Keys.TOTAL_TENDERS_WITH_START_DATE_AND_LOCATION),
                new CustomProjectionOperation(project1)
                // ,skip(filter.getSkip()),limit(filter.getPageSize())
        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Contracted amount & number of contracts per location as specified in contract.")
    @RequestMapping(value = "/api/fundingByContractLocation", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> fundingByContractLocation(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {

        Criteria filterCriteria = getContractMapFilterCriteria(filter);

        Aggregation agg = newAggregation(
                match(where(getTenderDateField()).exists(true)
                        .andOperator(filterCriteria)),
                unwind(ref(MongoConstants.FieldNames.CONTRACTS)),
                unwind(ref(MongoConstants.FieldNames.CONTRACTS_LOCATIONS)),
                match(filterCriteria),
                project()
                        .and(MongoConstants.FieldNames.CONTRACTS_VALUE_AMOUNT).as("amount")
                        .and(MongoConstants.FieldNames.CONTRACTS_LOCATIONS).as("location"),
                match(where("location.geometry.coordinates.0").exists(true)),
                group("location")
                        .sum("amount").as("contractsAmount")
                        .count().as("contractsCount"),
                Aggregation.replaceRoot()
                        .withValueOf(ObjectOperators.MergeObjects.merge(
                                new Document("contractsAmount", "$contractsAmount"),
                                new Document("contractsCount", "$contractsCount")
                        ).mergeWithValuesOf(Fields.UNDERSCORE_ID)));

        return releaseAgg(agg);
    }
}