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

import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames;
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
import java.util.Set;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.ifNull;
import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author mpostelnicu
 */
@RestController
@CacheConfig(keyGenerator = "genericPagingRequestKeyGenerator", cacheNames = "genericPagingRequestJson")
@Cacheable
public class ShareProcurementsAwardedAGPO extends GenericOCDSController {

    private static final String YOUTH = "Youth";
    private static final String WOMEN = "Women";
    private static final String PWD = "PWD";

    private static final Set<String> AGPO_GROUPS = ImmutableSet.of(YOUTH, WOMEN, PWD);

    public static final String NON_AGPO = "Non-AGPO";

    @ApiOperation(value = "Percent awarded for each target group by following the formula: (sum=(Contract value for "
            + "procurements won by each target group))/sum(Total contracted value of all awards))*100")
    @RequestMapping(value = "/api/shareProcurementsAwardedAgpo",
            method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")

    public List<Document> shareProcurementsAwardedAgpo(@ModelAttribute
                                                       @Valid final YearFilterPagingRequest filter) {
        Aggregation agg = newAggregation(
                match(getYearDefaultFilterCriteria(filter, getAwardDateField())),
                unwind("contracts"),
                match(where(FieldNames.CONTRACTS_VALUE_AMOUNT).exists(true)),
                project()
                        .and(FieldNames.CONTRACTS_VALUE_AMOUNT).as("amount")
                        .and(when(where(FieldNames.CONTRACTS_TARGET_GROUP).nin(AGPO_GROUPS))
                                .then(NON_AGPO)
                                .otherwiseValueOf(ifNull(FieldNames.CONTRACTS_TARGET_GROUP).then(NON_AGPO)))
                        .as("targetGroup"),
                group("targetGroup").sum("amount").as("amount")
        );
        return releaseAgg(agg);
    }
}
