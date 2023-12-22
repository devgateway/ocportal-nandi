/**
 *
 */
package org.devgateway.ocds.web.rest.controller.request;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.media.Schema;
import org.devgateway.ocds.persistence.mongo.Tender;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * @author mpostelnicu Filtering bean applied to all endpoints
 */
public class DefaultFilterPagingRequest extends GenericPagingRequest {

    @Schema(title = "Full text search of of release entities")
    private String text;

    @Schema(title = "Filter by award.status, possible values are available from the OCDS standard page"
            + "http://standard.open-contracting.org/latest/en/schema/codelists/#award-status")
    private TreeSet<String> awardStatus;

    @Schema(title = "Fiscal Year")
    private TreeSet<String> fiscalYear;

    @Schema(title = "Procurement Method Rationale")
    private TreeSet<String> procurementMethodRationale;

    @Schema(title = "Location Type, can be ward or subcounty")
    private TreeSet<String> locationType;

    @Schema(title = "Filter by OCID")
    private TreeSet<String> ocid;

    @Schema(title = "Filter by tender.status, possible values are available from the OCDS standard page"
            + "http://standard.open-contracting.org/latest/en/schema/codelists/#tender-status")
    private TreeSet<String> tenderStatus = Sets.newTreeSet(Arrays.asList(
            Tender.Status.active.toString(), Tender.Status.complete.toString(), Tender.Status.planned.toString()));

    @Schema(title = "This corresponds to the tender.items.classification._id")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> bidTypeId;


    @Schema(title =
            "This corresponds the negated bidTypeId filter, matches elements that are NOT in the TreeSet of Ids")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> notBidTypeId;


    @Schema(title = "This is the id of the organization/procuring entity. "
            + "Corresponds to the OCDS Organization.identifier")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> procuringEntityId;


    @Schema(title = "This corresponds the negated procuringEntityId filter,"
            + " matches elements that are NOT in the TreeSet of Ids")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> notProcuringEntityId;


    @Schema(title = "This is the id of the organization/supplier entity. "
            + "Corresponds to the OCDS Organization.identifier")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> supplierId;

    @Schema(title = "This is the id of the contractor entity. "
            + "Corresponds to the OCDS Organization.identifier")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> contractorId;

    @Schema(title = "This is the id of the organization/buyer entity. "
            + "Corresponds to the OCDS Organization.identifier")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9\\-]*$") String> buyerId;

    @Schema(title = "This is the new bidder format from tender.tenderers._id")
    private TreeSet<String> bidderId;

    @Schema(title = "This is the new bidder format from tender.tenderers._id")
    private ArrayList<String> leftBidderIds;

    @Schema(title = "This is the new bidder format from tender.tenderers._id")
    private ArrayList<String> rightBidderIds;

    @Schema(title = "This will filter after tender.locations._id or contracts.locations._id")
    private TreeSet<String> locationId = new TreeSet<>();

    @Schema(title = "This will filter after tender.procurementMethod")
    private TreeSet<String> procurementMethod = new TreeSet<>();

    @Schema(title = "This will filter after tender.value.amount and will specify a minimum"
            + "Use /api/tenderValueInterval to get the minimum allowed.")
    private BigDecimal minTenderValue;

    @Schema(title = "This will filter after tender.value.amount and will specify a maximum."
            + "Use /api/tenderValueInterval to get the maximum allowed.")
    private BigDecimal maxTenderValue;

    @Schema(title = "This will filter after awards.value.amount and will specify a minimum"
            + "Use /api/awardValueInterval to get the minimum allowed.")
    private BigDecimal minAwardValue;

    @Schema(title = "This will filter after awards.value.amount and will specify a maximum."
            + "Use /api/awardValueInterval to get the maximum allowed.")
    private BigDecimal maxAwardValue;


    @Schema(title = "This will filter releases that were flagged with a specific flag type "
            + ", by flags.flaggedStats.type, so it can filter by FRAUD, RIGGING, etc...")
    private TreeSet<@Pattern(regexp = "^[a-zA-Z0-9]*$") String> flagType;

    @Schema(title = "This will filter releases based on the count of the flags PER RELEASE, which is stored "
            + "in flags.totalFlagged. 0 (zero) is not allowed here, if you want to see all the releases where there "
            + "are no flags, just completely omit this filter.")

    private TreeSet<@Range(min = 1) Integer> totalFlagged;

    @Schema(title = "Filters after tender.submissionMethod='electronicSubmission', also known as"
            + " eBids")
    private Boolean electronicSubmission;

    @Schema(title = "Only show the releases that were flagged by at least one indicator")
    private Boolean flagged;

    @Schema(hidden = true)
    private Boolean awardFiltering = false;

    public DefaultFilterPagingRequest awardFiltering() {
        awardFiltering = true;
        return this;
    }

    public TreeSet<String> getLocationType() {
        return locationType;
    }

    public void setLocationType(TreeSet<String> locationType) {
        this.locationType = locationType;
    }

    public Boolean getAwardFiltering() {
        return awardFiltering;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TreeSet<String> getFlagType() {
        return flagType;
    }

    public void setFlagType(TreeSet<String> flagType) {
        this.flagType = flagType;
    }

    public DefaultFilterPagingRequest() {
        super();
    }

    public TreeSet<String> getBidTypeId() {
        return bidTypeId;
    }

    public void setBidTypeId(final TreeSet<String> bidTypeId) {
        this.bidTypeId = bidTypeId;
    }

    public TreeSet<String> getProcuringEntityId() {
        return procuringEntityId;
    }

    public void setProcuringEntityId(final TreeSet<String> procuringEntityId) {
        this.procuringEntityId = procuringEntityId;
    }

    public TreeSet<String> getLocationId() {
        return locationId;
    }

    public void setLocationId(TreeSet<String> locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getMinTenderValue() {
        return minTenderValue;
    }

    public void setMinTenderValue(final BigDecimal minTenderValueAmount) {
        this.minTenderValue = minTenderValueAmount;
    }

    public BigDecimal getMaxTenderValue() {
        return maxTenderValue;
    }

    public void setMaxTenderValue(final BigDecimal maxTenderValueAmount) {
        this.maxTenderValue = maxTenderValueAmount;
    }

    public BigDecimal getMinAwardValue() {
        return minAwardValue;
    }

    public void setMinAwardValue(final BigDecimal minAwardValue) {
        this.minAwardValue = minAwardValue;
    }

    public BigDecimal getMaxAwardValue() {
        return maxAwardValue;
    }

    public void setMaxAwardValue(final BigDecimal maxAwardValue) {
        this.maxAwardValue = maxAwardValue;
    }

    public TreeSet<String> getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final TreeSet<String> supplierId) {
        this.supplierId = supplierId;
    }

    public TreeSet<String> getNotBidTypeId() {
        return notBidTypeId;
    }

    public void setNotBidTypeId(TreeSet<String> notBidTypeId) {
        this.notBidTypeId = notBidTypeId;
    }

    public TreeSet<String> getNotProcuringEntityId() {
        return notProcuringEntityId;
    }

    public void setNotProcuringEntityId(TreeSet<String> notProcuringEntityId) {
        this.notProcuringEntityId = notProcuringEntityId;
    }

    public Boolean getElectronicSubmission() {
        return electronicSubmission;
    }

    public void setElectronicSubmission(Boolean electronicSubmission) {
        this.electronicSubmission = electronicSubmission;
    }

    public TreeSet<String> getProcurementMethod() {
        return procurementMethod;
    }

    public void setProcurementMethod(TreeSet<String> procurementMethod) {
        this.procurementMethod = procurementMethod;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }


    public TreeSet<String> getAwardStatus() {
        return awardStatus;
    }

    public void setAwardStatus(TreeSet<String> awardStatus) {
        this.awardStatus = awardStatus;
    }

    public TreeSet<String> getBidderId() {
        return bidderId;
    }

    public void setBidderId(TreeSet<String> bidderId) {
        this.bidderId = bidderId;
    }

    public TreeSet<Integer> getTotalFlagged() {
        return totalFlagged;
    }

    public void setTotalFlagged(TreeSet<Integer> totalFlagged) {
        this.totalFlagged = totalFlagged;
    }

    public TreeSet<String> getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(TreeSet<String> buyerId) {
        this.buyerId = buyerId;
    }

    public TreeSet<String> getTenderStatus() {
        return tenderStatus;
    }

    public void setTenderStatus(TreeSet<String> tenderStatus) {
        this.tenderStatus = tenderStatus;
    }

    public TreeSet<String> getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(TreeSet<String> fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public TreeSet<String> getContractorId() {
        return contractorId;
    }

    public void setContractorId(TreeSet<String> contractorId) {
        this.contractorId = contractorId;
    }

    public TreeSet<String> getProcurementMethodRationale() {
        return procurementMethodRationale;
    }

    public void setProcurementMethodRationale(TreeSet<String> procurementMethodRationale) {
        this.procurementMethodRationale = procurementMethodRationale;
    }

    public TreeSet<String> getOcid() {
        return ocid;
    }

    public void setOcid(TreeSet<String> ocid) {
        this.ocid = ocid;
    }

    public ArrayList<String> getLeftBidderIds() {
        return leftBidderIds;
    }

    public void setLeftBidderIds(ArrayList<String> leftBidderIds) {
        this.leftBidderIds = leftBidderIds;
    }

    public ArrayList<String> getRightBidderIds() {
        return rightBidderIds;
    }

    public void setRightBidderIds(ArrayList<String> rightBidderIds) {
        this.rightBidderIds = rightBidderIds;
    }
}
