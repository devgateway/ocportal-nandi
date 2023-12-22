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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author mpostelnicu
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class ProjectStatisticsController extends GenericOCDSController {


    public static final class Keys {
        public static final String YEAR = "year";
        public static final String COUNT = "count";
        public static final String AMOUNT = "amount";
    }


    @Operation(summary = "Calculates number of projects per year")
    @RequestMapping(value = "/api/numberOfProjectsByYear", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> numberOfProjectsByYear(@ModelAttribute @Valid final YearFilterPagingRequest filter) {

        DBObject project = new BasicDBObject();
        project.put("projectID", ref(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID));
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));

        Criteria filterCriteria = getTenderMapFilterCriteria(filter);

        Aggregation agg = newAggregation(
                match(where(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID).exists(true)
                        .and(getTenderDateField()).exists(true)
                        .andOperator(filterCriteria)),
                new CustomProjectionOperation(project),
                group(getYearlyMonthlyGroupingFields(filter, "projectID")),
                group(getYearlyMonthlyGroupingFields(filter)).count().as(Keys.COUNT),
                transformYearlyGrouping(filter).andInclude(Keys.COUNT),
                getSortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Calculates amount of project budgeted by year")
    @RequestMapping(value = "/api/amountBudgetedByYear", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> amountBudgetedByYear(@ModelAttribute @Valid final YearFilterPagingRequest filter) {


        DBObject project = new BasicDBObject();
        project.put("projectID", ref(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID));
        project.put(Keys.AMOUNT, ref(MongoConstants.FieldNames.PLANNING_BUDGET_AMOUNT));
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));

        Criteria filterCriteria = getTenderMapFilterCriteria(filter);

        Aggregation agg = newAggregation(
                match(where(MongoConstants.FieldNames.PLANNING_BUDGET_PROJECT_ID).exists(true)
                        .and(getTenderDateField()).exists(true)
                        .and(MongoConstants.FieldNames.PLANNING_BUDGET_AMOUNT).exists(true)
                        .andOperator(filterCriteria)),
                new CustomProjectionOperation(project),
                group(getYearlyMonthlyGroupingFields(filter, "projectID"))
                        .first(Keys.AMOUNT).as(Keys.AMOUNT),
                group(getYearlyMonthlyGroupingFields(filter)).sum(Keys.AMOUNT).as(Keys.AMOUNT),
                transformYearlyGrouping(filter).andInclude(Keys.AMOUNT),
                getSortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "Calculates number of contracts per year")
    @RequestMapping(value = "/api/numberOfContractsByYear", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> numberOfContractsByYear(@ModelAttribute @Valid final YearFilterPagingRequest filter) {

        DBObject project = new BasicDBObject();
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));

        Criteria filterCriteria = getContractMapFilterCriteria(filter);

        Aggregation agg = newAggregation(
                match(where(getTenderDateField()).exists(true)
                        .andOperator(filterCriteria)),
                unwind(MongoConstants.FieldNames.CONTRACTS),
                new CustomProjectionOperation(project),
                group(getYearlyMonthlyGroupingFields(filter)).count().as(Keys.COUNT),
                transformYearlyGrouping(filter).andInclude(Keys.COUNT),
                getSortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "Calculates contract amount by year")
    @RequestMapping(value = "/api/amountContractedByYear", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> amountContractedByYear(@ModelAttribute @Valid final YearFilterPagingRequest filter) {

        DBObject project = new BasicDBObject();
        project.put(Keys.AMOUNT, ref(MongoConstants.FieldNames.CONTRACTS_VALUE_AMOUNT));
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));

        Criteria filterCriteria = getContractMapFilterCriteria(filter);

        Aggregation agg = newAggregation(
                match(where(getTenderDateField()).exists(true).andOperator(filterCriteria)),
                unwind(MongoConstants.FieldNames.CONTRACTS),
                new CustomProjectionOperation(project),
                group(getYearlyMonthlyGroupingFields(filter)).sum(Keys.AMOUNT).as(Keys.AMOUNT),
                transformYearlyGrouping(filter).andInclude(Keys.AMOUNT),
                getSortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }
}