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

import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.Tender;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.web.rest.controller.request.YearFilterPagingRequest;
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
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author mpostelnicu
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class SmallRepetitiveDirectAwardsByBuyer extends GenericOCDSController {

    public static final Double AWARD_THRESHOLD = 30000d;

    @ApiOperation(value = "")
    @RequestMapping(value = "/api/smallRepetitiveDirectAwardsByBuyer",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    public List<Document> smallRepetitiveDirectAwardsByBuyer(@ModelAttribute
                                                             @Valid final YearFilterPagingRequest filter) {
        Aggregation agg = newAggregation(
                match(where(MongoConstants.FieldNames.TENDER_PROC_METHOD).is(Tender.ProcurementMethod.direct.toString())
                        .and(MongoConstants.FieldNames.AWARDS_VALUE).exists(true)
                        .and(MongoConstants.FieldNames.BUYER_NAME).exists(true)
                        .and(MongoConstants.FieldNames.TENDER_ITEMS_CLASSIFICATION).exists(true)
                        .and(MongoConstants.FieldNames.AWARDS_VALUE_AMOUNT).exists(true).lt(AWARD_THRESHOLD)
                        .andOperator(getYearDefaultFilterCriteria(filter, getAwardDateField()))),
                unwind("awards"),
                unwind("awards.suppliers"),
                project(MongoConstants.FieldNames.BUYER_NAME)
        );
        return releaseAgg(agg);
    }
}