package org.devgateway.toolkit.persistence.dao.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.devgateway.toolkit.persistence.dao.DBConstants;
import org.devgateway.toolkit.persistence.dao.categories.ProcuringEntity;
import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.excel.annotation.ExcelExport;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author gmutuhu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "purchase_requisition_id")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contract extends AbstractPurchaseReqMakueniEntity {
    @ExcelExport(useTranslation = true)
    private BigDecimal contractValue;

    @ExcelExport(name = "Supplier")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private Supplier awardee;

    @ExcelExport(useTranslation = true)
    private Date contractDate;

    @ExcelExport(useTranslation = true)
    private Date contractApprovalDate;

    @ExcelExport(useTranslation = true)
    private Date expiryDate;

    @ExcelExport(useTranslation = true)
    @Column(length = DBConstants.STD_DEFAULT_TEXT_LENGTH)
    private String referenceNumber;

    @ExcelExport(justExport = true, useTranslation = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private ProcuringEntity procuringEntity;

    @ExcelExport(separateSheet = true, useTranslation = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @OrderColumn(name = "index")
    private List<ContractDocument> contractDocs = new ArrayList<>();

    public BigDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(final BigDecimal contractValue) {
        this.contractValue = contractValue;
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
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public String getLabel() {
        return null;
    }

    @Override
    @Transactional
    @JsonIgnore
    @org.springframework.data.annotation.Transient
    public Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.emptyList();
    }
}
