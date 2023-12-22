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
import org.devgateway.ocds.persistence.mongo.Tender;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.web.rest.controller.request.YearFilterPagingRequest;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomOperation;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 *
 * @author mpostelnicu
 *
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class TotalCancelledTendersByYearController extends GenericOCDSController {

    public static final class Keys {
        public static final String TOTAL_CANCELLED_TENDERS_AMOUNT = "totalCancelledTendersAmount";
        public static final String YEAR = "year";
    }

    @Operation(summary = "Total Cancelled tenders by year. The tender amount is read from tender.value."
            + "The tender status has to be 'cancelled'. The year is retrieved from tender.tenderPeriod.startDate.")
    @RequestMapping(value = "/api/totalCancelledTendersByYear", method = { RequestMethod.POST, RequestMethod.GET },
            produces = "application/json")
    public List<Document> totalCancelledTendersByYear(@ModelAttribute @Valid final YearFilterPagingRequest filter) {

        clearTenderStatus(filter); //this endpoint is about tender status, so we clear any previously set tender status
        filter.getTenderStatus().add(Tender.Status.cancelled.toString());

        DBObject project = new BasicDBObject();
        addYearlyMonthlyProjection(filter, project, ref(getTenderDateField()));
        project.put(MongoConstants.FieldNames.TENDER_VALUE_AMOUNT, 1);

        Aggregation agg = newAggregation(
                match(where(getTenderDateField()).exists(true)
                        .andOperator(getYearDefaultFilterCriteria(filter,
                                getTenderDateField()
                        ))),
                new CustomOperation(new Document("$project", project)),
                getYearlyMonthlyGroupingOperation(filter).
                sum(ref(MongoConstants.FieldNames.TENDER_VALUE_AMOUNT)).as(Keys.TOTAL_CANCELLED_TENDERS_AMOUNT),
                transformYearlyGrouping(filter).andInclude(Keys.TOTAL_CANCELLED_TENDERS_AMOUNT),
                getSortByYearMonth(filter));

       return releaseAgg(agg);
    }

}