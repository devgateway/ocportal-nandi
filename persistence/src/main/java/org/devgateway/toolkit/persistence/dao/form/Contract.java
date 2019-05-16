package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author gmutuhu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "procurement_plan_id"), @Index(columnList = "tender_quotation_evaluation_id")})
public class Contract extends AbstractMakueniEntity implements ProjectAttachable {
    @OneToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnore
    private TenderQuotationEvaluation tenderQuotationEvaluation;

    private Double tenderValue;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private Supplier awardee;

    private Date contractDate;

    private Date contractApprovalDate;

    private Date expiryDate;

    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String referenceNumber;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private ProcuringEntity procuringEntity;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<ContractDocument> contractDocs = new ArrayList<>();

    public TenderQuotationEvaluation getTenderQuotationEvaluation() {
        return tenderQuotationEvaluation;
    }

    public void setTenderQuotationEvaluation(final TenderQuotationEvaluation tenderQuotationEvaluation) {
        this.tenderQuotationEvaluation = tenderQuotationEvaluation;
    }

    public Double getTenderValue() {
        return tenderValue;
    }

    public void setTenderValue(final Double tenderValue) {
        this.tenderValue = tenderValue;
    }

    public Supplier getAwardee() {
        return awardee;
    }

    public void setAwardee(final Supplier awardee) {
        this.awardee = awardee;
    }

    public Date getContractApprovalDate() {
        return contractApprovalDate;
    }

    public void setContractApprovalDate(final Date contractApprovalDate) {
        this.contractApprovalDate = contractApprovalDate;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public void setContractDate(final Date contractDate) {
        this.contractDate = contractDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(final String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public ProcuringEntity getProcuringEntity() {
        return procuringEntity;
    }

    public void setProcuringEntity(final ProcuringEntity procuringEntity) {
        this.procuringEntity = procuringEntity;
    }

    public List<ContractDocument> getContractDocs() {
        return contractDocs;
    }

    public void setContractDocs(final List<ContractDocument> contractDocs) {
        this.contractDocs = contractDocs;
    }

    @Override
    public void setLabel(final String label) {
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public ProjectAttachable getProjectAttachable() {
        return tenderQuotationEvaluation;
    }
}