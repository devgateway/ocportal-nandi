package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.devgateway.toolkit.persistence.dao.AbstractChildAuditableEntity;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.ListViewItem;
import org.devgateway.toolkit.persistence.dao.categories.Item;
import org.devgateway.toolkit.persistence.dao.categories.ProcurementMethod;
import org.devgateway.toolkit.persistence.dao.categories.TargetGroup;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author idobre
 * @since 2019-04-05
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
@Table(indexes = {@Index(columnList = "parent_id"), @Index(columnList = "item_id"), @Index(columnList = "description")})
public class PlanItem extends AbstractChildAuditableEntity<ProcurementPlan> implements ListViewItem {
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private Item item;

    @Column(length = DBConstants.MAX_DEFAULT_TEXT_LENGTH_ONE_LINE)
    private String description;

    private Double estimatedCost;

    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String unitOfIssue;

    private Integer quantity;

    private Double unitPrice;

    private Double totalCost;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private ProcurementMethod procurementMethod;

    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String sourceOfFunds;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private TargetGroup targetGroup;

    private Double targetGroupValue;

    private Double quarter1st;

    private Double quarter2nd;

    private Double quarter3rd;

    private Double quarter4th;

    @Transient
    @JsonIgnore
    private Boolean editable = false;

    @Transient
    @JsonIgnore
    private Boolean expanded = false;

    public Item getItem() {
        return item;
    }

    public void setItem(final Item item) {
        this.item = item;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(final Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getUnitOfIssue() {
        return unitOfIssue;
    }

    public void setUnitOfIssue(final String unitOfIssue) {
        this.unitOfIssue = unitOfIssue;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(final Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(final Double totalCost) {
        this.totalCost = totalCost;
    }

    public ProcurementMethod getProcurementMethod() {
        return procurementMethod;
    }

    public void setProcurementMethod(final ProcurementMethod procurementMethod) {
        this.procurementMethod = procurementMethod;
    }

    public String getSourceOfFunds() {
        return sourceOfFunds;
    }

    public void setSourceOfFunds(final String sourceOfFunds) {
        this.sourceOfFunds = sourceOfFunds;
    }

    public TargetGroup getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(final TargetGroup targetGroup) {
        this.targetGroup = targetGroup;
    }

    public Double getTargetGroupValue() {
        return targetGroupValue;
    }

    public void setTargetGroupValue(final Double targetGroupValue) {
        this.targetGroupValue = targetGroupValue;
    }

    public Double getQuarter1st() {
        return quarter1st;
    }

    public void setQuarter1st(final Double quarter1st) {
        this.quarter1st = quarter1st;
    }

    public Double getQuarter2nd() {
        return quarter2nd;
    }

    public void setQuarter2nd(final Double quarter2nd) {
        this.quarter2nd = quarter2nd;
    }

    public Double getQuarter3rd() {
        return quarter3rd;
    }

    public void setQuarter3rd(final Double quarter3rd) {
        this.quarter3rd = quarter3rd;
    }

    public Double getQuarter4th() {
        return quarter4th;
    }

    public void setQuarter4th(final Double quarter4th) {
        this.quarter4th = quarter4th;
    }

    @Override
    public Boolean getEditable() {
        return editable;
    }

    @Override
    public void setEditable(final Boolean editable) {
        this.editable = editable;
    }

    @Override
    public Boolean getExpanded() {
        return expanded;
    }

    @Override
    public void setExpanded(final Boolean expanded) {
        this.expanded = expanded;
    }
}