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

import io.swagger.v3.oas.annotations.Operation;
import org.bson.Document;
import org.devgateway.ocds.persistence.mongo.Contract;
import org.devgateway.ocds.web.rest.controller.request.YearFilterPagingRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.SetOperators;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_CONTRACTOR;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_CONTRACTOR_ID;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_DELAYED;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_ID;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_MILESTONES;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_MILESTONE_CODE;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_PAYMENT_AUTHORIZED;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_STATUS;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.CONTRACTS_TITLE;
import static org.devgateway.ocds.persistence.mongo.constants.MongoConstants.FieldNames.OCID;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author mpostelnicu
 */
@RestController
public class ContractStatsController extends GenericOCDSController {

    @Operation(summary = "List of contract names with PMC recommendation to not pay")
    @RequestMapping(value = "/api/pmcNotAuthContractNames", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> pmcNotAuthContractNames(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(CONTRACTS_CONTRACTOR).exists(true).
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        and(CONTRACTS_MILESTONE_CODE).is("PMCReport").
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                project(CONTRACTS).andInclude(OCID),
                unwind(ref(CONTRACTS)),
                unwind(ref(CONTRACTS_MILESTONES)),
                project().and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE)
                        .and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .as(CONTRACTS_PAYMENT_AUTHORIZED)
                        .and(CONTRACTS_TITLE)
                        .as("title")
                        .and(OCID)
                        .as(OCID),
                match(where(CONTRACTS_MILESTONE_CODE).is("PMCReport")
                        .and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .is(false)),
                group(OCID)
                        .first("title").as("title")
        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Top 10 Suppliers with PMC recommendation to not pay")
    @RequestMapping(value = "/api/topSuppliersPmcNotAuthContracts", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> topSuppliersPmcNotAuthContracts(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(CONTRACTS_CONTRACTOR).exists(true).
                        and(CONTRACTS_MILESTONE_CODE).is("PMCReport").
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                unwind(CONTRACTS),
                unwind(CONTRACTS_MILESTONES),
                project().and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .as(CONTRACTS_PAYMENT_AUTHORIZED)
                        .and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE)
                        .and(CONTRACTS_CONTRACTOR_ID)
                        .as(CONTRACTS_CONTRACTOR_ID)
                        .and(CONTRACTS_ID).as(CONTRACTS_ID),
                match(where(CONTRACTS_MILESTONE_CODE).is("PMCReport").and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .is(false)),
                group(CONTRACTS_ID).first(CONTRACTS_CONTRACTOR_ID).as("contractorId"),
                group("contractorId").count().as("count"),
                sort(Sort.Direction.DESC, "count"),
                limit(10)
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "List of contract names with MEReports with delay property")
    @RequestMapping(value = "/api/delayedContractNames", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> delayedContractNames(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(CONTRACTS_CONTRACTOR).exists(true).
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        and(CONTRACTS_MILESTONE_CODE).is("MEReport").
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                project(CONTRACTS).andInclude(OCID),
                unwind(ref(CONTRACTS)),
                unwind(ref(CONTRACTS_MILESTONES)),
                project().and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE)
                        .and(CONTRACTS_TITLE)
                        .as("title")
                        .and(CONTRACTS_DELAYED).as(CONTRACTS_DELAYED)
                        .and(OCID)
                        .as(OCID),
                match(where(CONTRACTS_MILESTONE_CODE).is("MEReport")
                        .and(CONTRACTS_DELAYED).is(true)),
                group(OCID)
                        .first("title").as("title")
        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Top 10 Suppliers who do not complete contracts on expected end date")
    @RequestMapping(value = "/api/topSuppliersDelayedContracts", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> topSuppliersDelayedContracts(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(CONTRACTS_CONTRACTOR).exists(true).
                        and(CONTRACTS_MILESTONE_CODE).is("MEReport").
                        and(CONTRACTS_DELAYED).is(true).
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                unwind(CONTRACTS),
                unwind(CONTRACTS_MILESTONES),
                project().and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE)
                        .and(CONTRACTS_CONTRACTOR_ID)
                        .as(CONTRACTS_CONTRACTOR_ID)
                        .and(CONTRACTS_ID)
                        .as(CONTRACTS_ID).and(CONTRACTS_DELAYED).as(CONTRACTS_DELAYED),
                match(where(CONTRACTS_MILESTONE_CODE).is("MEReport").
                        and(CONTRACTS_DELAYED).is(true)),
                group(CONTRACTS_ID).first(CONTRACTS_CONTRACTOR_ID).as("contractorId"),
                group("contractorId").count().as("count"),
                sort(Sort.Direction.DESC, "count"),
                limit(10)
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "Top 10 Suppliers with inspection report to not pay")
    @RequestMapping(value = "/api/topSuppliersInspectionNoPay", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> topSuppliersInspectionReportNotPay(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(CONTRACTS_CONTRACTOR).exists(true).
                        and(CONTRACTS_MILESTONE_CODE).is("InspectionReport").
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                unwind(CONTRACTS),
                unwind(CONTRACTS_MILESTONES),
                project().and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .as(CONTRACTS_PAYMENT_AUTHORIZED)
                        .and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE)
                        .and(CONTRACTS_CONTRACTOR_ID)
                        .as(CONTRACTS_CONTRACTOR_ID)
                        .and(CONTRACTS_ID)
                        .as(CONTRACTS_ID),
                match(where(CONTRACTS_MILESTONE_CODE).is("InspectionReport")
                        .and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .is(false)),
                group(CONTRACTS_ID).first(CONTRACTS_CONTRACTOR_ID).as("contractorId"),
                group("contractorId").count().as("count"),
                sort(Sort.Direction.DESC, "count"),
                limit(10)
        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Cancelled / Terminated Contracts")
    @RequestMapping(value = "/api/cancelledContracts", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> cancelledContracts(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                project(CONTRACTS),
                unwind(ref(CONTRACTS)),
                project(CONTRACTS).and(getContractDateField()).as("date"),
                projectYearlyMonthly(filter, "date")
                        .and(ConditionalOperators.when(where(CONTRACTS_STATUS)
                                .is(Contract.Status.cancelled.toString())).then(1).otherwise(0)).as("cancelled"),
                groupYearlyMonthly(filter).sum("cancelled").as("countCancelled"),
                transformYearlyGrouping(filter).andInclude("countCancelled"),
                sortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "List of contract names with Inspection Report to not pay")
    @RequestMapping(value = "/api/inspectionNoPayContractNames", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> inspectionNoPayContractNames(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(CONTRACTS_CONTRACTOR).exists(true).
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                project(CONTRACTS).andInclude(OCID),
                unwind(ref(CONTRACTS)),
                unwind(ref(CONTRACTS_MILESTONES)),
                project().and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .as(CONTRACTS_PAYMENT_AUTHORIZED)
                        .and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE)
                        .and(CONTRACTS_TITLE)
                        .as("title")
                        .and(OCID)
                        .as(OCID),
                match(where(CONTRACTS_MILESTONE_CODE).is("InspectionReport")
                        .and(CONTRACTS_PAYMENT_AUTHORIZED)
                        .is(false)),
                group(OCID)
                        .first("title").as("title")
        );

        return releaseAgg(agg);
    }


    @Operation(summary = "Number of Contracts with PMC recommendation to not pay")
    @RequestMapping(value = "/api/pmcNotAuthContracts", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> pmcNotAuthContracts(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).
                        and(atLeastOne(CONTRACTS_MILESTONES)).exists(true).
                        andOperator(getYearDefaultFilterCriteria(
                                filter, getContractDateField()))),
                project(CONTRACTS),
                unwind(ref(CONTRACTS)),
                unwind(ref(CONTRACTS_MILESTONES)),
                project(CONTRACTS).and(getContractDateField()).as("date"),
                project().and("date").as("date").and(CONTRACTS_MILESTONE_CODE)
                        .as(CONTRACTS_MILESTONE_CODE).and(CONTRACTS_ID).as(CONTRACTS_ID)
                        .and(ConditionalOperators.when(where(CONTRACTS_PAYMENT_AUTHORIZED)
                                .is(false)).then(1).otherwise(0))
                        .as("countReportsNotAuthorized"),
                match(where(CONTRACTS_MILESTONE_CODE).is("PMCReport")),
                group(CONTRACTS_ID)
                        .sum("countReportsNotAuthorized").as("countReportsNotAuthorized")
                        .first("date").as("date"),
                projectYearlyMonthly(filter, "date").and(
                        ConditionalOperators.when(where("countReportsNotAuthorized").gt(0)).then(1).otherwise(0))
                        .as("countNotAuthorized")
                        .and(ConditionalOperators.when(where("countReportsNotAuthorized").is(0)).then(1).otherwise(0))
                        .as("countAuthorized"),
                groupYearlyMonthly(filter)
                        .sum("countAuthorized").as("countAuthorized")
                        .sum("countNotAuthorized").as("countNotAuthorized"),
                transformYearlyGrouping(filter).andInclude("countAuthorized", "countNotAuthorized")
                        .andExpression("countNotAuthorized / (countAuthorized + countNotAuthorized) * 100")
                        .as("percentNotAuthorized"),
                sortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }

    @Operation(summary = "Number of contracts not completed on expected end date")
    @RequestMapping(value = "/api/delayedContracts", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json")
    public List<Document> delayedContracts(
            @ModelAttribute @Valid final YearFilterPagingRequest filter) {
        clearTenderStatus(filter);
        Aggregation agg = newAggregation(
                match(where(atLeastOne(CONTRACTS)).exists(true).andOperator(getYearDefaultFilterCriteria(
                        filter, getContractDateField()))),
                project(CONTRACTS),
                match(where(CONTRACTS_MILESTONE_CODE).is("MEReport")),
                unwind(ref(CONTRACTS)),
                project().and(getContractDateField()).as("date").and(
                        SetOperators.AnyElementTrue.arrayAsSet(CONTRACTS_DELAYED))
                        .as("contractDelayed"),
                projectYearlyMonthly(filter, "date")
                        .and(ConditionalOperators.when(where("contractDelayed")
                                .is(true)).then(1).otherwise(0)).as("countDelayed")
                        .and(ConditionalOperators.when(where("contractDelayed")
                                .is(false)).then(1).otherwise(0)).as("countOnTime"),
                groupYearlyMonthly(filter)
                        .sum("countDelayed").as("countDelayed")
                        .sum("countOnTime").as("countOnTime"),
                transformYearlyGrouping(filter).andInclude("countOnTime", "countDelayed")
                        .andExpression("countDelayed / (countOnTime + countDelayed) * 100").as("percentDelayed"),
                sortByYearMonth(filter)
        );

        return releaseAgg(agg);
    }

}
