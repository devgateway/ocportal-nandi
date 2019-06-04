package org.devgateway.toolkit.persistence.dao.form;

import org.devgateway.toolkit.persistence.dao.categories.Supplier;
import org.devgateway.toolkit.persistence.spring.PersistenceUtil;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * @author gmutuhu
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(indexes = {@Index(columnList = "purchase_requisition_id")})
public class AwardNotification extends AbstractPurchaseReqMakueniEntity {
    private Date awardDate;

    private Double tenderValue;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    private Supplier awardee;

    private Integer acknowledgementDays;

    public Double getTenderValue() {
        return tenderValue;
    }

    public void setTenderValue(final Double tenderValue) {
        this.tenderValue = tenderValue;
    }

    public Integer getAcknowledgementDays() {
        return acknowledgementDays;
    }

    public void setAcknowledgementDays(final Integer acknowledgementDays) {
        this.acknowledgementDays = acknowledgementDays;
    }

    public Date getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(final Date awardDate) {
        this.awardDate = awardDate;
    }

    public Supplier getAwardee() {
        return awardee;
    }

    public void setAwardee(final Supplier awardee) {
        this.awardee = awardee;
    }

    @Override
    public void setLabel(final String label) {
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    @Transactional
    public Collection<? extends AbstractMakueniEntity> getDirectChildrenEntities() {
        return Collections.singletonList(PersistenceUtil.getNext(getPurchaseRequisitionNotNull().getAwardAcceptance()));
    }
}
