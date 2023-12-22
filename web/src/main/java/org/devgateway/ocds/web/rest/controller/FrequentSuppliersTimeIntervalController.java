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
import org.devgateway.ocds.persistence.mongo.Award;
import org.devgateway.ocds.persistence.mongo.constants.MongoConstants;
import org.devgateway.ocds.web.rest.controller.request.DefaultFilterPagingRequest;
import org.devgateway.toolkit.persistence.mongo.aggregate.CustomProjectionOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;


/**
 * @author mpostelnicu
 */
@RestController
public class FrequentSuppliersTimeIntervalController extends GenericOCDSController {

    public static String getFrequentSuppliersResponseKey(FrequentSuppliersResponse response) {
        return getFrequentSuppliersResponseKey(response.getIdentifier().getBuyerId(),
                response.getIdentifier().getSupplierId(), response.getIdentifier().getTimeInterval()
        );
    }

    public static String getFrequentSuppliersResponseKey(String procuringEntityId, String supplierId, Integer
            timeInterval) {
        return new StringBuilder(procuringEntityId).append("-").append(supplierId).append("-").append(timeInterval)
                .toString();
    }

    @Operation(summary = "Returns the frequent suppliers of a buyer split by a time interval. "
            + "The time interval is "
            + "a parameter and represents the number of days to take as interval, starting with today and going back "
            + "till the last award date. The awards are grouped by procuringEntity, supplier and the time interval. "
            + "maxAwards parameter is used to designate what is the maximum number of awards granted to one supplier "
            + "by the same buyer inside one timeInterval. The default value for maxAwards is 3 (days) and the"
            + " default value for intervalDays is 365.")
    @RequestMapping(value = "/api/frequentSuppliersTimeInterval", method = {RequestMethod.POST, RequestMethod.GET},
            produces = "application/json")
    public List<FrequentSuppliersResponse> frequentSuppliersTimeInterval(
            @RequestParam(defaultValue = "365", required = false) Integer intervalDays,
            @RequestParam(defaultValue = "3", required = false) Integer maxAwards,
            @RequestParam(required = false) Date now,
            @RequestParam(required = false) String procurementMethod
    ) {

        if (now == null) {
            now = new Date();
        }
        DBObject project = new BasicDBObject();
        project.put(MongoConstants.FieldNames.BUYER_ID, 1);
        project.put(MongoConstants.FieldNames.AWARDS_SUPPLIERS_ID, 1);
        project.put(MongoConstants.FieldNames.AWARDS_DATE, 1);
        project.put(Fields.UNDERSCORE_ID, 0);
        project.put("timeInterval", new BasicDBObject("$ceil", new BasicDBObject(
                "$divide",
                Arrays.asList(new BasicDBObject("$divide", Arrays.asList(new BasicDBObject(
                        "$subtract",
                        Arrays.asList(now, ref(MongoConstants.FieldNames.AWARDS_DATE))
                ), DAY_MS)), intervalDays)
        )));

        DefaultFilterPagingRequest request = new DefaultFilterPagingRequest();
        clearTenderStatus(request);
        if (!ObjectUtils.isEmpty(procurementMethod)) {
            request.getProcurementMethod().add(procurementMethod);
        }

        Aggregation agg = Aggregation.newAggregation(
                match(where(MongoConstants.FieldNames.BUYER_ID).exists(true).and("awards.suppliers.0").exists(true)
                        .and(MongoConstants.FieldNames.AWARDS_DATE).exists(true)
                        .andOperator(getDefaultFilterCriteria(request))),
                unwind("awards"),
                unwind("awards.suppliers"),
                match(where(MongoConstants.FieldNames.AWARDS_STATUS).is(Award.Status.active.toString())),
                new CustomProjectionOperation(project),
                group(Fields.from(
                        Fields.field("buyerId", MongoConstants.FieldNames.BUYER_ID),
                        Fields.field("supplierId", MongoConstants
                                .FieldNames.AWARDS_SUPPLIERS_ID),
                        Fields.field("timeInterval", "timeInterval")
                )).
                        count().as("count"),
                project("count").and("identifier").previousOperation(),
                match(where("count").gte(maxAwards)),
                sort(Sort.Direction.DESC, "count")
        );

        return releaseAgg(agg, FrequentSuppliersResponse.class);
    }

    public static class FrequentSuppliersId implements Serializable {

        private String buyerId;
        private String supplierId;
        private Integer timeInterval;

        public String getBuyerId() {
            return buyerId;
        }

        public void setBuyerId(String buyerId) {
            this.buyerId = buyerId;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }

        public Integer getTimeInterval() {
            return timeInterval;
        }

        public void setTimeInterval(Integer timeInterval) {
            this.timeInterval = timeInterval;
        }

        @Override
        public String toString() {
            return "procuringEntityId=" + buyerId + "; supplierId=" + supplierId
                    + "; timeInterval=" + timeInterval;
        }
    }

    public static class FrequentSuppliersResponse implements Serializable {
        private FrequentSuppliersId identifier;
        private Integer count;

        public FrequentSuppliersId getIdentifier() {
            return identifier;
        }

        public void setIdentifier(FrequentSuppliersId identifier) {
            this.identifier = identifier;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return identifier.toString() + "; count=" + count;
        }
    }

}